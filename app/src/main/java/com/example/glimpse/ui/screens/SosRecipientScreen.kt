
package com.example.glimpse.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.glimpse.firebase.GlimpseDatabase
import com.example.glimpse.firebase.SosAcknowledgmentRepository
import com.example.glimpse.model.SOSEvent
import com.example.glimpse.model.SosEmergencyDetails
import com.example.glimpse.model.SosEmergencyLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

private val RecipientRed = Color(0xFFDC3545)
private val RecipientPurple = Color(0xFF4F46E5)
private val RecipientBackground = Color(0xFFF8F9FC)
private val RecipientInk = Color(0xFF111827)
private val RecipientMuted = Color(0xFF6B7280)
private val RecipientGreen = Color(0xFF15803D)
private val RecipientOrange = Color(0xFFD97706)
private val RecipientBorder = Color(0xFFE7E9F0)

private const val LocationFreshnessMillis = 120000L

@Composable
fun SosRecipientScreen(
    navController: NavController,
    senderUid: String,
    eventId: String
) {
    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val database = remember {
        GlimpseDatabase.instance
    }

    val acknowledgmentRepository = remember {
        SosAcknowledgmentRepository()
    }

    var currentUid by remember {
        mutableStateOf(auth.currentUser?.uid)
    }

    DisposableEffect(auth) {

        val listener = FirebaseAuth.AuthStateListener {
            currentUid = it.currentUser?.uid
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    key(senderUid, eventId, currentUid) {

        val uid = currentUid

        var event by remember {
            mutableStateOf<SOSEvent?>(null)
        }

        var details by remember {
            mutableStateOf<SosEmergencyDetails?>(null)
        }

        var loading by remember {
            mutableStateOf(true)
        }

        var detailsLoading by remember {
            mutableStateOf(true)
        }

        var loadError by remember {
            mutableStateOf<String?>(null)
        }

        var detailsError by remember {
            mutableStateOf<String?>(null)
        }

        var actionError by remember {
            mutableStateOf<String?>(null)
        }

        var acknowledgedBy by remember {
            mutableStateOf<String?>(null)
        }

        var acknowledgmentAvailable by remember {
            mutableStateOf(false)
        }

        var acknowledging by remember {
            mutableStateOf(false)
        }

        var contactAccepted by remember {
            mutableStateOf(false)
        }

        var contactChecked by remember {
            mutableStateOf(false)
        }

        var connected by remember {
            mutableStateOf(false)
        }

        var retryCount by remember {
            mutableIntStateOf(0)
        }

        var now by remember {
            mutableLongStateOf(System.currentTimeMillis())
        }

        var serverTimeOffset by remember {
            mutableLongStateOf(0L)
        }

        val validKey = remember {
            Regex("[A-Za-z0-9_-]{1,128}")
        }

        val validRequest =
            uid != null &&
                    uid != senderUid &&
                    validKey.matches(senderUid) &&
                    validKey.matches(eventId)

        LaunchedEffect(serverTimeOffset) {

            while (true) {

                now = System.currentTimeMillis() +
                        serverTimeOffset

                delay(5000L)
            }
        }

        DisposableEffect(database) {

            val connectionRef =
                database.getReference(".info/connected")

            val offsetRef =
                database.getReference(".info/serverTimeOffset")

            val connectionListener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    connected =
                        snapshot.getValue(Boolean::class.java) == true
                }

                override fun onCancelled(error: DatabaseError) {
                    connected = false
                }
            }

            val offsetListener = object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    serverTimeOffset =
                        snapshot.getValue(Long::class.java) ?: 0L
                }

                override fun onCancelled(error: DatabaseError) {
                    serverTimeOffset = 0L
                }
            }

            connectionRef.addValueEventListener(
                connectionListener
            )

            offsetRef.addValueEventListener(
                offsetListener
            )

            onDispose {

                connectionRef.removeEventListener(
                    connectionListener
                )

                offsetRef.removeEventListener(
                    offsetListener
                )
            }
        }

        DisposableEffect(validRequest, retryCount) {

            contactAccepted = false
            contactChecked = false

            if (!validRequest || uid == null) {

                contactChecked = true
                loading = false
                loadError = "You cannot access this emergency alert."

                onDispose { }

            } else {

                val contactRef = database
                    .getReference("emergencyContacts")
                    .child(senderUid)
                    .child(uid)

                val listener = object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (auth.currentUser?.uid != uid) {
                            contactAccepted = false
                            contactChecked = true
                            return
                        }

                        contactAccepted =
                            snapshot.child("status")
                                .getValue(String::class.java) ==
                                    "accepted"

                        contactChecked = true

                        if (!contactAccepted) {

                            event = null
                            details = null
                            acknowledgedBy = null
                            acknowledgmentAvailable = false
                            loading = false

                            loadError =
                                "You are no longer an approved emergency contact."
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                        contactAccepted = false
                        contactChecked = true
                        event = null
                        details = null
                        loading = false

                        loadError =
                            "Unable to verify emergency contact access."
                    }
                }

                contactRef.addValueEventListener(listener)

                onDispose {
                    contactRef.removeEventListener(listener)
                }
            }
        }

        DisposableEffect(
            validRequest,
            contactAccepted,
            retryCount
        ) {

            if (
                !validRequest ||
                !contactAccepted ||
                uid == null
            ) {

                event = null
                details = null

                onDispose { }

            } else {

                loading = true
                loadError = null

                val eventRef = database
                    .getReference("sosEvents")
                    .child(senderUid)
                    .child(eventId)

                val listener = object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (auth.currentUser?.uid != uid) {

                            event = null
                            details = null
                            loading = false
                            return
                        }

                        val loadedEvent = try {

                            snapshot.getValue(
                                SOSEvent::class.java
                            )

                        } catch (error: Exception) {
                            null
                        }

                        if (
                            loadedEvent == null ||
                            loadedEvent.id != eventId ||
                            loadedEvent.userUid != senderUid ||
                            loadedEvent.recipientUids[uid] != true
                        ) {

                            event = null
                            details = null

                            loadError =
                                "This emergency is unavailable or you are not an authorized recipient."

                        } else {

                            event = loadedEvent
                            loadError = null
                        }

                        loading = false
                    }

                    override fun onCancelled(error: DatabaseError) {

                        event = null
                        details = null
                        loading = false

                        loadError =
                            "Unable to access this emergency."
                    }
                }

                eventRef.addValueEventListener(listener)

                onDispose {
                    eventRef.removeEventListener(listener)
                }
            }
        }

        val authorizedEvent =
            contactAccepted && event != null

        val activeEvent =
            authorizedEvent && event?.status == "active"

        DisposableEffect(
            authorizedEvent,
            retryCount
        ) {

            acknowledgmentAvailable = false

            if (!authorizedEvent || uid == null) {

                acknowledgedBy = null

                onDispose { }

            } else {

                val acknowledgmentRef = database
                    .getReference("sosAcknowledgements")
                    .child(senderUid)
                    .child(eventId)

                val listener = object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (auth.currentUser?.uid != uid) {

                            acknowledgedBy = null
                            acknowledgmentAvailable = false
                            return
                        }

                        acknowledgedBy =
                            snapshot.child("acknowledgedBy")
                                .getValue(String::class.java)

                        acknowledgmentAvailable = true
                    }

                    override fun onCancelled(error: DatabaseError) {

                        acknowledgmentAvailable = false
                    }
                }

                acknowledgmentRef.addValueEventListener(
                    listener
                )

                onDispose {

                    acknowledgmentRef.removeEventListener(
                        listener
                    )
                }
            }
        }

        DisposableEffect(
            activeEvent,
            retryCount
        ) {

            details = null
            detailsError = null
            detailsLoading = activeEvent

            if (!activeEvent || uid == null) {

                onDispose { }

            } else {

                val detailsRef = database
                    .getReference("sosEmergencyDetails")
                    .child(senderUid)
                    .child(eventId)

                val listener = object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (auth.currentUser?.uid != uid) {

                            details = null
                            detailsLoading = false
                            return
                        }

                        val loadedDetails = try {

                            snapshot.getValue(
                                SosEmergencyDetails::class.java
                            )

                        } catch (error: Exception) {
                            null
                        }

                        if (
                            loadedDetails != null &&
                            loadedDetails.senderUid == senderUid &&
                            loadedDetails.eventId == eventId
                        ) {

                            details = loadedDetails
                            detailsError = null

                        } else {

                            details = null

                            detailsError =
                                "Emergency details have not arrived yet."
                        }

                        detailsLoading = false
                    }

                    override fun onCancelled(error: DatabaseError) {

                        details = null
                        detailsLoading = false

                        detailsError =
                            "Emergency details are currently unavailable."
                    }
                }

                detailsRef.addValueEventListener(listener)

                onDispose {
                    detailsRef.removeEventListener(listener)
                }
            }
        }

        Scaffold(
            containerColor = RecipientBackground,

            topBar = {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {

                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = RecipientInk
                        )
                    }

                    Column {

                        Text(
                            text = "Emergency Alert",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = RecipientInk
                        )

                        Text(
                            text = "GLIMPSE Safety",
                            fontSize = 12.sp,
                            color = RecipientMuted
                        )
                    }
                }
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                if (!connected) {

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = RecipientOrange.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {

                        Text(
                            text = "Offline or reconnecting. Emergency information may be outdated.",
                            color = RecipientOrange,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                when {

                    !contactChecked || loading -> {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            CircularProgressIndicator(
                                color = RecipientPurple
                            )
                        }
                    }

                    loadError != null ||
                            !contactAccepted ||
                            event == null -> {

                        RecipientInfoCard(
                            title = "Emergency unavailable",
                            description = loadError
                                ?: "Unable to access this emergency."
                        )

                        Button(
                            onClick = {
                                retryCount++
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RecipientPurple
                            )
                        ) {
                            Text("Retry")
                        }
                    }

                    else -> {

                        val sos = event

                        if (sos != null) {

                            val active =
                                sos.status == "active"

                            val acknowledged =
                                !acknowledgedBy.isNullOrBlank()

                            val acknowledgedByMe =
                                acknowledged &&
                                        acknowledgedBy == uid

                            val visibleDetails =
                                if (active && contactAccepted)
                                    details
                                else
                                    null

                            EmergencyStatusHeader(
                                status = sos.status,
                                acknowledged = acknowledged,
                                acknowledgedByMe =
                                    acknowledgedByMe
                            )

                            EmergencySenderCard(
                                details = visibleDetails,
                                loading =
                                    active && detailsLoading,
                                active = active
                            )

                            RecipientInfoCard(
                                title = "SOS activated",
                                description =
                                    formatSosTime(sos.createdAt)
                            )

                            if (active) {

                                val latest =
                                    visibleDetails?.latestLocation

                                val original =
                                    SosEmergencyLocation(
                                        latitude = sos.latitude,
                                        longitude = sos.longitude,
                                        timestamp =
                                            sos.locationTimestamp ?: 0L
                                    )

                                val latestValid =
                                    isValidEmergencyLocation(latest)

                                val originalValid =
                                    isValidEmergencyLocation(original)

                                val bestLocation = when {

                                    latestValid &&
                                            originalValid &&
                                            latest!!.timestamp >=
                                            original.timestamp ->
                                        latest

                                    originalValid ->
                                        original

                                    latestValid ->
                                        latest

                                    else ->
                                        null
                                }

                                val fromLatestUpdate =
                                    bestLocation != null &&
                                            latestValid &&
                                            bestLocation == latest

                                EmergencyLocationCard(
                                    title = "Latest emergency location",
                                    location = bestLocation,
                                    now = now,
                                    connected = connected,
                                    latestUpdate = fromLatestUpdate,
                                    activationSnapshot = false,
                                    onError = {
                                        actionError = it
                                    }
                                )

                                EmergencyLocationCard(
                                    title = "Location at SOS activation",
                                    location = original,
                                    now = now,
                                    connected = connected,
                                    latestUpdate = false,
                                    activationSnapshot = true,
                                    onError = {
                                        actionError = it
                                    }
                                )

                                detailsError?.let {

                                    RecipientInfoCard(
                                        title = "Emergency updates",
                                        description = it
                                    )

                                    OutlinedButton(
                                        onClick = {
                                            retryCount++
                                        },
                                        modifier =
                                            Modifier.fillMaxWidth()
                                    ) {
                                        Text("Refresh emergency details")
                                    }
                                }

                            } else {

                                RecipientInfoCard(
                                    title = "Emergency location sharing",
                                    description =
                                        "Emergency-specific location sharing has ended."
                                )
                            }

                            RecipientInfoCard(
                                title = "Acknowledgment status",

                                description = when {

                                    acknowledgedByMe ->
                                        "You have acknowledged this SOS."

                                    acknowledged ->
                                        "Another emergency contact has acknowledged this SOS."

                                    !acknowledgmentAvailable ||
                                            !connected ->
                                        "Current acknowledgment status cannot be confirmed."

                                    !active ->
                                        "This emergency is no longer active."

                                    else ->
                                        "Awaiting acknowledgment."
                                }
                            )

                            if (active && !acknowledged) {

                                Button(
                                    onClick = {

                                        if (
                                            auth.currentUser?.uid != uid ||
                                            !contactAccepted ||
                                            event?.status != "active"
                                        ) {
                                            actionError =
                                                "Emergency access has changed."
                                        } else {

                                            acknowledging = true
                                            actionError = null

                                            acknowledgmentRepository
                                                .acknowledgeSos(
                                                    senderUid =
                                                        senderUid,
                                                    eventId =
                                                        eventId,

                                                    onSuccess = {
                                                            responder, _ ->

                                                        if (
                                                            auth.currentUser?.uid ==
                                                            uid
                                                        ) {

                                                            acknowledgedBy =
                                                                responder

                                                            acknowledging =
                                                                false
                                                        }
                                                    },

                                                    onFailure = {
                                                            message ->

                                                        if (
                                                            auth.currentUser?.uid ==
                                                            uid
                                                        ) {

                                                            actionError =
                                                                message

                                                            acknowledging =
                                                                false
                                                        }
                                                    }
                                                )
                                        }
                                    },

                                    enabled =
                                        !acknowledging &&
                                                connected &&
                                                contactAccepted &&
                                                uid != null,

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),

                                    shape =
                                        RoundedCornerShape(16.dp),

                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor =
                                                RecipientRed
                                        )
                                ) {

                                    if (acknowledging) {

                                        CircularProgressIndicator(
                                            modifier =
                                                Modifier.size(22.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )

                                    } else {

                                        Text(
                                            text =
                                                "ACKNOWLEDGE EMERGENCY",
                                            fontWeight =
                                                FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text =
                                        "Acknowledge only if you can respond. This requests an end to further SOS escalation.",
                                    color = RecipientMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier =
                                        Modifier.fillMaxWidth()
                                )
                            }

                            actionError?.let {

                                Text(
                                    text = it,
                                    color = RecipientRed,
                                    fontSize = 13.sp
                                )
                            }

                            Text(
                                text =
                                    "Acknowledgment does not mean that the sender is safe. Contact emergency services directly when necessary.",
                                color = RecipientMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyStatusHeader(
    status: String,
    acknowledged: Boolean,
    acknowledgedByMe: Boolean
) {

    val active = status == "active"

    val headerColor = when {

        !active ->
            RecipientMuted

        acknowledged ->
            RecipientGreen

        else ->
            RecipientRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                headerColor,
                RoundedCornerShape(22.dp)
            )
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector =
                if (acknowledged)
                    Icons.Outlined.CheckCircle
                else
                    Icons.Outlined.NotificationsActive,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(46.dp)
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = when {

                status == "resolved" ->
                    "SOS RESOLVED"

                status == "cancelled" ||
                        status == "canceled" ->
                    "SOS CANCELLED"

                !active ->
                    "SOS INACTIVE"

                acknowledged ->
                    "SOS ACKNOWLEDGED"

                else ->
                    "EMERGENCY SOS"
            },

            color = Color.White,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = when {

                !active ->
                    "This emergency is no longer active."

                acknowledgedByMe ->
                    "You acknowledged this emergency. The SOS remains active."

                acknowledged ->
                    "An emergency contact has responded. The SOS remains active."

                else ->
                    "A trusted contact has requested assistance."
            },

            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun EmergencySenderCard(
    details: SosEmergencyDetails?,
    loading: Boolean,
    active: Boolean
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            RecipientBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = "Emergency sender",
                color = RecipientInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            RecipientPurple.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = RecipientPurple,
                        modifier = Modifier.size(36.dp)
                    )

                    if (
                        active &&
                        !details?.profilePhotoUrl.isNullOrBlank()
                    ) {

                        AsyncImage(
                            model = details?.profilePhotoUrl,
                            contentDescription =
                                "Emergency sender profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = when {

                            loading ->
                                "Loading sender..."

                            !details?.name.isNullOrBlank() ->
                                details?.name.orEmpty()

                            else ->
                                "Trusted emergency contact"
                        },

                        color = RecipientInk,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text =
                            if (!details?.glimpseId.isNullOrBlank())
                                "GLIMPSE ID: ${details?.glimpseId}"
                            else
                                "GLIMPSE ID unavailable",

                        color = RecipientMuted,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text =
                            if (active)
                                "Emergency SOS active"
                            else
                                "Emergency ended",

                        color =
                            if (active)
                                RecipientRed
                            else
                                RecipientMuted,

                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmergencyLocationCard(
    title: String,
    location: SosEmergencyLocation?,
    now: Long,
    connected: Boolean,
    latestUpdate: Boolean,
    activationSnapshot: Boolean,
    onError: (String) -> Unit
) {

    val context = LocalContext.current

    val valid =
        isValidEmergencyLocation(location)

    val timestamp =
        location?.timestamp ?: 0L

    val age =
        (now - timestamp).coerceAtLeast(0L)

    val recent =
        valid &&
                timestamp > 0L &&
                timestamp <= now + 30000L &&
                age <= LocationFreshnessMillis

    val statusText = when {

        !valid ->
            "LOCATION UNAVAILABLE"

        activationSnapshot ->
            "SAVED LOCATION"

        !connected ->
            "OFFLINE · LAST KNOWN"

        timestamp <= 0L ||
                timestamp > now + 30000L ->
            "LOCATION TIME UNCONFIRMED"

        latestUpdate && recent ->
            "RECENT LOCATION UPDATE"

        else ->
            "LAST KNOWN LOCATION"
    }

    val statusColor = when {

        !valid ->
            RecipientMuted

        activationSnapshot ->
            RecipientPurple

        connected && latestUpdate && recent ->
            RecipientGreen

        else ->
            RecipientOrange
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            RecipientBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = RecipientPurple
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = RecipientInk,
                    fontSize = 15.sp
                )
            }

            Surface(
                color = statusColor.copy(alpha = 0.10f),
                shape = RoundedCornerShape(8.dp)
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 7.dp
                    ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                statusColor,
                                CircleShape
                            )
                    )

                    Spacer(Modifier.width(7.dp))

                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (valid && location != null) {

                val latitude = location.latitude
                val longitude = location.longitude

                Text(
                    text = "$latitude, $longitude",
                    color = RecipientInk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text =
                        "Recorded: ${formatSosTime(timestamp)}",
                    color = RecipientMuted,
                    fontSize = 12.sp
                )

                Text(
                    text =
                        if (
                            timestamp > 0L &&
                            timestamp <= now + 30000L
                        )
                            formatLocationAge(age)
                        else
                            "Location age unavailable",

                    color = statusColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                OutlinedButton(
                    onClick = {

                        try {

                            val uri = Uri.parse(
                                "geo:$latitude,$longitude?q=$latitude,$longitude"
                            )

                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    uri
                                )
                            )

                        } catch (error: Exception) {

                            onError(
                                "No compatible map application is available."
                            )
                        }
                    },

                    modifier = Modifier.fillMaxWidth(),

                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RecipientPurple
                    )
                ) {
                    Text("View location on map")
                }

                Button(
                    onClick = {

                        try {

                            val uri = Uri.parse(
                                "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
                            )

                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    uri
                                )
                            )

                        } catch (error: Exception) {

                            onError(
                                "Unable to open directions."
                            )
                        }
                    },

                    modifier = Modifier.fillMaxWidth(),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = RecipientPurple
                    )
                ) {
                    Text("Get directions")
                }

                Text(
                    text = when {

                        activationSnapshot ->
                            "This location was saved when the SOS was activated."

                        !connected ->
                            "Showing available saved information. New updates require a connection."

                        latestUpdate && recent ->
                            "Updates automatically when new location information arrives."

                        else ->
                            "This location may be outdated. The sender may have moved."
                    },

                    color = RecipientMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

            } else {

                Text(
                    text =
                        "No valid location is currently available for this emergency.",
                    color = RecipientMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun RecipientInfoCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            RecipientBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                color = RecipientInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = description,
                color = RecipientMuted,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

private fun isValidEmergencyLocation(
    location: SosEmergencyLocation?
): Boolean {

    val latitude =
        location?.latitude ?: return false

    val longitude =
        location.longitude ?: return false

    return latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0
}

private fun formatSosTime(
    timestamp: Long
): String {

    if (timestamp <= 0L) {
        return "Unavailable"
    }

    return DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT
    ).format(Date(timestamp))
}

private fun formatLocationAge(
    ageMillis: Long
): String {

    val seconds =
        ageMillis / 1000L

    val minutes =
        seconds / 60L

    val hours =
        minutes / 60L

    val days =
        hours / 24L

    return when {

        seconds < 10L ->
            "Recorded just now"

        seconds < 60L ->
            "Recorded $seconds seconds ago"

        minutes == 1L ->
            "Recorded 1 minute ago"

        minutes < 60L ->
            "Recorded $minutes minutes ago"

        hours == 1L ->
            "Recorded 1 hour ago"

        hours < 24L ->
            "Recorded $hours hours ago"

        days == 1L ->
            "Recorded 1 day ago"

        else ->
            "Recorded $days days ago"
    }
}
