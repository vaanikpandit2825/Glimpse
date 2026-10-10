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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
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
private val GPanelBg     = Color(0xFF0E0E10)
private val GCardBg      = Color(0xFF161618)
private val GRaisedBg    = Color(0xFF1E1E22)

private val GText1       = Color(0xFFF5F4F0)
private val GText2       = Color(0xFF8A8A8E)
private val GText3       = Color(0xFF4A4A50)

private val GViolet      = Color(0xFF6366F1)
private val GVioletSoft  = Color(0xFF1E1F3D)

private val GLive        = Color(0xFF30D158)
private val GRecent      = Color(0xFFFF9F0A)
private val GOffline     = Color(0xFF3A3A3E)

private val GDanger      = Color(0xFFFF375F)

private val GLine        = Color(0xFF2A2A2E)
private val GLineStrong  = Color(0xFF3A3A3E)

private val MapStyleUrl =
    "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

// ─── Data class and helpers ───────────────────────────────────────────────────
private data class MemberLocation(val member: GroupMember, val location: UserLocation)

private fun locationStatusLabel(timestamp: Long): String {
    val age = System.currentTimeMillis() - timestamp
    return when {
        age < 2 * 60_000L  -> "Live"
        age < 60 * 60_000L -> "${age / 60_000}m ago"
        else               -> "Offline"
    }
}

private fun locationStatusColor(timestamp: Long): Color = when {
    System.currentTimeMillis() - timestamp < 2 * 60_000L  -> GLive
    System.currentTimeMillis() - timestamp < 15 * 60_000L -> GRecent
    else                                                   -> GOffline
}

// ─── Battery / Network helpers ────────────────────────────────────────────────
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

// ─── Initials avatar ──────────────────────────────────────────────────────────
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
            .background(GRaisedBg),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = GViolet, fontSize = fontSize, fontWeight = FontWeight.SemiBold)
    }
}

// ─── ConcaveTopShape ──────────────────────────────────────────────────────────
private class ConcaveTopShape(private val archDepth: Dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val depth = with(density) { archDepth.toPx() }
        val path = Path()
        path.moveTo(0f, depth)
        path.cubicTo(
            size.width * 0.25f, 0f,
            size.width * 0.75f, 0f,
            size.width, depth
        )
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()
        return Outline.Generic(path)
    }
}

// ─── CurvedPanel ─────────────────────────────────────────────────────────────
@Composable
private fun CurvedPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = ConcaveTopShape(archDepth = 18.dp)
    Box(
        modifier = modifier
            .shadow(elevation = 20.dp, shape = shape, clip = false)
            .clip(shape)
            .background(GPanelBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)  // offset for arch depth so content starts below the arch
                .navigationBarsPadding()
        ) {
            content()
        }
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

    var userLocations     by remember { mutableStateOf(emptyList<UserLocation>()) }
    var userGroups        by remember { mutableStateOf<List<GlimpseGroup>>(emptyList()) }
    var selectedGroupId   by remember { mutableStateOf<String?>(null) }
    var groupMembers      by remember { mutableStateOf<List<GroupMember>>(emptyList()) }
    var selectedFriendUid by remember { mutableStateOf<String?>(null) }
    var sosDialogVisible  by remember { mutableStateOf(false) }
    var userName          by remember { mutableStateOf("") }
    var profilePhotoUrl   by remember { mutableStateOf("") }
    var placeName         by remember { mutableStateOf<String?>(null) }

    val batteryLevel        = rememberBatteryLevel()
    val currentBatteryLevel = rememberUpdatedState(batteryLevel)
    val networkStatus       = rememberNetworkStatus()

    val selectedGroup = remember(userGroups, selectedGroupId) {
        if (selectedGroupId == null) null
        else userGroups.firstOrNull { it.id == selectedGroupId }
    }

    val myLocation = remember(userLocations, currentUid) {
        userLocations.find { it.uid == currentUid }
    }

    val visibleLocations = remember(userLocations, selectedGroup) {
        if (selectedGroup == null) userLocations
        else {
            val ids = selectedGroup.members.keys
            userLocations.filter { it.uid in ids }
        }
    }

    val memberLocations = remember(groupMembers, visibleLocations) {
        groupMembers.mapNotNull { member ->
            val loc = visibleLocations.firstOrNull { it.uid == member.uid } ?: return@mapNotNull null
            MemberLocation(member, loc)
        }
    }

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

    LaunchedEffect(myLocation) {
        val loc = myLocation ?: return@LaunchedEffect
        cameraState.animateTo(
            CameraPosition(
                target = Position(loc.longitude, loc.latitude),
                zoom   = 14.5
            )
        )
    }

    fun refreshLocation(syncToServer: Boolean) {
        locationRepository.getCurrentLocation { location ->
            if (location == null) return@getCurrentLocation
            LocationPlaceUtils.getPlaceName(
                context   = context,
                latitude  = location.latitude,
                longitude = location.longitude
            ) { result -> placeName = result }
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

    if (!locationPermissionGranted) {
        RequestLocationPermission(
            onPermissionGranted = { locationPermissionGranted = true }
        )
    }

    // ── Root layout ──────────────────────────────────────────────────────────
    Box(Modifier.fillMaxSize().background(GPanelBg)) {

        // ── MAP — full screen, behind the panel ──
        MaplibreMap(
            modifier    = Modifier.fillMaxSize(),
            baseStyle   = BaseStyle.Uri(MapStyleUrl),
            cameraState = cameraState
        ) {
            visibleLocations.forEach { userLoc ->
                val uid       = userLoc.uid
                val isSelf    = uid == currentUid
                val statusColor = when {
                    isSelf -> GViolet
                    System.currentTimeMillis() - userLoc.timestamp < 2 * 60_000L  -> GLive
                    System.currentTimeMillis() - userLoc.timestamp < 15 * 60_000L -> GRecent
                    else   -> GOffline
                }
                val isSelected = uid == selectedFriendUid

                val source = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        Point(Position(userLoc.longitude, userLoc.latitude))
                    )
                )

                if (isSelected) {
                    CircleLayer(
                        id      = "sel-$uid",
                        source  = source,
                        radius  = const(16.dp),
                        color   = const(statusColor.copy(alpha = 0.2f)),
                        opacity = const(1f)
                    )
                }
                CircleLayer(
                    id          = "dot-$uid",
                    source      = source,
                    radius      = const(6.dp),
                    color       = const(statusColor),
                    strokeColor = const(Color(0xFF0E0E10)),
                    strokeWidth = const(2.5.dp)
                )
            }
        }

        // ── CURVED PANEL — bottom 40% ──
        CurvedPanel(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.40f)
        ) {
            // Group filter chips row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    // "All" chip
                    val isAllSelected = selectedGroupId == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isAllSelected) GVioletSoft else GRaisedBg)
                            .border(1.dp, GLine, RoundedCornerShape(6.dp))
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                selectedGroupId = null
                                groupMembers = emptyList()
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "All",
                            color = if (isAllSelected) GViolet else GText2,
                            fontSize = 12.sp,
                            fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
                items(userGroups) { group ->
                    val isGroupSelected = selectedGroupId == group.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isGroupSelected) GVioletSoft else GRaisedBg)
                            .border(1.dp, GLine, RoundedCornerShape(6.dp))
                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                selectedGroupId = group.id
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = group.name,
                            color = if (isGroupSelected) GViolet else GText2,
                            fontSize = 12.sp,
                            fontWeight = if (isGroupSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            // People rail — takes remaining panel height
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
            ) {
                // Self card first
                val selfLoc = visibleLocations.firstOrNull { it.uid == currentUid }
                if (selfLoc != null) {
                    item(key = "self") {
                        val selfMember = GroupMember(
                            uid = currentUid ?: "",
                            name = userName,
                            profilePhotoUrl = profilePhotoUrl
                        )
                        PersonCard(
                            memberLoc = MemberLocation(selfMember, selfLoc),
                            isSelected = selectedFriendUid == currentUid,
                            isSelf = true,
                            onTap = { navController.navigate("connections") },
                            navController = navController,
                            modifier = Modifier.fillParentMaxHeight()
                        )
                    }
                }

                // Friends
                val filteredMembers = memberLocations.filter { it.member.uid != currentUid }
                items(filteredMembers, key = { it.member.uid }) { ml ->
                    PersonCard(
                        memberLoc = ml,
                        isSelected = selectedFriendUid == ml.member.uid,
                        isSelf = false,
                        onTap = {
                            selectedFriendUid = ml.member.uid
                            scope.launch {
                                cameraState.animateTo(
                                    CameraPosition(
                                        target = Position(ml.location.longitude, ml.location.latitude),
                                        zoom   = 15.0
                                    )
                                )
                            }
                        },
                        navController = navController,
                        modifier = Modifier.fillParentMaxHeight()
                    )
                }

                // Empty state
                if (selfLoc == null && filteredMembers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .width(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No one here yet", color = GText3, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // ── GPS FAB — above the panel ──
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 14.dp, bottom = 250.dp),
            shape = CircleShape,
            color = GPanelBg,
            border = BorderStroke(1.dp, GLine),
            shadowElevation = 3.dp
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                        refreshLocation(syncToServer = false)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.GpsFixed,
                    contentDescription = "Recenter",
                    tint               = GViolet,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }

        // ── TOP CONTROLS — float over map ──
        // Filter chip — top left
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 14.dp, top = 10.dp)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { /* filter tap: no-op, inline chip row handles it */ },
            shape = RoundedCornerShape(8.dp),
            color = GPanelBg,
            border = BorderStroke(1.dp, GLine),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier.size(6.dp).clip(CircleShape).background(GViolet)
                )
                Text(
                    text = selectedGroup?.name ?: "Everyone",
                    color = GText1,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GVioletSoft)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${visibleLocations.size}",
                        color = GViolet,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Action buttons — top right
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 14.dp, top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SOS
            Surface(
                shape = CircleShape,
                color = GPanelBg,
                border = BorderStroke(1.dp, GLine),
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            sosDialogVisible = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Warning,
                        contentDescription = "SOS",
                        tint               = GDanger,
                        modifier           = Modifier.size(17.dp)
                    )
                }
            }
            // Notifications
            Surface(
                shape = CircleShape,
                color = GPanelBg,
                border = BorderStroke(1.dp, GLine),
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            navController.navigate("connectionRequests")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint               = GText1,
                        modifier           = Modifier.size(17.dp)
                    )
                    // Indicator dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(GViolet)
                    )
                }
            }
            // Profile
            Surface(
                shape = CircleShape,
                color = GPanelBg,
                border = BorderStroke(1.dp, GLine),
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            navController.navigate("profile")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model              = profilePhotoUrl,
                            contentDescription = "Profile",
                            modifier           = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale       = ContentScale.Crop
                        )
                    } else {
                        InitialsAvatar(name = userName.ifBlank { "?" }, size = 36.dp, fontSize = 13.sp)
                    }
                }
            }
        }

        // ── FRIEND DETAIL SHEET ──
        if (selectedFriendLocation != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedFriendUid = null },
                sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor   = GPanelBg,
                shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 28.dp, height = 3.dp)
                                .background(GLineStrong, RoundedCornerShape(2.dp))
                        )
                    }
                }
            ) {
                FriendDetailSheetContent(
                    memberLoc     = selectedFriendLocation,
                    onClose       = { selectedFriendUid = null },
                    navController = navController
                )
            }
        }

        // ── SOS DIALOG ──
        if (sosDialogVisible) {
            AlertDialog(
                onDismissRequest = { sosDialogVisible = false },
                containerColor   = GPanelBg,
                shape            = RoundedCornerShape(20.dp),
                icon = {
                    Icon(
                        imageVector        = Icons.Rounded.Warning,
                        tint               = GDanger,
                        contentDescription = null,
                        modifier           = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text("Emergency SOS", color = GText1, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                text = {
                    Text(
                        "Share your location with your connections immediately?",
                        color = GText2,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { sosDialogVisible = false; navController.navigate("connections") },
                        colors  = ButtonDefaults.buttonColors(containerColor = GDanger),
                        shape   = RoundedCornerShape(10.dp)
                    ) {
                        Text("Send SOS", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { sosDialogVisible = false }) {
                        Text("Cancel", color = GText2)
                    }
                }
            )
        }
    }
}

// ─── PersonCard ───────────────────────────────────────────────────────────────
@Composable
private fun PersonCard(
    memberLoc: MemberLocation,
    isSelected: Boolean,
    isSelf: Boolean,
    onTap: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isSelf) GViolet
        else locationStatusColor(memberLoc.location.timestamp)
    val statusLabel = if (isSelf) "You"
        else locationStatusLabel(memberLoc.location.timestamp)

    Surface(
        modifier = modifier
            .width(140.dp)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onTap() },
        shape = RoundedCornerShape(16.dp),
        color = GCardBg,
        border = if (isSelected) BorderStroke(1.dp, GViolet.copy(alpha = 0.6f))
                 else BorderStroke(1.dp, GLine),
        shadowElevation = if (isSelected) 8.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: avatar + name + status
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Avatar with status dot overlay
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (isSelected) statusColor else GLine, CircleShape)
                    ) {
                        if (memberLoc.member.profilePhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model              = memberLoc.member.profilePhotoUrl,
                                contentDescription = null,
                                modifier           = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale       = ContentScale.Crop
                            )
                        } else {
                            InitialsAvatar(
                                name     = memberLoc.member.name,
                                size     = 52.dp,
                                fontSize = 16.sp
                            )
                        }
                    }
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .offset(x = 2.dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(GPanelBg)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }

                // Name
                Text(
                    text       = memberLoc.member.name.ifBlank { "Unknown" },
                    color      = GText1,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                // Status
                Text(
                    text       = statusLabel,
                    color      = statusColor,
                    fontSize   = 11.sp,
                    maxLines   = 1
                )
            }

            // Bottom: self gets a Share button, others get nothing
            if (isSelf) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(7.dp))
                        .background(GVioletSoft)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            navController.navigate("connections")
                        }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Share", color = GViolet, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── FriendDetailSheetContent ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendDetailSheetContent(
    memberLoc: MemberLocation,
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
            .padding(horizontal = 22.dp, vertical = 0.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header row: avatar + name/status + close
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth()
        ) {
            // 64dp avatar
            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                if (memberLoc.member.profilePhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model              = memberLoc.member.profilePhotoUrl,
                        contentDescription = memberLoc.member.name,
                        modifier           = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, statusColor, CircleShape),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, statusColor, CircleShape)
                    ) {
                        InitialsAvatar(
                            name     = memberLoc.member.name,
                            size     = 60.dp,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = memberLoc.member.name,
                    color      = GText1,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
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
                        color    = GText2,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Close button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(GRaisedBg)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint               = GText2,
                    modifier           = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(GLineStrong.copy(alpha = 0.5f)))
        Spacer(Modifier.height(16.dp))

        // Location freshness row
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint               = GText3,
                modifier           = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text     = locationStatusLabel(memberLoc.location.timestamp),
                color    = GText2,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (isLive) GLive.copy(alpha = 0.12f) else GRaisedBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text       = freshnessLabel,
                    color      = if (isLive) GLive else GText2,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Action buttons row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailActionButton(
                label   = "Message",
                icon    = Icons.Outlined.NearMe,
                tint    = GViolet,
                onClick = { navController.navigate("connections"); onClose() },
                modifier = Modifier.weight(1f)
            )
            DetailActionButton(
                label   = "Directions",
                icon    = Icons.Rounded.Directions,
                tint    = GText2,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            DetailActionButton(
                label   = "Profile",
                icon    = Icons.Rounded.Person,
                tint    = GText2,
                onClick = { navController.navigate("profile"); onClose() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─── DetailActionButton ───────────────────────────────────────────────────────
@Composable
private fun DetailActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier,
        shape    = RoundedCornerShape(10.dp),
        border   = BorderStroke(1.dp, GLine),
        colors   = ButtonDefaults.outlinedButtonColors(containerColor = GRaisedBg)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = tint,
                modifier           = Modifier.size(18.dp)
            )
            Text(label, fontSize = 11.sp, color = GText1)
        }
    }
}
