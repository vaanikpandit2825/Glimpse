package com.example.glimpse.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glimpse.firebase.SOSRepository
import com.example.glimpse.model.SOSEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.ui.platform.LocalContext
import com.example.glimpse.ui.screens.vibrate
import kotlinx.coroutines.delay
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
    Creating,
    CreateFailed,
    Active
}

@Composable
fun SafetyScreen(
    navController: NavController,
    incomingEventId: String?=null
) {
    val sosRepository = remember { SOSRepository() }
    val context = LocalContext.current

    var screenState by remember { mutableStateOf(SosScreenState.Ready) }
    var countdown by remember { mutableIntStateOf(3) }
    var sosEvent by remember { mutableStateOf<SOSEvent?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vibrate(context, 100L)
    }

    BackHandler(enabled = screenState != SosScreenState.Ready) {
        when (screenState) {
            SosScreenState.Countdown,
            SosScreenState.CreateFailed -> {
                screenState = SosScreenState.Ready
                errorMessage = null
            }

            SosScreenState.Active -> {
                screenState = SosScreenState.Ready
                sosEvent = null
            }

            SosScreenState.Creating,
            SosScreenState.Ready -> Unit
        }
    }
    LaunchedEffect(incomingEventId) {
        if (!incomingEventId.isNullOrBlank()) {
            sosRepository.getSosEvent(
                eventId = incomingEventId,
                onSuccess = { event ->
                    sosEvent = event
                    errorMessage = null
                    screenState = SosScreenState.Active
                    vibrate(context, 250L)
                },
                onFailure = { error ->
                    errorMessage = error.localizedMessage
                        ?: "Could not load the saved SOS event."
                    screenState = SosScreenState.CreateFailed
                }
            )
        }
    }

    LaunchedEffect(screenState) {
        if (screenState == SosScreenState.Countdown) {
            for (seconds in 3 downTo 1) {
                countdown = seconds
                delay(1000L)
            }
            screenState = SosScreenState.Creating
        }
    }

    LaunchedEffect(screenState) {
        if (screenState == SosScreenState.Creating) {
            sosRepository.createSosevents(
                onSuccess = { event ->
                    sosEvent = event
                    errorMessage = null
                    screenState = SosScreenState.Active
                },
                onFailure = { error ->
                    errorMessage = error.localizedMessage
                        ?: "Couldn't save the SOS. Please try again."
                    screenState = SosScreenState.CreateFailed
                }
            )
        }
    }

    LaunchedEffect(screenState) {
        if (screenState == SosScreenState.Creating) {
            if (!incomingEventId.isNullOrBlank()) {
                sosRepository.getSosEvent(
                    eventId = incomingEventId,
                    onSuccess = { event ->
                        sosEvent = event
                        errorMessage = null
                        screenState = SosScreenState.Active
                    },
                    onFailure = { error ->
                        errorMessage = error.localizedMessage
                            ?: "Couldn't load the existing SOS event."
                        screenState = SosScreenState.CreateFailed
                    }
                )
            } else {
                sosRepository.createSosevents(
                    onSuccess = { event ->
                        sosEvent = event
                        errorMessage = null
                        screenState = SosScreenState.Active
                    },
                    onFailure = { error ->
                        errorMessage = error.localizedMessage
                            ?: "Couldn't save the SOS. Please try again."
                        screenState = SosScreenState.CreateFailed
                    }
                )
            }
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
                    enabled = screenState != SosScreenState.Creating,
                    onClick = {
                        when (screenState) {
                            SosScreenState.Ready -> navController.popBackStack()

                            SosScreenState.Countdown,
                            SosScreenState.CreateFailed -> {
                                screenState = SosScreenState.Ready
                                errorMessage = null
                            }

                            SosScreenState.Active -> {
                                screenState = SosScreenState.Ready
                                sosEvent = null
                            }

                            SosScreenState.Creating -> Unit
                        }
                    }
                ) {
                    Icon(
                        Icons.Outlined.ArrowBack,
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
                    Icons.Outlined.Shield,
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

            PreviewNotice(
                message = when (screenState) {
                    SosScreenState.Ready ->
                        "Hold to begin · Nothing is sent yet"

                    SosScreenState.Countdown ->
                        "Countdown running · You can still cancel"

                    SosScreenState.Creating ->
                        "Saving your SOS to Firebase…"

                    SosScreenState.CreateFailed ->
                        "SOS was not saved · Please retry"

                    SosScreenState.Active ->
                        "SOS record saved · Contact alerts not configured"
                }
            )

            Spacer(Modifier.height(22.dp))

            when (screenState) {
                SosScreenState.Ready -> ReadyContent {
                    countdown = 3
                    errorMessage = null
                    screenState = SosScreenState.Countdown
                }

                SosScreenState.Countdown -> CountdownContent(
                    seconds = countdown,
                    onCancel = {
                        screenState = SosScreenState.Ready
                    }
                )

                SosScreenState.Creating -> CreatingSosContent()

                SosScreenState.CreateFailed -> SosFailureContent(
                    message = errorMessage ?: "Couldn't save the SOS.",
                    onRetry = {
                        errorMessage = null
                        screenState = SosScreenState.Creating
                    },
                    onCancel = {
                        errorMessage = null
                        screenState = SosScreenState.Ready
                    }
                )

                SosScreenState.Active -> ActiveSosContent(
                    event = sosEvent,
                    onDone = {
                        screenState = SosScreenState.Ready
                        sosEvent = null
                    }
                )
            }
        }
    }
}

@Composable
private fun PreviewNotice(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SafetyPurpleSoft)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Shield,
            contentDescription = null,
            tint = SafetyPurple,
            modifier = Modifier.size(19.dp)
        )

        Spacer(Modifier.size(9.dp))

        Text(
            text = message,
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
        animationSpec = tween(1500),
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
                .pointerInput(onHoldComplete) {
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
                Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = SafetyPurple
            )
        },
        title = "Location sharing",
        description = "Your latest saved location will be included if available."
    )

    Spacer(Modifier.height(12.dp))

    InfoCard(
        icon = {
            Icon(
                Icons.Outlined.NotificationsActive,
                contentDescription = null,
                tint = SafetyPurple
            )
        },
        title = "Trusted contacts",
        description = "Contact notifications still need to be configured."
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
        text = "No SOS record is created until the countdown finishes.",
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
        Icon(Icons.Outlined.Close, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text("Cancel SOS", fontWeight = FontWeight.SemiBold)
    }

    Spacer(Modifier.height(12.dp))

    TextButton(onClick = onCancel) {
        Text("I'm safe — go back", color = SafetyMuted)
    }
}

@Composable
private fun CreatingSosContent() {
    Spacer(Modifier.height(60.dp))

    CircularProgressIndicator(
        color = SafetyPurple,
        modifier = Modifier.size(48.dp)
    )

    Spacer(Modifier.height(24.dp))

    Text(
        text = "Saving your SOS",
        color = SafetyInk,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(10.dp))

    Text(
        text = "Recording your emergency request in Firebase. Please keep this screen open.",
        color = SafetyMuted,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SosFailureContent(
    message: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Spacer(Modifier.height(42.dp))

    Icon(
        Icons.Outlined.Close,
        contentDescription = null,
        tint = SafetyRed,
        modifier = Modifier.size(48.dp)
    )

    Spacer(Modifier.height(18.dp))

    Text(
        text = "SOS wasn't saved",
        color = SafetyInk,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(10.dp))

    Text(
        text = message,
        color = SafetyMuted,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        lineHeight = 21.sp
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = onRetry,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SafetyPurple
        )
    ) {
        Text("Retry saving SOS", fontWeight = FontWeight.SemiBold)
    }

    TextButton(onClick = onCancel) {
        Text("Return to safety screen", color = SafetyMuted)
    }
}

@Composable
private fun ActiveSosContent(
    event: SOSEvent?,
    onDone: () -> Unit
) {
    Spacer(Modifier.height(22.dp))

    Icon(
        Icons.Outlined.CheckCircle,
        contentDescription = null,
        tint = SafetyPurple,
        modifier = Modifier.size(64.dp)
    )

    Spacer(Modifier.height(18.dp))

    Text(
        text = "SOS recorded",
        color = SafetyInk,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "Firebase confirmed that your SOS record was saved. Trusted-contact notifications have not been configured yet.",
        color = SafetyMuted,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(24.dp))

    StatusCard(
        title = "SOS record",
        status = "Saved",
        detail = "ID: ${event?.id ?: "Unavailable"}"
    )

    Spacer(Modifier.height(12.dp))

    val hasLocation = event?.latitude != null && event.longitude != null

    StatusCard(
        title = "Location snapshot",
        status = if (hasLocation) "Available" else "Unavailable",
        detail = if (hasLocation) {
            "Coordinates: ${event!!.latitude}, ${event.longitude}"
        } else {
            "No coordinates were included in this SOS record."
        }
    )

    Spacer(Modifier.height(12.dp))

    StatusCard(
        title = "Trusted contacts",
        status = "Not notified",
        detail = "${event?.recipientUids?.size ?: 0} connected contacts recorded as recipients. This does not mean any notification was delivered."
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = onDone,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SafetyPurple
        )
    ) {
        Text("Done", fontWeight = FontWeight.SemiBold)
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, SafetyBorder),
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, SafetyBorder),
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