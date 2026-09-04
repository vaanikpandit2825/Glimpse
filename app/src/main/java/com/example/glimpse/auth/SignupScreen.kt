package com.example.glimpse.auth

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private object GlimpseColors {
    val Background = Color(0xFFF7F9FF)
    val OnBackground = Color(0xFF151C24)
    val OnSurface = Color(0xFF151C24)
    val Tertiary = Color(0xFF495B70)
    val PrimaryContainer = Color(0xFF0077BE)
    val Primary = Color(0xFF005E97)
    val OutlineVariant = Color(0xFFC0C7D2)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFEDF4FF)
    val SurfaceContainer = Color(0xFFE8EEF9)
    val OnPrimary = Color.White
    val Error = Color(0xFFBA1A1A)
}

private val EpilogueFamily = FontFamily.Default
private val InterFamily = FontFamily.Default

private object GlimpseType {
    val HeadlineXlMobile = TextStyle(
        fontFamily = EpilogueFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.85).sp
    )

    val HeadlineSm = TextStyle(
        fontFamily = EpilogueFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    )

    val BodyMd = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    )

    val BodySm = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )

    val LabelLg = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.14.sp
    )

    val LabelMd = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.36.sp
    )

    val LabelSm = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.44.sp
    )
}

@Composable
fun SignupScreen(
    navController: NavController
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val viewModel: AuthViewModel = viewModel()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlimpseColors.Background)
    ) {
        LensBackgroundArt(
            modifier = Modifier.align(Alignment.TopEnd)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {

                Text(
                    text = "G L I M P S E",
                    style = GlimpseType.LabelMd.copy(
                        fontSize = 16.sp,
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GlimpseColors.OnSurface
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column {
                    Text(
                        text = "Create\nyour circle.",
                        style = GlimpseType.HeadlineXlMobile,
                        color = GlimpseColors.OnBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Stay close to the people who matter.",
                        style = GlimpseType.BodyMd,
                        color = GlimpseColors.Tertiary,
                        modifier = Modifier.widthIn(max = 260.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            2.dp,
                            RoundedCornerShape(16.dp),
                            clip = false
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlimpseColors.SurfaceContainerLowest)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GlimpseColors.SurfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = GlimpseColors.Tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Curated proximity",
                                style = GlimpseType.LabelMd,
                                color = GlimpseColors.OnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "Encounters built on genuine intent",
                                style = GlimpseType.BodySm,
                                color = GlimpseColors.Tertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GlimpseColors.SurfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = GlimpseColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                GlimpseFieldLabel(text = "FULL NAME")

                Spacer(modifier = Modifier.height(4.dp))

                GlimpseTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = ""
                    },
                    placeholder = "Elena Rostova",
                    trailingIcon = Icons.Rounded.Badge,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlimpseFieldLabel(text = "EMAIL ADDRESS")

                Spacer(modifier = Modifier.height(4.dp))

                GlimpseTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    placeholder = "elena@domain.com",
                    trailingIcon = Icons.Rounded.Mail,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlimpseFieldLabel(text = "PASSWORD")

                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        style = GlimpseType.LabelSm,
                        color = GlimpseColors.PrimaryContainer,
                        modifier = Modifier.clickable {
                            passwordVisible = !passwordVisible
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                GlimpseTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    placeholder = "Minimum 8 characters",
                    trailingIcon = Icons.Rounded.Key,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = errorMessage,
                        color = GlimpseColors.Error,
                        style = GlimpseType.BodySm
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            4.dp,
                            RoundedCornerShape(50),
                            clip = false
                        )
                        .clip(RoundedCornerShape(50))
                        .background(GlimpseColors.PrimaryContainer)
                        .clickable {
                            when {
                                name.isBlank() -> {
                                    errorMessage = "Please enter your name"
                                }

                                email.isBlank() -> {
                                    errorMessage = "Please enter the email address"
                                }

                                password.isBlank() -> {
                                    errorMessage = "Please enter the password"
                                }

                                else -> {
                                    errorMessage = ""

                                    viewModel.signUp(
                                        name = name,
                                        email = email,
                                        password = password,
                                        onSuccess = {
                                            navController.navigate("home") {
                                                popUpTo("signup") {
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
                                                    "Password must be at least 6 characters"

                                                it.message?.contains(
                                                    "already",
                                                    true
                                                ) == true ->
                                                    "User with this email already exists"

                                                it.message?.contains(
                                                    "badly formatted",
                                                    true
                                                ) == true ->
                                                    "Please enter a valid email address"

                                                else ->
                                                    "Signup failed. Please try again later"
                                            }
                                        }
                                    )
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Create account",
                            style = GlimpseType.LabelLg,
                            color = GlimpseColors.OnPrimary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = GlimpseColors.OnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Zero algorithmic feeds. Pure spatial discretion.",
                    style = GlimpseType.BodySm,
                    color = GlimpseColors.Tertiary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = GlimpseType.BodyMd,
                        color = GlimpseColors.Tertiary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            navController.navigate("login")
                        }
                    ) {
                        Text(
                            text = "Sign in",
                            style = GlimpseType.BodyMd,
                            fontWeight = FontWeight.SemiBold,
                            color = GlimpseColors.PrimaryContainer
                        )

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = GlimpseColors.PrimaryContainer,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun GlimpseFieldLabel(text: String) {
    Text(
        text = text,
        style = GlimpseType.LabelSm,
        color = GlimpseColors.Tertiary,
        modifier = Modifier.padding(start = 8.dp)
    )
}

@Composable
private fun GlimpseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                1.dp,
                RoundedCornerShape(16.dp),
                clip = false
            )
            .clip(RoundedCornerShape(16.dp))
            .background(GlimpseColors.SurfaceContainerLowest)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = GlimpseType.BodyMd.copy(
                color = GlimpseColors.OnSurface
            ),
            cursorBrush = SolidColor(GlimpseColors.PrimaryContainer),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 28.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = GlimpseType.BodyMd,
                            color = GlimpseColors.OutlineVariant
                        )
                    }

                    innerTextField()
                }
            }
        )

        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = GlimpseColors.OutlineVariant,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(20.dp)
        )
    }
}

@Composable
private fun LensBackgroundArt(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .offset(
                    x = 96.dp,
                    y = (-48).dp
                )
                .size(340.dp)
                .clip(CircleShape)
                .background(
                    GlimpseColors.SurfaceContainerLow.copy(
                        alpha = 0.6f
                    )
                )
        )

        Canvas(
            modifier = Modifier
                .offset(
                    x = 80.dp,
                    y = (-64).dp
                )
                .size(380.dp)
        ) {
            drawCircle(
                color = GlimpseColors.PrimaryContainer.copy(
                    alpha = 0.12f
                ),
                radius = 160.dp.toPx(),
                center = Offset(
                    210.dp.toPx(),
                    170.dp.toPx()
                ),
                style = Stroke(
                    width = 1.dp.toPx()
                )
            )

            drawCircle(
                color = GlimpseColors.PrimaryContainer.copy(
                    alpha = 0.16f
                ),
                radius = 115.dp.toPx(),
                center = Offset(
                    210.dp.toPx(),
                    170.dp.toPx()
                ),
                style = Stroke(
                    width = 1.dp.toPx()
                )
            )

            drawCircle(
                color = GlimpseColors.PrimaryContainer.copy(
                    alpha = 0.18f
                ),
                radius = 70.dp.toPx(),
                center = Offset(
                    210.dp.toPx(),
                    170.dp.toPx()
                ),
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(
                            3.dp.toPx(),
                            5.dp.toPx()
                        ),
                        0f
                    )
                )
            )

            drawCircle(
                color = GlimpseColors.Tertiary.copy(
                    alpha = 0.45f
                ),
                radius = 4.5.dp.toPx(),
                center = Offset(
                    95.dp.toPx(),
                    170.dp.toPx()
                )
            )

            drawCircle(
                color = GlimpseColors.Primary.copy(
                    alpha = 0.06f
                ),
                radius = 14.dp.toPx(),
                center = Offset(
                    160.dp.toPx(),
                    85.dp.toPx()
                )
            )

            drawCircle(
                color = GlimpseColors.OnBackground,
                radius = 6.dp.toPx(),
                center = Offset(
                    160.dp.toPx(),
                    85.dp.toPx()
                )
            )

            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = Offset(
                    160.dp.toPx(),
                    85.dp.toPx()
                )
            )

            drawCircle(
                color = GlimpseColors.PrimaryContainer.copy(
                    alpha = 0.12f
                ),
                radius = 18.dp.toPx(),
                center = Offset(
                    245.dp.toPx(),
                    125.dp.toPx()
                )
            )

            drawCircle(
                color = GlimpseColors.PrimaryContainer,
                radius = 8.dp.toPx(),
                center = Offset(
                    245.dp.toPx(),
                    125.dp.toPx()
                )
            )

            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(
                    245.dp.toPx(),
                    125.dp.toPx()
                )
            )

            drawCircle(
                color = GlimpseColors.Primary.copy(
                    alpha = 0.08f
                ),
                radius = 12.dp.toPx(),
                center = Offset(
                    270.dp.toPx(),
                    235.dp.toPx()
                )
            )

            drawCircle(
                color = GlimpseColors.OnBackground.copy(
                    alpha = 0.85f
                ),
                radius = 5.dp.toPx(),
                center = Offset(
                    270.dp.toPx(),
                    235.dp.toPx()
                )
            )
        }
    }
}