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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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

// ─── Design tokens ────────────────────────────────────────────────────────────
private val GDarkBg = Color(0xFF0C0C0F)
private val GDarkSurface = Color(0xFF16161A)
private val GDarkElevated = Color(0xFF1E1E24)
private val GTextPrimary = Color(0xFFF2F2F7)
private val GTextSecondary = Color(0xFF8E8E9A)
private val GTextTertiary = Color(0xFF4A4A57)
private val GAccent = Color(0xFF3D7EFF)
private val GAccentSoft = Color(0xFF1A2A4A)
private val GLive = Color(0xFF34C759)
private val GRecent = Color(0xFFFF9F0A)
private val GStale = Color(0xFF636366)
private val GBorder = Color(0xFF2C2C35)
private val GHandle = Color(0xFF3A3A45)
private val GOverlayBg = Color(0xCC16161A)

private val MapStyleUrl =
    "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

// ─── Small data class for enriched member+location ────────────────────────────
private data class MemberLocation(val member: GroupMember, val location: UserLocation)

// ─── Status helpers ───────────────────────────────────────────────────────────
private fun locationStatusLabel(timestamp: Long): String {
    val age = System.currentTimeMillis() - timestamp
    return when {
        age < 2 * 60_000L -> "Live"
        age < 60 * 60_000L -> "${age / 60_000}m ago"
        else -> "Offline"
    }
}

private fun locationStatusColor(timestamp: Long): Color {
    val age = System.currentTimeMillis() - timestamp
    return when {
        age < 2 * 60_000L -> GLive
        age < 15 * 60_000L -> GRecent
        else -> GStale
    }
}

private fun locationDotColor(uid: String, currentUid: String?, timestamp: Long): Color {
    return if (uid == currentUid) GAccent
    else locationStatusColor(timestamp)
}

private fun getGreeting(): String {
    return when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..20 -> "Good evening,"
        else -> "Good night,"
    }
}

// ─── Battery helpers ──────────────────────────────────────────────────────────
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

// ─── Initials avatar fallback ─────────────────────────────────────────────────
@Composable
private fun InitialsAvatar(name: String, size: Dp, textSize: TextUnit) {
    val initials = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "?" }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(GAccentSoft),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = GAccent, fontSize = textSize, fontWeight = FontWeight.SemiBold)
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
    var placeName by remember { mutableStateOf<String?>(null) }
    var weather by remember { mutableStateOf<WeatherData?>(null) }
    var userName by remember {
        mutableStateOf(currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "there")
    }
    var profilePhotoUrl by remember {
        mutableStateOf(currentUser?.photoUrl?.toString().orEmpty())
    }
    var userGroups by remember { mutableStateOf<List<GlimpseGroup>>(emptyList()) }
    var selectedGroup by remember { mutableStateOf<GlimpseGroup?>(null) }
    var groupMembers by remember { mutableStateOf<List<GroupMember>>(emptyList()) }
    var groupPickerVisible by remember { mutableStateOf(false) }
    var selectedMemberUid by remember { mutableStateOf<String?>(null) }

    val batteryLevel = rememberBatteryLevel()
    val currentBatteryLevel = rememberUpdatedState(batteryLevel)
    val networkStatus = rememberNetworkStatus()

    val myLocation = remember(userLocations, currentUid) {
        userLocations.find { it.uid == currentUid }
    }

    // ── Visible + enriched member locations ──────────────────────────────────
    val visibleLocations = remember(userLocations, selectedGroup) {
        if (selectedGroup == null) {
            userLocations
        } else {
            val groupMemberIds = selectedGroup!!.members.keys
            userLocations.filter { it.uid in groupMemberIds }
        }
    }

    val memberLocations = remember(groupMembers, visibleLocations) {
        groupMembers.mapNotNull { member ->
            val loc = visibleLocations.firstOrNull { it.uid == member.uid } ?: return@mapNotNull null
            MemberLocation(member, loc)
        }
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

    // ── Auto-center on own location ────────────────────────────────────────
    LaunchedEffect(myLocation) {
        val loc = myLocation ?: return@LaunchedEffect
        cameraState.animateTo(
            CameraPosition(
                target = Position(loc.longitude, loc.latitude),
                zoom = 14.5
            )
        )
    }

    // ── Refresh location ──────────────────────────────────────────────────
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

    // ── Adaptive location tracker ─────────────────────────────────────────
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

    // ── Location permission request ───────────────────────────────────────
    if (!locationPermissionGranted) {
        RequestLocationPermission(
            onPermissionGranted = { locationPermissionGranted = true }
        )
    }

    // ── Root layout: everything floats over the map ───────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GDarkBg)
    ) {
        // 1. Map hero fills entire screen
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(MapStyleUrl),
            cameraState = cameraState
        ) {
            visibleLocations.forEach { userLoc ->
                val uid = userLoc.uid
                val dotColor = locationDotColor(uid, currentUid, userLoc.timestamp)
                val source = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        Point(Position(userLoc.longitude, userLoc.latitude))
                    )
                )
                CircleLayer(
                    id = "loc-halo-$uid",
                    source = source,
                    radius = const(18.dp),
                    color = const(dotColor.copy(alpha = 0.15f)),
                    opacity = const(1f)
                )
                CircleLayer(
                    id = "loc-dot-$uid",
                    source = source,
                    radius = const(8.dp),
                    color = const(dotColor),
                    strokeColor = const(Color.White),
                    strokeWidth = const(2.5.dp)
                )
            }
        }

        // 2. Top header gradient scrim + header
        HomeTopBar(
            profilePhotoUrl = profilePhotoUrl,
            onNotifications = { navController.navigate("connectionRequests") },
            onProfile = { navController.navigate("profile") },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 3. Group selector pill (below top bar)
        GroupSelectorPill(
            selectedGroup = selectedGroup,
            userGroups = userGroups,
            groupPickerVisible = groupPickerVisible,
            onTogglePicker = { groupPickerVisible = !groupPickerVisible },
            onSelectGroup = { group ->
                selectedGroup = group
                groupPickerVisible = false
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 68.dp)
        )

        // 4. Member overlay (above bottom nav)
        AnimatedVisibility(
            visible = memberLocations.isNotEmpty(),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 100.dp)
        ) {
            MemberOverlayCard(
                memberLocations = memberLocations,
                currentUid = currentUid,
                selectedMemberUid = selectedMemberUid,
                onMemberTap = { memberLoc ->
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // 5. My location FAB (above member overlay + nav)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(
                    end = 16.dp,
                    bottom = if (memberLocations.isNotEmpty()) 220.dp else 100.dp
                )
        ) {
            MapFabControls(onMyLocation = { refreshLocation(syncToServer = false) })
        }

        // 6. Bottom floating nav bar
        HomeFloatingNavBar(
            onHome = { /* already here */ },
            onGroups = { navController.navigate("groups") },
            onConnections = { navController.navigate("connections") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp, start = 16.dp, end = 16.dp)
        )
    }
}

// ─── HomeTopBar ───────────────────────────────────────────────────────────────
@Composable
private fun HomeTopBar(
    profilePhotoUrl: String,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnread: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        GDarkBg.copy(alpha = 0.85f),
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // GLIMPSE wordmark
        Text(
            text = "GLIMPSE",
            color = GAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        // Right actions
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification bell
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GDarkSurface)
                    .border(1.dp, GBorder, CircleShape)
                    .clickable(role = Role.Button, onClick = onNotifications),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = GTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(GAccent)
                    )
                }
            }
            // Profile avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GAccentSoft)
                    .border(1.dp, GBorder, CircleShape)
                    .clickable(role = Role.Button, onClick = onProfile),
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
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = "Profile",
                        tint = GAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ─── GroupSelectorPill ────────────────────────────────────────────────────────
@Composable
private fun GroupSelectorPill(
    selectedGroup: GlimpseGroup?,
    userGroups: List<GlimpseGroup>,
    groupPickerVisible: Boolean,
    onTogglePicker: () -> Unit,
    onSelectGroup: (GlimpseGroup?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pill chip
        Box(
            modifier = Modifier
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(GDarkSurface)
                .border(1.dp, GBorder, RoundedCornerShape(50))
                .clickable(role = Role.Button, onClick = onTogglePicker)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = selectedGroup?.name ?: "All People",
                    color = GTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (selectedGroup != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GAccentSoft)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${selectedGroup.members.size}",
                            color = GAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = "Select group",
                    tint = GTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Group picker dropdown
        AnimatedVisibility(
            visible = groupPickerVisible,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 }
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(GDarkSurface)
                    .border(1.dp, GBorder, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    item {
                        GroupChip(
                            name = "All People",
                            selected = selectedGroup == null,
                            onClick = { onSelectGroup(null) }
                        )
                    }
                    items(userGroups) { group ->
                        GroupChip(
                            name = group.name,
                            selected = selectedGroup?.id == group.id,
                            onClick = { onSelectGroup(group) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) GAccentSoft else GDarkElevated
    val textColor = if (selected) GAccent else GTextSecondary
    val borderColor = if (selected) GAccent.copy(alpha = 0.4f) else GBorder
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = name,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ─── MemberOverlayCard ────────────────────────────────────────────────────────
@Composable
private fun MemberOverlayCard(
    memberLocations: List<MemberLocation>,
    currentUid: String?,
    selectedMemberUid: String?,
    onMemberTap: (MemberLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(GDarkSurface.copy(alpha = 0.92f))
            .border(1.dp, GBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 12.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            items(memberLocations) { memberLoc ->
                MemberPill(
                    memberLocation = memberLoc,
                    isSelected = memberLoc.member.uid == selectedMemberUid,
                    isSelf = memberLoc.member.uid == currentUid,
                    onClick = { onMemberTap(memberLoc) }
                )
            }
        }
    }
}

@Composable
private fun MemberPill(
    memberLocation: MemberLocation,
    isSelected: Boolean,
    isSelf: Boolean,
    onClick: () -> Unit
) {
    val statusColor = if (isSelf) GAccent else locationStatusColor(memberLocation.location.timestamp)
    val statusLabel = locationStatusLabel(memberLocation.location.timestamp)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            if (memberLocation.member.profilePhotoUrl.isNotBlank()) {
                AsyncImage(
                    model = memberLocation.member.profilePhotoUrl,
                    contentDescription = memberLocation.member.name,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) Modifier.border(2.dp, GAccent, CircleShape)
                            else Modifier
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GAccentSoft)
                        .then(
                            if (isSelected) Modifier.border(2.dp, GAccent, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = memberLocation.member.name
                            .split(" ")
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            .joinToString("")
                            .ifEmpty { "?" },
                        color = GAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            // Status dot
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(GDarkSurface)
                    .padding(1.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = memberLocation.member.name.split(" ").firstOrNull() ?: memberLocation.member.name,
            color = GTextPrimary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                text = statusLabel,
                color = statusColor,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

// ─── HomeFloatingNavBar ───────────────────────────────────────────────────────
@Composable
private fun HomeFloatingNavBar(
    onHome: () -> Unit,
    onGroups: () -> Unit,
    onConnections: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(GDarkSurface)
            .border(1.dp, GBorder, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingNavItem(
                icon = Icons.Rounded.Map,
                label = "Map",
                selected = true,
                onClick = onHome
            )
            FloatingNavItem(
                icon = Icons.Outlined.Groups,
                label = "Groups",
                selected = false,
                onClick = onGroups
            )
            FloatingNavItem(
                icon = Icons.Outlined.People,
                label = "Connections",
                selected = false,
                onClick = onConnections
            )
        }
    }
}

@Composable
private fun FloatingNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) GAccent else GTextSecondary
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

// ─── MapFabControls ───────────────────────────────────────────────────────────
@Composable
private fun MapFabControls(onMyLocation: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(GDarkSurface)
            .border(1.dp, GBorder, CircleShape)
            .clickable(role = Role.Button, onClick = onMyLocation),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.GpsFixed,
            contentDescription = "My location",
            tint = GAccent,
            modifier = Modifier.size(20.dp)
        )
    }
}
