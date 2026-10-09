package com.example.glimpse.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.glimpse.BuildConfig
import com.example.glimpse.Location.AdaptiveLocationEngine
import com.example.glimpse.Location.AdaptiveLocationTracker
import com.example.glimpse.Location.LocationRepository
import com.example.glimpse.Location.RequestLocationPermission
import com.example.glimpse.firebase.FirebaseRepository
import com.example.glimpse.groups.GroupsViewModel
import com.example.glimpse.model.GlimpseGroup
import com.example.glimpse.model.GroupMember
import com.example.glimpse.model.UserLocation
import com.example.glimpse.util.LocationPlaceUtils
import com.example.glimpse.weather.WeatherData
import com.example.glimpse.weather.WeatherRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

// ─── Design tokens ─────────────────────────────────────────────────────────────
// Backgrounds
private val GSurface      = Color(0xFF111114)
private val GCard         = Color(0xFF1A1A1F)
private val GElevated     = Color(0xFF22222A)

// Text
private val GTextPrimary  = Color(0xFFF0EFE8)
private val GTextSecondary = Color(0xFF787870)
private val GTextTertiary = Color(0xFF444440)

// Accent — warm amber/gold
private val GAmber        = Color(0xFFE8B86D)
private val GAmberSoft    = Color(0xFF2A2218)

// Status
private val GLive         = Color(0xFF4CD964)
private val GRecent       = Color(0xFFFF9500)
private val GOffline      = Color(0xFF48484A)

// Structure
private val GBorder       = Color(0xFF282830)
private val GScrim        = Color(0xE0111114)
private val GMapScrimTop  = Color(0x991A1A1F)

private val MapStyleUrl =
    "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

// ─── Small data class for enriched member+location ─────────────────────────────
private data class MemberLocation(val member: GroupMember, val location: UserLocation)

// ─── Status helpers ─────────────────────────────────────────────────────────────
private fun locationStatusLabel(timestamp: Long): String {
    val age = System.currentTimeMillis() - timestamp
    return when {
        age < 2 * 60_000L -> "Live"
        age < 60 * 60_000L -> "${age / 60_000}m ago"
        else -> "Offline"
    }
}

private fun locationStatusColor(timestamp: Long): Color = when {
    System.currentTimeMillis() - timestamp < 2 * 60_000L -> GLive
    System.currentTimeMillis() - timestamp < 15 * 60_000L -> GRecent
    else -> GOffline
}

private fun getGreeting(): String {
    return when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..20 -> "Good evening,"
        else -> "Good night,"
    }
}

// ─── Battery / Network helpers ───────────────────────────────────────────────
private enum class NetworkStatus(val label: String) {
    WIFI("Wi-Fi"),
    MOBILE("Mobile"),
    ONLINE("Online"),
    OFFLINE("Offline")
}

@Composable
private fun rememberBatteryLevel(): Int {
    val context = LocalContext.current
    var level by remember { mutableIntStateOf(getBatteryLevel(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                intent.batteryPercent()?.let { level = it }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { context.unregisterReceiver(receiver) }
    }

    return level
}

@Composable
private fun rememberNetworkStatus(): NetworkStatus {
    val context = LocalContext.current
    var status by remember { mutableStateOf(getNetworkStatus(context)) }

    DisposableEffect(context) {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                status = caps.toNetworkStatus()
            }
            override fun onLost(network: Network) {
                status = NetworkStatus.OFFLINE
            }
        }
        manager.registerDefaultNetworkCallback(callback)
        onDispose { manager.unregisterNetworkCallback(callback) }
    }

    return status
}

private fun getBatteryLevel(context: Context): Int {
    val batteryManager = context.getSystemService(BatteryManager::class.java)
    return batteryManager
        .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        .coerceIn(0, 100)
}

private fun Intent.batteryPercent(): Int? {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    return if (level < 0 || scale <= 0) null else (level * 100 / scale).coerceIn(0, 100)
}

private fun NetworkCapabilities.toNetworkStatus(): NetworkStatus = when {
    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI
    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.MOBILE
    else -> NetworkStatus.ONLINE
}

private fun getNetworkStatus(context: Context): NetworkStatus {
    val manager = context.getSystemService(ConnectivityManager::class.java)
    val capabilities = manager.activeNetwork
        ?.let { manager.getNetworkCapabilities(it) }
        ?: return NetworkStatus.OFFLINE
    return capabilities.toNetworkStatus()
}

// ─── Initials avatar fallback ──────────────────────────────────────────────────
@Composable
private fun InitialsAvatar(
    name: String,
    size: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val initials = name.trim().split("\\s+".toRegex())
        .take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifEmpty { "?" }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(GElevated),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = GAmber, fontSize = fontSize, fontWeight = FontWeight.SemiBold)
    }
}

// ─── HomeScreen ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationRepository = remember { LocationRepository(context) }
    val adaptiveLocationEngine = remember { AdaptiveLocationEngine() }
    val adaptiveLocationTracker = remember {
        AdaptiveLocationTracker(
            locationRepository = locationRepository,
            locationEngine = adaptiveLocationEngine
        )
    }
    val firebaseRepository = remember { FirebaseRepository() }
    val weatherRepository = remember { WeatherRepository() }
    val groupsViewModel: GroupsViewModel = viewModel()
    val cameraState = rememberCameraState()

    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val currentUid = currentUser?.uid

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var userLocations by remember { mutableStateOf(emptyList<UserLocation>()) }
    var userGroups by remember { mutableStateOf<List<GlimpseGroup>>(emptyList()) }
    var selectedGroup by remember { mutableStateOf<GlimpseGroup?>(null) }
    var groupMembers by remember { mutableStateOf<List<GroupMember>>(emptyList()) }
    var selectedMemberUid by remember { mutableStateOf<String?>(null) }
    var groupSheetVisible by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var profilePhotoUrl by remember { mutableStateOf("") }
    var placeName by remember { mutableStateOf<String?>(null) }
    var weather by remember { mutableStateOf<WeatherData?>(null) }

    val batteryLevel = rememberBatteryLevel()
    val currentBatteryLevel = rememberUpdatedState(batteryLevel)
    val networkStatus = rememberNetworkStatus()

    val myLocation = remember(userLocations, currentUid) {
        userLocations.find { it.uid == currentUid }
    }

    // ── Visible + enriched member locations ──────────────────────────────────
    val visibleLocations = remember(userLocations, selectedGroup) {
        if (selectedGroup == null) userLocations
        else {
            val ids = selectedGroup!!.members.keys
            userLocations.filter { it.uid in ids }
        }
    }

    val memberLocations = remember(groupMembers, visibleLocations) {
        groupMembers.mapNotNull { member ->
            val loc = visibleLocations.firstOrNull { it.uid == member.uid } ?: return@mapNotNull null
            MemberLocation(member, loc)
        }
    }

    val selectedMemberLocation = remember(memberLocations, selectedMemberUid) {
        memberLocations.firstOrNull { it.member.uid == selectedMemberUid }
    }

    // ── Data loading ─────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        firebaseRepository.getCurrentUserProfile(
            onResult = { name, photoUrl ->
                if (name.isNotBlank()) userName = name
                if (photoUrl.isNotBlank()) profilePhotoUrl = photoUrl
            },
            onFailure = { Log.e("GLIMPSE_PROFILE", "Failed to load profile", it) }
        )
    }

    LaunchedEffect(Unit) {
        firebaseRepository.getUsersLocations { locations ->
            userLocations = locations
        }
    }

    LaunchedEffect(Unit) {
        if (currentUid != null) {
            groupsViewModel.getGroup(
                uid = currentUid,
                onSuccess = { groups -> userGroups = groups },
                onFailure = {}
            )
        }
    }

    LaunchedEffect(selectedGroup) {
        val group = selectedGroup ?: return@LaunchedEffect
        groupsViewModel.getGroupMembers(
            memberIds = group.members.keys.toList(),
            onSuccess = { groupMembers = it },
            onFailure = { groupMembers = emptyList() }
        )
    }

    // ── Auto-center on own location ───────────────────────────────────────────
    LaunchedEffect(myLocation) {
        val loc = myLocation ?: return@LaunchedEffect
        cameraState.animateTo(
            CameraPosition(
                target = Position(loc.longitude, loc.latitude),
                zoom = 14.5
            )
        )
    }

    // ── Refresh location ───────────────────────────────────────────────────────
    fun refreshLocation(syncToServer: Boolean) {
        locationRepository.getCurrentLocation { location ->
            if (location == null) return@getCurrentLocation
            LocationPlaceUtils.getPlaceName(
                context = context,
                latitude = location.latitude,
                longitude = location.longitude
            ) { result -> placeName = result }
            scope.launch {
                weatherRepository.getCurrentWeather(
                    latitude = location.latitude,
                    longitude = location.longitude
                )?.let { weather = it }
            }
            scope.launch {
                cameraState.animateTo(
                    CameraPosition(
                        target = Position(location.longitude, location.latitude),
                        zoom = 15.5
                    )
                )
            }
            if (syncToServer && currentUid != null) {
                firebaseRepository.updateLocation(
                    uid = currentUid,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    onSuccess = {
                        firebaseRepository.getUsersLocations { locations ->
                            userLocations = locations
                        }
                    }
                )
            }
        }
    }

    // ── Adaptive location tracker ──────────────────────────────────────────────
    DisposableEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            adaptiveLocationTracker.start(
                batteryLevelProvider = { currentBatteryLevel.value },
                onLocationReceived = { location ->
                    LocationPlaceUtils.getPlaceName(
                        context = context,
                        latitude = location.latitude,
                        longitude = location.longitude
                    ) { result -> placeName = result }
                    if (currentUid != null) {
                        firebaseRepository.updateLocation(
                            uid = currentUid,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            onSuccess = {
                                firebaseRepository.getUsersLocations { locations ->
                                    userLocations = locations
                                }
                            }
                        )
                    }
                }
            )
        }
        onDispose { adaptiveLocationTracker.stop() }
    }

    // ── Location permission request ────────────────────────────────────────────
    if (!locationPermissionGranted) {
        RequestLocationPermission(
            onPermissionGranted = { locationPermissionGranted = true }
        )
    }

    // ── Root layout: everything floats over the map ────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GSurface)
    ) {
        // Layer 1: Map hero fills entire screen
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(MapStyleUrl),
            cameraState = cameraState
        ) {
            visibleLocations.forEach { userLoc ->
                val uid = userLoc.uid
                val isSelf = uid == currentUid
                val dotColor = when {
                    isSelf -> GAmber
                    System.currentTimeMillis() - userLoc.timestamp < 2 * 60_000L -> GLive
                    System.currentTimeMillis() - userLoc.timestamp < 15 * 60_000L -> GRecent
                    else -> GOffline
                }
                val source = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        Point(Position(userLoc.longitude, userLoc.latitude))
                    )
                )
                // Outer glow halo
                CircleLayer(
                    id = "halo-$uid",
                    source = source,
                    radius = const(22.dp),
                    color = const(dotColor.copy(alpha = 0.12f)),
                    opacity = const(1f)
                )
                // Status ring
                CircleLayer(
                    id = "ring-$uid",
                    source = source,
                    radius = const(13.dp),
                    color = const(dotColor.copy(alpha = 0.25f)),
                    opacity = const(1f)
                )
                // Core dot
                CircleLayer(
                    id = "dot-$uid",
                    source = source,
                    radius = const(7.dp),
                    color = const(dotColor),
                    strokeColor = const(Color(0xFF1A1A1F)),
                    strokeWidth = const(2.dp)
                )
                // Selected ring
                if (selectedMemberUid == uid) {
                    CircleLayer(
                        id = "sel-ring-$uid",
                        source = source,
                        radius = const(18.dp),
                        color = const(dotColor.copy(alpha = 0.35f)),
                        opacity = const(1f)
                    )
                }
            }
        }

        // Layer 2: Transparent tap-dismisser
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    selectedMemberUid = null
                    groupSheetVisible = false
                }
        )

        // Layer 3a: GroupPill (TopStart)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(GCard)
                    .border(1.dp, GBorder, RoundedCornerShape(12.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { groupSheetVisible = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Colored dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GAmber)
                    )
                    // Group name
                    Text(
                        text = selectedGroup?.name ?: "Everyone",
                        color = GTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Member count badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GElevated)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${visibleLocations.size}",
                            color = GTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Layer 3b: NavIconsColumn (TopEnd)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 16.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile/avatar
            Box(
                modifier = Modifier
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GCard)
                    .border(1.dp, GBorder, CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { navController.navigate("profile") },
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = profilePhotoUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    InitialsAvatar(
                        name = userName.ifBlank { "?" },
                        size = 36.dp,
                        fontSize = 13.sp
                    )
                }
            }

            // Notifications bell
            Box(
                modifier = Modifier
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GCard)
                    .border(1.dp, GBorder, CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { navController.navigate("connectionRequests") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = GTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Groups
            Box(
                modifier = Modifier
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GCard)
                    .border(1.dp, GBorder, CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { navController.navigate("groups") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = "Groups",
                    tint = GTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Layer 4: GroupSwitcherSheet
        if (groupSheetVisible) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ModalBottomSheet(
                onDismissRequest = { groupSheetVisible = false },
                sheetState = sheetState,
                containerColor = GCard,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 32.dp, height = 3.dp)
                                .background(GBorder, RoundedCornerShape(2.dp))
                        )
                    }
                }
            ) {
                GroupSheetContent(
                    userGroups = userGroups,
                    selectedGroup = selectedGroup,
                    visibleCount = visibleLocations.size,
                    onSelect = { group ->
                        selectedGroup = group
                        groupSheetVisible = false
                    },
                    onDismiss = { groupSheetVisible = false }
                )
            }
        }

        // Layer 5: PeopleStrip
        if (memberLocations.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentHeight()
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(GCard.copy(alpha = 0.92f))
                        .border(1.dp, GBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    val displayedMembers = memberLocations.take(5)
                    val extraCount = memberLocations.size - 5

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(displayedMembers) { memberLoc ->
                            val isSelected = memberLoc.member.uid == selectedMemberUid
                            val isSelf = memberLoc.member.uid == currentUid
                            val statusColor = locationStatusColor(memberLoc.location.timestamp)

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        selectedMemberUid = memberLoc.member.uid
                                        scope.launch {
                                            cameraState.animateTo(
                                                CameraPosition(
                                                    target = Position(
                                                        memberLoc.location.longitude,
                                                        memberLoc.location.latitude
                                                    ),
                                                    zoom = 15.0
                                                )
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box {
                                    // Avatar
                                    val avatarBorderModifier = when {
                                        isSelected -> Modifier.border(2.dp, GAmber, CircleShape)
                                        isSelf -> Modifier.border(1.5.dp, GAmber.copy(alpha = 0.4f), CircleShape)
                                        else -> Modifier.border(1.dp, GBorder, CircleShape)
                                    }
                                    if (memberLoc.member.profilePhotoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = memberLoc.member.profilePhotoUrl,
                                            contentDescription = memberLoc.member.name,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .then(avatarBorderModifier),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        InitialsAvatar(
                                            name = memberLoc.member.name,
                                            size = 40.dp,
                                            fontSize = 14.sp,
                                            modifier = avatarBorderModifier
                                        )
                                    }
                                    // Status dot
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(GCard)
                                            .padding(1.5.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                }
                            }
                        }

                        if (extraCount > 0) {
                            item {
                                Box(
                                    modifier = Modifier.size(52.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(GElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+$extraCount",
                                            color = GTextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Layer 6: GPS FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 90.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GCard)
                    .border(1.dp, GBorder, CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { refreshLocation(syncToServer = false) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.GpsFixed,
                    contentDescription = "My location",
                    tint = GAmber,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Layer 7: PeekCard
        AnimatedVisibility(
            visible = selectedMemberUid != null,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
        ) {
            selectedMemberLocation?.let { ml ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = GCard,
                    border = BorderStroke(1.dp, GBorder),
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: 44dp avatar
                        if (ml.member.profilePhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = ml.member.profilePhotoUrl,
                                contentDescription = ml.member.name,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, GBorder, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            InitialsAvatar(
                                name = ml.member.name,
                                size = 44.dp,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        // Center: name + status
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ml.member.name,
                                color = GTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(locationStatusColor(ml.location.timestamp))
                                )
                                Text(
                                    text = locationStatusLabel(ml.location.timestamp),
                                    color = GTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Right: close button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GElevated)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { selectedMemberUid = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = GTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── GroupSheetContent ────────────────────────────────────────────────────────
@Composable
private fun GroupSheetContent(
    userGroups: List<GlimpseGroup>,
    selectedGroup: GlimpseGroup?,
    visibleCount: Int,
    onSelect: (GlimpseGroup?) -> Unit,
    onDismiss: () -> Unit
) {
    Column {
        Text(
            text = "Switch Group",
            color = GTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 16.dp)
        )

        LazyColumn {
            // "Everyone" tile
            item {
                val isSelected = selectedGroup == null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GAmberSoft else GElevated)
                        .border(
                            1.dp,
                            if (isSelected) GAmber.copy(alpha = 0.4f) else GBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onSelect(null) }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) GAmber else GTextSecondary)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Everyone",
                            color = GTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$visibleCount",
                            color = GTextSecondary,
                            fontSize = 13.sp
                        )
                        if (isSelected) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = GAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            items(userGroups) { group ->
                val isSelected = selectedGroup?.id == group.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GAmberSoft else GElevated)
                        .border(
                            1.dp,
                            if (isSelected) GAmber.copy(alpha = 0.4f) else GBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onSelect(group) }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) GAmber else GTextSecondary)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = group.name,
                            color = GTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${group.members.size}",
                            color = GTextSecondary,
                            fontSize = 13.sp
                        )
                        if (isSelected) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = GAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}
