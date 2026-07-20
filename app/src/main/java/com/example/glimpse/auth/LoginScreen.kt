package com.example.glimpse.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glimpse.R
import com.example.glimpse.ui.theme.GlimpseFonts
import androidx.navigation.NavController

private val AccentBlue     = Color(0xFF6EA8FF)
private val FieldBg        = Color(0xFF1C1C1C)
private val GoogleBg       = Color(0xFF1E1E1E)
private val LabelGray      = Color(0xFF888888)
private val DividerGray    = Color(0xFF2A2A2A)

private val ColumnTopPad   = 120.dp
private val ColumnStartPad = 24.dp
private val ColumnEndPad   = 16.dp
private val ColumnFraction = 0.80f
private val FieldHeight    = 58.dp
private val FieldCorner    = 12.dp
private val ButtonHeight   = 54.dp
private val ButtonCorner   = 14.dp
private val LogoSlotHeight = 0.dp

@Composable
fun LoginScreen(
    navController : NavController
) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val viewModel: AuthViewModel = viewModel()
    var errorMessage by remember{mutableStateOf("")}


    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter            = painterResource(R.drawable.signup_bg),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black.copy(alpha = 0.72f),
                            0.48f to Color.Black.copy(alpha = 0.42f),
                            0.74f to Color.Black.copy(alpha = 0.08f),
                            1.00f to Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Transparent,
                            0.50f to Color.Transparent,
                            1.00f to Color.Black.copy(alpha = 0.70f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(ColumnFraction)
                .padding(
                    start = ColumnStartPad,
                    top   = ColumnTopPad,
                    end   = ColumnEndPad
                ),
            verticalArrangement = Arrangement.Top
        ) {

            Box(modifier = Modifier.height(LogoSlotHeight))

            Spacer(Modifier.height(10.dp))

            Text(
                text  = "Glimpse",
                color = Color.White,
                style = TextStyle(
                    fontFamily    = GlimpseFonts.PlayfairDisplay,
                    fontSize      = 52.sp,
                    fontWeight    = FontWeight.Normal,
                    letterSpacing = (-0.5).sp
                )
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text  = "The people who matter,\nalways close.",
                color = Color.White.copy(alpha = 0.72f),
                style = TextStyle(
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 22.sp
                )
            )

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth(0.78f)
            ) {
                HorizontalDivider(
                    modifier  = Modifier.weight(1f),
                    color     = DividerGray,
                    thickness = 0.6.dp
                )
                Text(
                    text  = "  ✦  ",
                    color = Color.White.copy(alpha = 0.30f),
                    style = TextStyle(fontSize = 10.sp)
                )
                HorizontalDivider(
                    modifier  = Modifier.weight(1f),
                    color     = DividerGray,
                    thickness = 0.6.dp
                )
            }

            Spacer(Modifier.height(24.dp))

            FieldLabel("EMAIL")

            Spacer(Modifier.height(7.dp))

            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                placeholder   = {
                    Text(
                        text  = "you@example.com",
                        color = Color.White.copy(alpha = 0.28f),
                        style = TextStyle(fontSize = 14.sp)
                    )
                },
                singleLine    = true,
                colors        = glimpseFieldColors(),
                shape         = RoundedCornerShape(FieldCorner),
                textStyle     = TextStyle(fontSize = 14.sp, color = Color.White),
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(FieldHeight)
            )

            Spacer(Modifier.height(18.dp))

            FieldLabel("PASSWORD")

            Spacer(Modifier.height(7.dp))

            OutlinedTextField(
                value                = password,
                onValueChange        = { password = it },
                placeholder          = {
                    Text(
                        text  = "••••••••••",
                        color = Color.White.copy(alpha = 0.28f),
                        style = TextStyle(fontSize = 14.sp)
                    )
                },
                singleLine           = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon         = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector        = if (passwordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password"
                            else "Show password",
                            tint               = Color.White.copy(alpha = 0.45f),
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                },
                colors               = glimpseFieldColors(),
                shape                = RoundedCornerShape(FieldCorner),
                textStyle            = TextStyle(fontSize = 14.sp, color = Color.White),
                modifier             = Modifier
                    .fillMaxWidth()
                    .height(FieldHeight)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick  = {
                    if(email.isBlank()){
                        errorMessage = "Please enter the email id"
                    }
                    else if(password.isBlank()){
                        errorMessage = "Please enter the password"
                    }
                    else{


                        errorMessage=""
                    viewModel.login(
                        email     = email,
                        password  = password,
                        onSuccess = {
                            navController.navigate("login"){
                                popUpTo("login"){
                                    inclusive=true
                                }
                            }
                        },

                        onFailure = {
                            errorMessage = when{
                                it.message?.contains("password",true)==true->
                                    "Incorrect Password"
                                it.message?.contains("no user",true)==true->
                                    "No account found with this email"
                                it.message?.contains("badly formatted",true)==true->
                                    "Please enter a valid email"

                                else->
                                    "Login Faile. Please try again later"
                            }

                        }
                    )
                    }
                },
                colors   = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape    = RoundedCornerShape(ButtonCorner),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ButtonHeight)
            ) {
                Text(
                    text  = "Login",
                    color = Color.Black,
                    style = TextStyle(
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier  = Modifier.weight(1f),
                    color     = DividerGray,
                    thickness = 0.6.dp
                )
                Text(
                    text  = "  OR  ",
                    color = LabelGray,
                    style = TextStyle(
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Medium,
                        letterSpacing = 1.2.sp
                    )
                )
                HorizontalDivider(
                    modifier  = Modifier.weight(1f),
                    color     = DividerGray,
                    thickness = 0.6.dp
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick  = {},
                colors   = ButtonDefaults.outlinedButtonColors(containerColor = GoogleBg),
                border   = BorderStroke(0.6.dp, DividerGray),
                shape    = RoundedCornerShape(ButtonCorner),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ButtonHeight)
            ) {

                Spacer(Modifier.width(10.dp))
                Text(
                    text  = "Continue with Google",
                    color = Color.White.copy(alpha = 0.88f),
                    style = TextStyle(
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(
                        color    = Color.White.copy(alpha = 0.42f),
                        fontSize = 13.sp
                    )) {
                        append("Don't have an account?")
                    }
                    withStyle(SpanStyle(
                        color      = AccentBlue,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium
                    )) {
                        append("Sign Up")
                    }
                },
                modifier = Modifier.clickable {
                    navController.navigate("signup")
                }
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text  = text,
        color = LabelGray,
        style = TextStyle(
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Medium,
            letterSpacing = 1.2.sp
        )
    )
}

@Composable
private fun glimpseFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor   = FieldBg,
    unfocusedContainerColor = FieldBg,
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color.White,
    cursorColor             = AccentBlue,
    focusedIndicatorColor   = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor  = Color.Transparent
)