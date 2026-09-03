package com.example.glimpse.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private val Primary = Color(0xFF005E97)
private val PrimaryContainer = Color(0xFF0077BE)
private val SecondaryContainer = Color(0xFF81C1FF)
private val Background = Color(0xFFF7F9FF)
private val Surface = Color(0xFFF7F9FF)
private val SurfaceLowest = Color(0xFFFFFFFF)
private val OnBackground = Color(0xFF151C24)
private val Tertiary = Color(0xFF495B70)
private val TertiaryDim = Color(0xFFB5C8E0)

@Composable
fun LoginScreen(
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val viewModel: AuthViewModel = viewModel()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Surface)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier

                ) {

                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val pingTransition = rememberInfiniteTransition(label = "pingHalo")

                val pingRadius by pingTransition.animateFloat(
                    initialValue = 14f,
                    targetValue = 28f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 1400,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pingRadius"
                )

                val pingAlpha by pingTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 1400,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pingAlpha"
                )

                Canvas(
                    modifier = Modifier
                        .size(330.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 80.dp, y = (-48).dp)
                ) {
                    val center = Offset(
                        x = 210.dp.toPx(),
                        y = 120.dp.toPx()
                    )

                    drawCircle(
                        color = PrimaryContainer.copy(alpha = 0.18f),
                        radius = 160.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.25.dp.toPx())
                    )

                    drawCircle(
                        color = PrimaryContainer.copy(alpha = 0.10f),
                        radius = 110.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    drawCircle(
                        color = PrimaryContainer.copy(alpha = 0.07f),
                        radius = 58.dp.toPx(),
                        center = center,
                        style = Stroke(width = 0.75.dp.toPx())
                    )

                    val alphaCenter = Offset(
                        x = 78.dp.toPx(),
                        y = 88.dp.toPx()
                    )

                    drawCircle(
                        color = SecondaryContainer.copy(alpha = pingAlpha),
                        radius = pingRadius.dp.toPx(),
                        center = alphaCenter
                    )

                    drawCircle(
                        color = PrimaryContainer,
                        radius = 6.dp.toPx(),
                        center = alphaCenter
                    )

                    drawCircle(
                        color = Tertiary.copy(alpha = 0.72f),
                        radius = 3.5.dp.toPx(),
                        center = Offset(
                            x = 184.dp.toPx(),
                            y = 64.dp.toPx()
                        )
                    )

                    val betaCenter = Offset(
                        x = 138.dp.toPx(),
                        y = 166.dp.toPx()
                    )

                    drawCircle(
                        color = SurfaceLowest,
                        radius = 5.dp.toPx(),
                        center = betaCenter
                    )

                    drawCircle(
                        color = OnBackground,
                        radius = 2.5.dp.toPx(),
                        center = betaCenter
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 24.dp,
                            bottom = 42.dp
                        )
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "G L I M P S E",
                                color = OnBackground,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 4.2.sp
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Box(
                                modifier = Modifier
                                    .offset(y = (-1).dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryContainer)
                            )
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        Text(
                            text = "Welcome\nback.",
                            color = OnBackground,
                            fontSize = 36.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.7).sp
                        )

                        Spacer(modifier = Modifier.height(11.dp))

                        Text(
                            text = "Your people are waiting.",
                            color = Tertiary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column {
                        Text(
                            text = "Email",
                            color = Tertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(7.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = ""
                            },
                            placeholder = {
                                Text(
                                    text = "name@domain.com",
                                    color = TertiaryDim,
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = loginFieldColors(),
                            textStyle = TextStyle(
                                color = OnBackground,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Password",
                                color = Tertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Forgot password?",
                                color = PrimaryContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {}
                            )
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = ""
                            },
                            placeholder = {
                                Text(
                                    text = "••••••••••••",
                                    color = TertiaryDim,
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            visualTransformation =
                                if (passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        passwordVisible = !passwordVisible
                                    }
                                ) {
                                    Icon(
                                        imageVector =
                                            if (passwordVisible) {
                                                Icons.Rounded.VisibilityOff
                                            } else {
                                                Icons.Rounded.Visibility
                                            },
                                        contentDescription =
                                            if (passwordVisible) {
                                                "Hide password"
                                            } else {
                                                "Show password"
                                            },
                                        tint = Tertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = loginFieldColors(),
                            textStyle = TextStyle(
                                color = OnBackground,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        )

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = errorMessage,
                                color = Color(0xFFBA1A1A),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                when {
                                    email.isBlank() -> {
                                        errorMessage = "Please enter the email id"
                                    }

                                    password.isBlank() -> {
                                        errorMessage = "Please enter the password"
                                    }

                                    else -> {
                                        errorMessage = ""

                                        viewModel.login(
                                            email = email,
                                            password = password,
                                            onSuccess = {
                                                navController.navigate("home") {
                                                    popUpTo("login") {
                                                        inclusive = true
                                                    }
                                                }
                                            },
                                            onFailure = {
                                                errorMessage = when {
                                                    it.message?.contains(
                                                        "password",
                                                        true
                                                    ) == true ->
                                                        "Incorrect Password"

                                                    it.message?.contains(
                                                        "no user",
                                                        true
                                                    ) == true ->
                                                        "No account found with this email"

                                                    it.message?.contains(
                                                        "badly formatted",
                                                        true
                                                    ) == true ->
                                                        "Please enter a valid email"

                                                    else ->
                                                        "Login failed. Please try again later"
                                                }
                                            }
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryContainer
                            ),
                            shape = RoundedCornerShape(26.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Sign in  →",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        HorizontalDivider(
                            color = TertiaryDim.copy(alpha = 0.45f),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "New to GLIMPSE? ",
                                color = Tertiary,
                                fontSize = 11.sp
                            )

                            Text(
                                text = "Create an account",
                                color = PrimaryContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    navController.navigate("signup")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun loginFieldColors() =
    TextFieldDefaults.colors(
        focusedContainerColor = SurfaceLowest,
        unfocusedContainerColor = SurfaceLowest,
        focusedTextColor = OnBackground,
        unfocusedTextColor = OnBackground,
        focusedPlaceholderColor = TertiaryDim,
        unfocusedPlaceholderColor = TertiaryDim,
        cursorColor = PrimaryContainer,
        focusedIndicatorColor = PrimaryContainer,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )