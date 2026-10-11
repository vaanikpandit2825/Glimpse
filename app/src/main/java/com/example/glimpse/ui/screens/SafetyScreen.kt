
package com.example.glimpse.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

private val SafetyPurple = Color(0xFF4F46E5)
private val SafetyPurpleSoft = Color(0xFFEDEBFF)
private val SafetyRed = Color(0xFFDC3545)
private val SafetyRedSoft = Color(0xFFFFEAEE)
private val SafetyInk = Color(0xFF111827)
private val SafetyMuted = Color(0xFF6B7280)
private val SafetyBackground = Color(0xFFF8F9FC)
private val SafetyBorder = Color(0xFFE7E9F0)

private enum class SosScreenState {
    Ready,
    Countdown,
    ActivePreview
}

@Composable
fun SafetyScreen(navController: NavController) {
    var screenState by remember { mutableStateOf(SosScreenState.Ready) }
    var countdown by remember { mutableIntStateOf(5) }

    BackHandler(enabled = screenState != SosScreenState.Ready) {
        screenState = SosScreenState.Ready
    }

    LaunchedEffect(screenState) {
        if (screenState == SosScreenState.Countdown) {
            for (seconds in 5 downTo 1) {
                countdown = seconds
                delay(1000)
            }
            screenState = SosScreenState.ActivePreview
        }
    }

    Scaffold(
        containerColor = SafetyBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SafetyBackground)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (screenState != SosScreenState.Ready) {
                            screenState = SosScreenState.Ready
                        } else {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = SafetyInk
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Safety",
                        color = SafetyInk,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your emergency space",
                        color = SafetyMuted,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = SafetyPurple,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(25.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))

            PreviewNotice()

            Spacer(Modifier.height(22.dp))

            when (screenState) {
                SosScreenState.Ready -> {
                    ReadyContent(
                        onHoldComplete = {
                            countdown = 5
                            screenState = SosScreenState.Countdown
                        }
                    )
                }

                SosScreenState.Countdown -> {
                    CountdownContent(
                        seconds = countdown,
                        onCancel = {
                            screenState = SosScreenState.Ready
                        }
                    )
                }

                SosScreenState.ActivePreview -> {
                    ActivePreviewContent(
                        onStopPreview = {
                            screenState = SosScreenState.Ready
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SafetyPurpleSoft)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Shield,
            contentDescription = null,
            tint = SafetyPurple,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.size(9.dp))
        Text(
            text = "UI preview · No emergency alerts will be sent",
            color = SafetyPurple,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ReadyContent(onHoldComplete: () -> Unit) {
    var isHolding by remember { mutableStateOf(false) }

    val holdProgress by animateFloatAsState(
        targetValue = if (isHolding) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "sosHoldProgress"
    )

    Text(
        text = "Need help?",
        color = SafetyInk,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "Press and hold to start the SOS flow. You can still cancel before the final step.",
        color = SafetyMuted,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp)
    )

    Spacer(Modifier.height(30.dp))

    Box(
        modifier = Modifier
            .size(178.dp)
            .clip(CircleShape)
            .background(SafetyRedSoft)
            .border(1.dp, Color(0xFFF7C9D0), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(142.dp)
                .clip(CircleShape)
                .background(SafetyRed)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            val releasedEarly = withTimeoutOrNull(1500L) {
                                tryAwaitRelease()
                                true
                            }
                            isHolding = false

                            if (releasedEarly == null) {
                                onHoldComplete()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SOS",
                    color = Color.White,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "HOLD",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
            }
        }
    }

    Spacer(Modifier.height(18.dp))

    LinearProgressIndicator(
        progress = { holdProgress },
        modifier = Modifier
            .fillMaxWidth(0.62f)
            .height(4.dp)
            .clip(CircleShape),
        color = SafetyRed,
        trackColor = SafetyBorder
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = if (isHolding) "Keep holding…" else "Hold for 1.5 seconds",
        color = SafetyMuted,
        fontSize = 12.sp
    )

    Spacer(Modifier.height(30.dp))

    InfoCard(
        icon = {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = SafetyPurple
            )
        },
        title = "Location sharing",
        description = "Live location status will be shown here once connected."
    )

    Spacer(Modifier.height(12.dp))

    InfoCard(
        icon = {
            Icon(
                imageVector = Icons.Outlined.NotificationsActive,
                contentDescription = null,
                tint = SafetyPurple
            )
        },
        title = "Trusted contacts",
        description = "Contact delivery and acknowledgements will appear here after integration."
    )
}

@Composable
private fun CountdownContent(
    seconds: Int,
    onCancel: () -> Unit
) {
    Spacer(Modifier.height(22.dp))

    Box(
        modifier = Modifier
            .size(112.dp)
            .clip(CircleShape)
            .background(SafetyRedSoft),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = seconds.toString(),
            color = SafetyRed,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(Modifier.height(24.dp))

    Text(
        text = "Cancel if this was accidental",
        color = SafetyInk,
        fontSize = 23.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(10.dp))

    Text(
        text = "The countdown is a local preview. No one is being contacted.",
        color = SafetyMuted,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(28.dp))

    Button(
        onClick = onCancel,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SafetyRed,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = null
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "Cancel SOS",
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(Modifier.height(12.dp))

    TextButton(onClick = onCancel) {
        Text("I'm safe — go back", color = SafetyMuted)
    }
}

@Composable
private fun ActivePreviewContent(onStopPreview: () -> Unit) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(SafetyRedSoft),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsActive,
            contentDescription = null,
            tint = SafetyRed,
            modifier = Modifier.size(36.dp)
        )
    }

    Spacer(Modifier.height(18.dp))

    Text(
        text = "SOS preview active",
        color = SafetyRed,
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "This is a demonstration of the active SOS screen. No alert has been created or sent.",
        color = SafetyMuted,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(24.dp))

    StatusCard(
        title = "Your location",
        status = "Not connected",
        detail = "Live coordinates and last-updated time will appear here after location integration."
    )

    Spacer(Modifier.height(12.dp))

    StatusCard(
        title = "Trusted contacts",
        status = "Not connected",
        detail = "Delivery, acknowledgement, and response status will appear here after Firebase integration."
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = onStopPreview,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SafetyPurple
        )
    ) {
        Text("End preview", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun InfoCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SafetyBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SafetyPurpleSoft),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = SafetyInk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    color = SafetyMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    status: String,
    detail: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SafetyBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = SafetyInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SafetyRedSoft
                ) {
                    Text(
                        text = status,
                        color = SafetyRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        )
                    )
                }
            }

            Spacer(Modifier.height(9.dp))

            Text(
                text = detail,
                color = SafetyMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
