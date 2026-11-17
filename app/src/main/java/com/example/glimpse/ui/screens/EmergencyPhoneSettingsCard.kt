
package com.example.glimpse.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimpse.ui.theme.OceanBlue
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

private fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

@Composable
fun EmergencyPhoneSettingsCard() {

    val context = LocalContext.current
    val activity = context.findActivity()
    val auth = remember { FirebaseAuth.getInstance() }

    val accountUid = auth.currentUser?.uid

    var phoneNumber by remember(accountUid) {
        mutableStateOf("")
    }

    var otp by remember(accountUid) {
        mutableStateOf("")
    }

    var verificationId by remember(accountUid) {
        mutableStateOf<String?>(null)
    }

    var verifiedPhone by remember(accountUid) {
        mutableStateOf(
            auth.currentUser?.phoneNumber.orEmpty()
        )
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    var isError by remember {
        mutableStateOf(false)
    }

    fun verifyCredential(
        credential: PhoneAuthCredential
    ) {
        val user = auth.currentUser

        if (user == null || user.uid != accountUid) {
            loading = false
            isError = true
            message = "Please log in again."
            return
        }

        loading = true

        // Link the verified phone to the existing account.
        // Do not create or sign into a different account.
        user.linkWithCredential(credential)
            .addOnSuccessListener { result ->

                if (auth.currentUser?.uid != accountUid) {
                    loading = false
                    return@addOnSuccessListener
                }

                verifiedPhone =
                    result.user?.phoneNumber.orEmpty()

                loading = false
                verificationId = null
                otp = ""
                phoneNumber = ""

                isError = verifiedPhone.isBlank()

                message = if (verifiedPhone.isNotBlank()) {
                    "Phone number verified successfully."
                } else {
                    "Unable to confirm the linked number."
                }
            }
            .addOnFailureListener { error ->
                loading = false
                isError = true

                message = error.localizedMessage
                    ?: "Phone verification failed."
            }
    }

    fun sendOtp() {

        if (activity == null) {
            isError = true
            message = "Unable to start phone verification."
            return
        }

        if (auth.currentUser?.uid != accountUid ||
            accountUid == null
        ) {
            isError = true
            message = "Please log in first."
            return
        }

        if (!phoneNumber.matches(Regex("^[6-9][0-9]{9}$"))) {
            isError = true
            message = "Enter a valid 10-digit Indian mobile number."
            return
        }

        loading = true
        isError = false
        message = ""

        val fullNumber = "+91$phoneNumber"

        val callbacks = object :
            PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(
                credential: PhoneAuthCredential
            ) {
                verifyCredential(credential)
            }

            override fun onVerificationFailed(
                exception: FirebaseException
            ) {
                loading = false
                isError = true

                message = exception.localizedMessage
                    ?: "Unable to send OTP."
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = id
                loading = false
                isError = false
                message = "Verification code sent."
            }

            override fun onCodeAutoRetrievalTimeOut(
                id: String
            ) {
                verificationId = id
                loading = false
            }
        }

        val options = PhoneAuthOptions
            .newBuilder(auth)
            .setPhoneNumber(fullNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        try {
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (error: Exception) {
            loading = false
            isError = true
            message = "Unable to start verification."
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            Text(
                text = "Emergency Phone Settings",
                fontSize = 18.sp,
                color = OceanBlue
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Verify your mobile number to enable " +
                        "emergency SOS calling.",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (verifiedPhone.isNotBlank()) {

                Text(
                    text = "Verified mobile number",
                    color = Color(0xFF388E3C),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "••••••${verifiedPhone.takeLast(4)}",
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                EmergencyCallConsent(
                    verifiedPhone = verifiedPhone
                )

            } else {

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { value ->
                        if (
                            value.length <= 10 &&
                            value.all { it.isDigit() }
                        ) {
                            phoneNumber = value
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mobile number") },
                    prefix = { Text("+91 ") },
                    singleLine = true,
                    enabled = !loading &&
                            verificationId == null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (verificationId == null) {

                    Button(
                        onClick = { sendOtp() },
                        enabled = !loading &&
                                phoneNumber.length == 10,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OceanBlue
                        )
                    ) {
                        Text("Send OTP")
                    }

                } else {

                    OutlinedTextField(
                        value = otp,
                        onValueChange = { value ->
                            if (
                                value.length <= 6 &&
                                value.all { it.isDigit() }
                            ) {
                                otp = value
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter OTP") },
                        singleLine = true,
                        enabled = !loading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val id = verificationId

                            if (id != null) {
                                val credential =
                                    PhoneAuthProvider.getCredential(
                                        id,
                                        otp
                                    )

                                verifyCredential(credential)
                            }
                        },
                        enabled = !loading && otp.length == 6,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OceanBlue
                        )
                    ) {
                        Text("Verify Phone Number")
                    }

                    TextButton(
                        enabled = !loading,
                        onClick = {
                            verificationId = null
                            otp = ""
                            message = ""
                        }
                    ) {
                        Text("Change number")
                    }
                }
            }

            if (loading) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (message.isNotBlank()) {

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = if (isError) {
                        Color(0xFFD32F2F)
                    } else {
                        Color(0xFF388E3C)
                    }
                )
            }
        }
    }
}
