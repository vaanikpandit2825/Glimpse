
package com.example.glimpse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimpse.firebase.GlimpseFunctions
import com.example.glimpse.ui.theme.OceanBlue
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EmergencyCallConsent(
    verifiedPhone: String
) {
    val functions = remember {
        GlimpseFunctions.instance
    }

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var consent by remember(uid, verifiedPhone) {
        mutableStateOf(false)
    }

    var loading by remember(uid, verifiedPhone) {
        mutableStateOf(true)
    }

    var settingsLoaded by remember(uid, verifiedPhone) {
        mutableStateOf(false)
    }

    var message by remember(uid, verifiedPhone) {
        mutableStateOf("")
    }

    var isError by remember(uid, verifiedPhone) {
        mutableStateOf(false)
    }

    fun loadSettings() {

        loading = true
        settingsLoaded = false

        functions
            .getHttpsCallable("getSosPhoneSettings")
            .call()
            .addOnSuccessListener { result ->

                val data = result.data as? Map<*, *>

                consent =
                    data?.get("voiceCallConsent") == true

                settingsLoaded = data != null
                loading = false
                message = ""
            }
            .addOnFailureListener {

                loading = false
                settingsLoaded = false
                isError = true

                message =
                    "Unable to load emergency call settings."
            }
    }

    fun updateConsent(enabled: Boolean) {

        loading = true
        message = ""

        functions
            .getHttpsCallable("setSosVoiceConsent")
            .call(mapOf("enabled" to enabled))
            .addOnSuccessListener { result ->

                val data = result.data as? Map<*, *>

                if (data?.get("success") == true) {

                    consent =
                        data["voiceCallConsent"] == true

                    isError = false

                    message = if (consent) {
                        "Emergency call consent enabled."
                    } else {
                        "Emergency call consent disabled."
                    }

                } else {
                    isError = true
                    message = "Unable to update consent."
                }

                loading = false
            }
            .addOnFailureListener {

                loading = false
                isError = true

                message =
                    "Unable to update emergency call consent."
            }
    }

    LaunchedEffect(uid, verifiedPhone) {
        if (uid != null && verifiedPhone.isNotBlank()) {
            loadSettings()
        } else {
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Emergency Voice Calls",
                    fontSize = 16.sp,
                    color = OceanBlue
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Allow GLIMPSE to call you automatically " +
                            "when an accepted emergency contact sends " +
                            "an unanswered SOS.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = consent,
                onCheckedChange = { enabled ->
                    updateConsent(enabled)
                },
                enabled = !loading &&
                        settingsLoaded &&
                        uid != null &&
                        verifiedPhone.isNotBlank(),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = OceanBlue
                )
            )
        }

        if (loading) {

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (message.isNotBlank()) {

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                fontSize = 12.sp,
                color = if (isError) {
                    Color(0xFFD32F2F)
                } else {
                    Color(0xFF388E3C)
                }
            )
        }

        if (!settingsLoaded && !loading &&
            uid != null && verifiedPhone.isNotBlank()
        ) {
            TextButton(
                onClick = { loadSettings() }
            ) {
                Text("Retry loading settings")
            }
        }
    }
}
