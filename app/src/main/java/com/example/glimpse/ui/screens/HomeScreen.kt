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
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.glimpse.ui.theme.GlimpseFonts
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
private val GSurface       = Color(0xFF111114)
private val GCard          = Color(0xFF1A1A1F)
private val GElevated      = Color(0xFF22222A)

// Text
private val GTextPrimary   = Color(0xFFF0EFE8)
private val GTextSecondary = Color(0xFF787870)
private val GTextTertiary  = Color(0xFF444440)

// Accent — warm amber/gold
private val GAmber         = Color(0xFFE8B86D)
private val GAmberSoft     = Color(0xFF2A2218)

// Status
private val GLive          = Color(0xFF4CD964)
private val GRecent        = Color(0xFFFF9500)
private val GOffline       = Color(0xFF48484A)

// Structure
private val GBorder        = Color(0xFF282830)
private val GScrim         = Color(0xE0111114)
private val GMapScrimTop   = Color(0x991A1A1F)

// New branded tokens
private val GIndigo        = Color(0xFF4F46E5)
private val GIndigoSoft    = Color(0xFF1A1850)
private val GSosRed        = Color(0xFFFF3B30)
private val GSosRedSoft    = Color(0xFF2A1218)
private val GShareGreen    = Color(0xFF34C759)
private val GShareGreenSoft = Color(0xFF122216)

private val MapStyleUrl =
    "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

// ─── Small data class for enriched member+location ─────────────────────────────
private data class MemberLocation(val member: GroupMember, val location: UserLocation)

// ─── Status helpers ─────────────────────────────────────────────────────────────
private fun locationStatusLabel(timestamp: Long): String {
    val age = System.currentTimeMillis() - timestamp
    return when {
        age < 2 * 60_000L  -> "Live"
        age < 60 * 60_000L -> "${age / 60_000}m ago"
        else               -> "Offline"
    }
}

private fun locationStatusColor(timestamp: Long): Color = when {
    System.currentTimeMillis() - timestamp < 2 * 60_000L   -> GLive
    System.currentTimeMillis() - timestamp < 15 * 60_000L  -> GRecent
    else                                                    -> GOffline
}

private fun getGreeting(): String {
    return when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
        in 5..11  -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..20 -> "Good evening,"
        else      -> "Good night,"
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
    hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> NetworkStatus.WIFI
    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.MOBILE
    else                                                 -> NetworkStatus.ONLINE
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
    val scope   = rememberCoroutineScope()

    val locationRepository      = remember { LocationRepository(context) }
    val adaptiveLocationEngine  = remember { AdaptiveLocationEngine() }
    val adaptiveLocationTracker = remember {
        AdaptiveLocationTracker(
            locationRepository = locationRepository,
            locationEngine     = adaptiveLocationEngine
        )
    }
    val firebaseRepository = remember { FirebaseRepository() }
    val weatherRepository  = remember { WeatherRepository() }
    val groupsViewModel: GroupsViewModel = viewModel()
    val cameraState = rememberCameraState()

    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val currentUid  = currentUser?.uid

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

    var userLocations    by remember { mutableStateOf(emptyList<UserLocation>()) }
    var userGroups       by remember { mutableStateOf<List<GlimpseGroup>>(emptyList()) }
    var selectedGroup    by remember { mutableStateOf<GlimpseGroup?>(null) }
    var groupMembers     by remember { mutableStateOf<List<GroupMember>>(emptyList()) }
    var selectedFriendUid by remember { mutableStateOf<String?>(null) }
    var groupSheetVisible by remember { mutableStateOf(false) }
    var sosDialogVisible  by remember { mutableStateOf(false) }
    var userName         by remember { mutableStateOf("") }
    var profilePhotoUrl  by remember { mutableStateOf("") }
    var placeName        by remember { mutableStateOf<String?>(null) }
    var weather          by remember { mutableStateOf<WeatherData?>(null) }

    val batteryLevel        = rememberBatteryLevel()
    val currentBatteryLevel = rememberUpdatedState(batteryLevel)
    val networkStatus       = rememberNetworkStatus()

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

    // selectedFriendLocation — derived state used in ModalBottomSheet below
    val selectedFriendLocation = remember(memberLocations, selectedFriendUid) {
        memberLocations.firstOrNull { it.member.uid == selectedFriendUid }
    }

    // ── Data loading ─────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        firebaseRepository.getCurrentUserProfile(
            onResult  = { name, photoUrl ->
                if (name.isNotBlank())     userName = name
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
                uid       = currentUid,
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
                zoom   = 14.5
            )
        )
    }

    // ── Refresh location ───────────────────────────────────────────────────────
    fun refreshLocation(syncToServer: Boolean) {
        locationRepository.getCurrentLocation { location ->
            if (location == null) return@getCurrentLocation
            LocationPlaceUtils.getPlaceName(
                context   = context,
                latitude  = location.latitude,
                longitude = location.longitude
            ) { result -> placeName = result }
            scope.launch {
                weatherRepository.getCurrentWeather(
                    latitude  = location.latitude,
                    longitude = location.longitude
                )?.let { weather = it }
            }
            scope.launch {
                cameraState.animateTo(
                    CameraPosition(
                        target = Position(location.longitude, location.latitude),
                        zoom   = 15.5
                    )
                )
            }
            if (syncToServer && currentUid != null) {
                firebaseRepository.updateLocation(
                    uid       = currentUid,
                    latitude  = location.latitude,
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
                onLocationReceived   = { location ->
                    LocationPlaceUtils.getPlaceName(
                        context   = context,
                        latitude  = location.latitude,
                        longitude = location.longitude
                    ) { result -> placeName = result }
                    if (currentUid != null) {
                        firebaseRepository.updateLocation(
                            uid       = currentUid,
                            latitude  = location.latitude,
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

        // ── Layer 1: Map ──────────────────────────────────────────────────────
        MaplibreMap(
            modifier  = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(MapStyleUrl),
            cameraState = cameraState
        ) {
            visibleLocations.forEach { userLoc ->
                val uid    = userLoc.uid
                val isSelf = uid == currentUid
                val dotColor = when {
                    isSelf -> GAmber
                    System.currentTimeMillis() - userLoc.timestamp < 2 * 60_000L   -> GLive
                    System.currentTimeMillis() - userLoc.timestamp < 15 * 60_000L  -> GRecent
                    else -> GOffline
                }
                val source = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        Point(Position(userLoc.longitude, userLoc.latitude))
                    )
                )
                // Outer glow halo
                CircleLayer(
                    id      = "halo-$uid",
                    source  = source,
                    radius  = const(22.dp),
                    color   = const(dotColor.copy(alpha = 0.12f)),
                    opacity = const(1f)
                )
                // Status ring
                CircleLayer(
                    id      = "ring-$uid",
                    source  = source,
                    radius  = const(13.dp),
                    color   = const(dotColor.copy(alpha = 0.25f)),
                    opacity = const(1f)
                )
                // Core dot
                CircleLayer(
                    id          = "dot-$uid",
                    source      = source,
                    radius      = const(7.dp),
                    color       = const(dotColor),
                    strokeColor = const(Color(0xFF1A1A1F)),
                    strokeWidth = const(2.dp)
                )
                // Selected ring
                if (selectedFriendUid == uid) {
                    CircleLayer(
                        id      = "sel-ring-$uid",
                        source  = source,
                        radius  = const(18.dp),
                        color   = const(dotColor.copy(alpha = 0.35f)),
                        opacity = const(1f)
                    )
                }
            }
        }

        // Dim overlay when a friend is selected
        if (selectedFriendUid != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
        }

        // ── Layer 2: Transparent tap-dismisser ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication      = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    selectedFriendUid = null
                    groupSheetVisible = false
                }
        )

        // ── Layer 3: Unified Top Bar ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group pill chip
            Surface(
                shape          = RoundedCornerShape(10.dp),
                color          = GCard,
                border         = BorderStroke(1.dp, GBorder),
                shadowElevation = 3.dp,
                modifier       = Modifier.clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { groupSheetVisible = true }
            ) {
                Row(
                    modifier            = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(GIndigo)
                    )
                    Text(
                        text       = selectedGroup?.name ?: "Everyone",
                        color      = GTextPrimary,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GIndigoSoft)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text       = "${visibleLocations.size}",
                            color      = GIndigo,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector   = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint          = GTextSecondary,
                        modifier      = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Notification + Profile circles
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Notifications
                Surface(
                    shape           = CircleShape,
                    color           = GCard,
                    border          = BorderStroke(1.dp, GBorder),
                    shadowElevation = 3.dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { navController.navigate("connectionRequests") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint               = GTextPrimary,
                            modifier           = Modifier.size(18.dp)
                        )
                        // Unread dot
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 9.dp, end = 9.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GIndigo)
                        )
                    }
                }
                // Profile
                Surface(
                    shape           = CircleShape,
                    color           = GCard,
                    border          = BorderStroke(1.dp, GBorder),
                    shadowElevation = 3.dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { navController.navigate("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model              = profilePhotoUrl,
                                contentDescription = "Profile",
                                modifier           = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale       = ContentScale.Crop
                            )
                        } else {
                            InitialsAvatar(
                                name     = userName.ifBlank { "?" },
                                size     = 38.dp,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // ── Layer 4: GPS FAB ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 175.dp)
        ) {
            Surface(
                shape           = CircleShape,
                color           = GCard,
                border          = BorderStroke(1.dp, GBorder),
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clickable(
                            indication        = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { refreshLocation(syncToServer = false) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.GpsFixed,
                        contentDescription = "Recenter",
                        tint               = GAmber,
                        modifier           = Modifier.size(19.dp)
                    )
                }
            }
        }

        // ── Layer 5: Bottom Persistent Column ─────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Zone A: People strip (only when there are members)
            if (memberLocations.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(GCard.copy(alpha = 0.95f))
                        .border(1.dp, GBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(memberLocations.take(6)) { memberLoc ->
                            PeopleStripAvatar(
                                memberLoc  = memberLoc,
                                isSelected = memberLoc.member.uid == selectedFriendUid,
                                isSelf     = memberLoc.member.uid == currentUid,
                                onClick    = {
                                    selectedFriendUid = memberLoc.member.uid
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
                                }
                            )
                        }
                        if (memberLocations.size > 6) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(GElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = "+${memberLocations.size - 6}",
                                        color      = GTextSecondary,
                                        fontSize   = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Zone B: Action Row
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share Location chip
                Surface(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    color    = GShareGreenSoft,
                    border   = BorderStroke(1.dp, GShareGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { navController.navigate("connections") }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.NearMe,
                            contentDescription = "Share location",
                            tint               = GShareGreen,
                            modifier           = Modifier.size(18.dp)
                        )
                        Text(
                            text       = "Share Location",
                            color      = GTextPrimary,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines   = 1
                        )
                    }
                }
                // Emergency SOS chip
                Surface(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    color    = GSosRedSoft,
                    border   = BorderStroke(1.dp, GSosRed.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { sosDialogVisible = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.Warning,
                            contentDescription = "Emergency SOS",
                            tint               = GSosRed,
                            modifier           = Modifier.size(18.dp)
                        )
                        Text(
                            text       = "Emergency SOS",
                            color      = GTextPrimary,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines   = 1
                        )
                    }
                }
            }

            // Zone C: Bottom Nav Tabs
            Surface(
                modifier        = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape           = RoundedCornerShape(14.dp),
                color           = GCard,
                border          = BorderStroke(1.dp, GBorder),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    BottomNavItem(
                        icon     = Icons.Rounded.Map,
                        label    = "Map",
                        selected = true,
                        onClick  = { /* already on home */ },
                        modifier = Modifier.weight(1f)
                    )
                    BottomNavItem(
                        icon     = Icons.Outlined.Groups,
                        label    = "Groups",
                        selected = false,
                        onClick  = { navController.navigate("groups") },
                        modifier = Modifier.weight(1f)
                    )
                    BottomNavItem(
                        icon     = Icons.Outlined.People,
                        label    = "Connect",
                        selected = false,
                        onClick  = { navController.navigate("connections") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Layer 6: Friend Detail Sheet ──────────────────────────────────────
        if (selectedFriendLocation != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedFriendUid = null },
                sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor   = GCard,
                shape            = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                dragHandle       = {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp),
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
                FriendDetailSheetContent(
                    memberLoc  = selectedFriendLocation,
                    currentUid = currentUid,
                    onClose    = { selectedFriendUid = null },
                    navController = navController
                )
            }
        }

        // ── Layer 7: Group Switcher Sheet ─────────────────────────────────────
        if (groupSheetVisible) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ModalBottomSheet(
                onDismissRequest = { groupSheetVisible = false },
                sheetState       = sheetState,
                containerColor   = GCard,
                shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                dragHandle       = {
                    Box(
                        modifier         = Modifier
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
                    userGroups   = userGroups,
                    selectedGroup = selectedGroup,
                    visibleCount = visibleLocations.size,
                    onSelect     = { group ->
                        selectedGroup = group
                        groupSheetVisible = false
                    },
                    onDismiss    = { groupSheetVisible = false }
                )
            }
        }

        // ── Layer 8: SOS AlertDialog ──────────────────────────────────────────
        if (sosDialogVisible) {
            AlertDialog(
                onDismissRequest = { sosDialogVisible = false },
                containerColor   = GCard,
                shape            = RoundedCornerShape(20.dp),
                icon             = {
                    Icon(
                        imageVector        = Icons.Rounded.Warning,
                        tint               = GSosRed,
                        contentDescription = null,
                        modifier           = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text       = "Emergency SOS",
                        color      = GTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                },
                text = {
                    Text(
                        text     = "Share your location with your connections immediately?",
                        color    = GTextSecondary,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            sosDialogVisible = false
                            navController.navigate("connections")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GSosRed),
                        shape  = RoundedCornerShape(10.dp)
                    ) {
                        Text("Send SOS", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { sosDialogVisible = false }) {
                        Text("Cancel", color = GTextSecondary)
                    }
                }
            )
        }
    }
}

// ─── PeopleStripAvatar ────────────────────────────────────────────────────────
@Composable
private fun PeopleStripAvatar(
    memberLoc: MemberLocation,
    isSelected: Boolean,
    isSelf: Boolean,
    onClick: () -> Unit
) {
    val statusColor = locationStatusColor(memberLoc.location.timestamp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val borderModifier = when {
            isSelected -> Modifier.border(2.dp, GAmber, CircleShape)
            isSelf     -> Modifier.border(1.5.dp, GAmber.copy(alpha = 0.4f), CircleShape)
            else       -> Modifier.border(1.dp, GBorder, CircleShape)
        }
        Box {
            if (memberLoc.member.profilePhotoUrl.isNotBlank()) {
                AsyncImage(
                    model              = memberLoc.member.profilePhotoUrl,
                    contentDescription = memberLoc.member.name,
                    modifier           = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .then(borderModifier),
                    contentScale       = ContentScale.Crop
                )
            } else {
                InitialsAvatar(
                    name     = memberLoc.member.name,
                    size     = 40.dp,
                    fontSize = 14.sp,
                    modifier = borderModifier
                )
            }
            // Status dot in bottom-right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(GCard)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }
    }
}

// ─── BottomNavItem ────────────────────────────────────────────────────────────
@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) GIndigoSoft else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = if (selected) GIndigo else GTextSecondary,
                modifier           = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text       = label,
            color      = if (selected) GIndigo else GTextSecondary,
            fontSize   = 10.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

// ─── FriendDetailSheetContent ─────────────────────────────────────────────────
@Composable
private fun FriendDetailSheetContent(
    memberLoc: MemberLocation,
    currentUid: String?,
    onClose: () -> Unit,
    navController: NavController
) {
    val age           = System.currentTimeMillis() - memberLoc.location.timestamp
    val isLive        = age < 2 * 60_000L
    val freshnessLabel = when {
        isLive              -> "Live"
        age < 60 * 60_000L -> "${age / 60_000}m ago"
        else                -> "Offline"
    }
    val statusColor = locationStatusColor(memberLoc.location.timestamp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 28.dp)
    ) {
        // Header: avatar + name/status + close button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth()
        ) {
            // 56dp avatar with status color ring
            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                if (memberLoc.member.profilePhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model              = memberLoc.member.profilePhotoUrl,
                        contentDescription = memberLoc.member.name,
                        modifier           = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, statusColor, CircleShape),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, statusColor, CircleShape)
                    ) {
                        InitialsAvatar(
                            name     = memberLoc.member.name,
                            size     = 56.dp,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = memberLoc.member.name,
                    color      = GTextPrimary,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GlimpseFonts.PlayfairDisplay,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text     = locationStatusLabel(memberLoc.location.timestamp),
                        color    = GTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            // Close button
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(GElevated)
                    .clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint               = GTextSecondary,
                    modifier           = Modifier.size(15.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Divider(color = GBorder)
        Spacer(Modifier.height(16.dp))

        // Location info row
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint               = GTextSecondary,
                modifier           = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text     = locationStatusLabel(memberLoc.location.timestamp),
                color    = GTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            // Freshness chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isLive) GLive.copy(alpha = 0.15f) else GElevated)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text       = freshnessLabel,
                    color      = if (isLive) GLive else GTextSecondary,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Action buttons row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick  = {
                    navController.navigate("connections")
                    onClose()
                },
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(10.dp),
                border   = BorderStroke(1.dp, GBorder),
                colors   = ButtonDefaults.outlinedButtonColors(containerColor = GElevated)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.NearMe,
                    contentDescription = null,
                    tint               = GIndigo,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Message", fontSize = 13.sp, color = GTextPrimary)
            }
            OutlinedButton(
                onClick  = { /* directions: external map intent would go here */ },
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(10.dp),
                border   = BorderStroke(1.dp, GBorder),
                colors   = ButtonDefaults.outlinedButtonColors(containerColor = GElevated)
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Directions,
                    contentDescription = null,
                    tint               = GTextSecondary,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Directions", fontSize = 13.sp, color = GTextPrimary)
            }
            OutlinedButton(
                onClick  = {
                    navController.navigate("profile")
                    onClose()
                },
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(10.dp),
                border   = BorderStroke(1.dp, GBorder),
                colors   = ButtonDefaults.outlinedButtonColors(containerColor = GElevated)
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Person,
                    contentDescription = null,
                    tint               = GTextSecondary,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Profile", fontSize = 13.sp, color = GTextPrimary)
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
            text       = "Switch Group",
            color      = GTextPrimary,
            fontSize   = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 16.dp)
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
                            indication        = null,
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
                            text       = "Everyone",
                            color      = GTextPrimary,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier.weight(1f)
                        )
                        Text(
                            text     = "$visibleCount",
                            color    = GTextSecondary,
                            fontSize = 13.sp
                        )
                        if (isSelected) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector        = Icons.Rounded.Check,
                                contentDescription = null,
                                tint               = GAmber,
                                modifier           = Modifier.size(16.dp)
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
                            indication        = null,
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
                            text       = group.name,
                            color      = GTextPrimary,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier.weight(1f)
                        )
                        Text(
                            text     = "${group.members.size}",
                            color    = GTextSecondary,
                            fontSize = 13.sp
                        )
                        if (isSelected) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector        = Icons.Rounded.Check,
                                contentDescription = null,
                                tint               = GAmber,
                                modifier           = Modifier.size(16.dp)
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
