package com.example.glimpse.ui.screens

import android.content.BroadcastReceiver
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.glimpse.firebase.SOSRepository
import coil.compose.AsyncImage
import com.example.glimpse.BuildConfig
import com.example.glimpse.Location.AdaptiveLocationEngine
import com.example.glimpse.Location.AdaptiveLocationTracker
import com.example.glimpse.Location.LocationRepository
import com.example.glimpse.Location.RequestLocationPermission
import com.example.glimpse.firebase.FirebaseRepository
import com.example.glimpse.model.UserLocation
import com.example.glimpse.model.ConnectionRequest
import com.example.glimpse.util.LocationPlaceUtils
import com.example.glimpse.weather.WeatherData
import com.example.glimpse.weather.WeatherRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.glimpse.network.SosBackend

private val GlimpseBlue = Color(0xFF4F46E5)
private val GlimpseBlueLight = Color(0xFF6C7BFF)
private val GlimpseViolet = Color(0xFF8B5CF6)
private val GlimpseBlueSoft = Color(0xFFEDEBFF)
private val GlimpseActionBlueBg = Color(0xFFEEF0FF)
private val GlimpseSheet = Color(0xFFFBFBFF)
private val GlimpseNavTint = Color(0xFFF5F5FC)
private val GlimpseNavy = Color(0xFF111827)
private val GlimpseTextGray = Color(0xFF6B7280)
private val GlimpseHandle = Color(0xFFD9DCE6)
private val GlimpseWhite = Color.White
private val GlimpseGreen = Color(0xFF20B878)
private val GlimpseAmber = Color(0xFFF5A623)
private val GlimpseRed = Color(0xFFFF4D5E)
private val GlimpseRedSoft = Color(0xFFFFEAEE)
private val GlimpseBackground = Color(0xFFF8F9FC)

private val LogoBrush = Brush.linearGradient(listOf(GlimpseBlueLight, GlimpseViolet))

private val ScreenMargin = 16.dp
private val RowSpacing = 10.dp
private val CardGap = 8.dp
private val CardShape = RoundedCornerShape(16.dp)
private val CardBorder = BorderStroke(1.dp, GlimpseHandle.copy(alpha = 0.55f))
private val SheetCorner = 28.dp

private val SheetPeekHeight = 250.dp

private val MapStyleUrl =
    "https://api.maptiler.com/maps/streets-v2/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
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
    val sosRepository = remember { SOSRepository() }

    var sosDialogVisible by remember { mutableStateOf(false) }
    var sosCountdownActive by remember { mutableStateOf(false) }
    var sosCountdown by remember { mutableIntStateOf(3) }
    var sosCreating by remember { mutableStateOf(false) }
    var sosMessage by remember { mutableStateOf<String?>(null) }

    fun beginHomeSos() {
        if (sosDialogVisible || sosCreating) return
        vibrate(context, 80L)
        sosCountdown = 3
        sosMessage = null
        sosCreating = false
        sosDialogVisible = true
        sosCountdownActive = true
    }

    val cameraState = rememberCameraState()

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = false
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

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
    var connections by remember { mutableStateOf(emptyList<ConnectionRequest>()) }
    var placeName by remember { mutableStateOf<String?>(null) }
    var weather by remember { mutableStateOf<WeatherData?>(null) }

    var userName by remember {
        mutableStateOf(
            currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "there"
        )
    }
    var profilePhotoUrl by remember {
        mutableStateOf(currentUser?.photoUrl?.toString().orEmpty())
    }

    val batteryLevel = rememberBatteryLevel()
    val currentBatteryLevel = rememberUpdatedState(batteryLevel)
    val networkStatus = rememberNetworkStatus()

    val myLocation = remember(userLocations, currentUid) {
        userLocations.find { it.uid == currentUid }
    }

    LaunchedEffect(Unit) {
        firebaseRepository.getCurrentUserProfile(
            onResult = { name, photoUrl ->
                if (name.isNotBlank()) userName = name
                if (photoUrl.isNotBlank()) profilePhotoUrl = photoUrl
            },
            onFailure = {
                Log.e("GLIMPSE_PROFILE", "Failed to load profile", it)
            }
        )
    }

    LaunchedEffect(Unit) {
        firebaseRepository.getUsersLocations { locations ->
            userLocations = locations
        }
    }
    LaunchedEffect(currentUid) {
        val uid = currentUid ?: return@LaunchedEffect
        firebaseRepository.getConnections(
            uid = uid,
            onResult = { result -> connections = result },
            onFailure = { error -> Log.e("GLIMPSE_CONNECTIONS", "Failed to load connected people", error) }
        )
    }
    fun refreshLocation(syncToServer: Boolean) {
        locationRepository.getCurrentLocation { location ->
            if (location == null) {
                return@getCurrentLocation
            }

            LocationPlaceUtils.getPlaceName(
                context = context,
                latitude = location.latitude,
                longitude = location.longitude
            ) { result ->
                placeName = result
            }

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

    DisposableEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            adaptiveLocationTracker.start(
                batteryLevelProvider = { currentBatteryLevel.value },
                onLocationReceived = { location ->
                    scope.launch {
                        cameraState.animateTo(
                            CameraPosition(
                                target = Position(location.longitude, location.latitude),
                                zoom = 14.8
                            )
                        )
                    }

                    LocationPlaceUtils.getPlaceName(
                        context = context,
                        latitude = location.latitude,
                        longitude = location.longitude
                    ) { result ->
                        placeName = result
                    }

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

        onDispose {
            adaptiveLocationTracker.stop()
        }
    }
    LaunchedEffect(sosCountdownActive) {
        if (sosCountdownActive) {
            for (seconds in 3 downTo 1) {
                sosCountdown = seconds
                vibrate(context, 45L)
                delay(1_000L)
            }

            if (sosCountdownActive) {
                sosCountdownActive = false
                sosCreating = true
                vibrate(context, 180L)

                sosRepository.createSosevents(
                    onSuccess = { event ->
                        sosCreating = false
                        sosDialogVisible = false
                        vibrate(context, 350L)
                        scope.launch{
                            val result= SosBackend.notifySos(
                                senderUid = currentUser?.uid.orEmpty(),
                                eventId=event.id
                            )
                            result.onFailure {error->
                                Log.e(
                                    "GLIMPSE_HOME_SOS",
                                    "SOS notification request failed",
                                    error
                                )
                            }
                        }
                        navController.navigate("safety/${event.id}") {
                            launchSingleTop = true
                        }
                    },
                    onFailure = { error ->
                        sosCreating = false
                        sosMessage = "Could not create the SOS event: ${error.localizedMessage ?: "Please try again."}"
                        Log.e("GLIMPSE_HOME_SOS", "Failed to create SOS event", error)
                        vibrate(context, 120L)
                    }
                )
            }
        }
    }

    Scaffold(
        containerColor = GlimpseSheet,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlimpseSheet)
                    .navigationBarsPadding()
                    .padding(start = ScreenMargin, end = ScreenMargin, bottom = 8.dp)
            ) {
                HomeBottomNavigation(
                    modifier = Modifier.fillMaxWidth(),
                    onHome = {},
                    onGroups = { navController.navigate("groups") },
                    onEmergencySos = { navController.navigate("safety") },
                    onLongPressEmergencySos = { beginHomeSos() }
                )
            }
        }
    ) { barPadding ->

        BottomSheetScaffold(
            modifier = Modifier.padding(bottom = barPadding.calculateBottomPadding()),
            scaffoldState = scaffoldState,
            sheetPeekHeight = SheetPeekHeight,
            sheetContainerColor = GlimpseSheet,
            sheetShape = RoundedCornerShape(topStart = SheetCorner, topEnd = SheetCorner),
            sheetShadowElevation = 8.dp,
            sheetDragHandle = {
                BottomSheetDefaults.DragHandle(
                    modifier = Modifier.padding(top = 10.dp, bottom = 12.dp),
                    width = 36.dp,
                    height = 4.dp,
                    color = GlimpseHandle
                )
            },
            sheetContent = {
                HomeBottomSheet(
                    userName = userName,
                    placeName = placeName,
                    friends = connections,
                    userLocations = userLocations,
                    onAddFriend = { navController.navigate("addperson") },
                    onViewFriends = { navController.navigate("connections") },
                    onShareLocation = { navController.navigate("connections") },
                    onEmergencySos = { navController.navigate("safety") },
                    onGroups = { navController.navigate("groups") },
                    onLocationSettings = { navController.navigate("profile") }
                )
            }
        ) { mapPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GlimpseBackground)
            ) {

                MaplibreMap(
                    modifier = Modifier.fillMaxSize(),
                    baseStyle = BaseStyle.Uri(MapStyleUrl),
                    cameraState = cameraState
                ) {
                    if (myLocation != null) {
                        val source = rememberGeoJsonSource(
                            data = GeoJsonData.Features(
                                Point(Position(myLocation.longitude, myLocation.latitude))
                            )
                        )
                        CircleLayer(
                            id = "home-my-location-halo",
                            source = source,
                            radius = const(58.dp),
                            color = const(Color(0xFF1976F3)),
                            opacity = const(0.14f)
                        )
                        CircleLayer(
                            id = "home-my-location",
                            source = source,
                            radius = const(9.dp),
                            color = const(Color(0xFF1976F3)),
                            strokeColor = const(Color.White),
                            strokeWidth = const(3.dp)
                        )
                    }
                    connections.filter { it.senderSharing.location }.forEach { friend ->
                        val friendLocation = userLocations.find { it.uid == friend.senderUid }
                        if (friendLocation != null) {
                            val friendSource = rememberGeoJsonSource(
                                data = GeoJsonData.Features(
                                    Point(Position(friendLocation.longitude, friendLocation.latitude))
                                )
                            )
                            CircleLayer(
                                id = "home-friend-${friend.senderUid.hashCode()}",
                                source = friendSource,
                                radius = const(8.dp),
                                color = const(GlimpseViolet),
                                strokeColor = const(Color.White),
                                strokeWidth = const(3.dp)
                            )
                        }
                    }
                }

                if (!locationPermissionGranted) {
                    RequestLocationPermission(
                        onPermissionGranted = {
                            locationPermissionGranted = true
                        }
                    )
                }

                HomeHeader(
                    userName = userName,
                    profilePhotoUrl = profilePhotoUrl,
                    onNotifications = { navController.navigate("connectionRequests") },
                    onProfile = { navController.navigate("profile") }
                )

                AnimatedVisibility(
                    visible = sheetState.targetValue != SheetValue.Expanded &&
                            sheetState.targetValue != SheetValue.Hidden,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp, top = 70.dp)
                ) {
                    MapControls(
                        onMyLocation = { refreshLocation(syncToServer = false) }
                    )
                }

                AnimatedVisibility(
                    visible = sheetState.currentValue == SheetValue.Hidden,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier.clickable(
                            role = Role.Button,
                            onClick = {
                                scope.launch {
                                    sheetState.partialExpand()
                                }
                            }
                        ),
                        shape = RoundedCornerShape(50),
                        color = GlimpseWhite,
                        shadowElevation = 6.dp
                    ) {
                        Text(
                            text = "Show location card  ↑",
                            modifier = Modifier.padding(
                                horizontal = 20.dp,
                                vertical = 12.dp
                            ),
                            color = GlimpseNavy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (sosDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                if (!sosCreating) {
                    sosCountdownActive = false
                    sosDialogVisible = false
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.GppMaybe,
                    contentDescription = null,
                    tint = GlimpseRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = when {
                        sosCreating -> "Creating SOS"
                        sosMessage != null -> "SOS status"
                        else -> "Emergency SOS"
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (sosCountdownActive) {
                        Text("SOS will be triggered in")
                        Text(
                            text = "$sosCountdown",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlimpseRed
                        )
                        Text("Cancel now if this was accidental.")
                    } else if (sosCreating) {
                        CircularProgressIndicator()
                        Text("Creating your SOS event…")
                    } else {
                        Text(sosMessage ?: "SOS countdown cancelled.")
                    }
                }
            },
            confirmButton = {
                if (!sosCountdownActive && !sosCreating) {
                    TextButton(onClick = { sosDialogVisible = false }) {
                        Text("Done")
                    }
                }
            },
            dismissButton = {
                if (sosCountdownActive) {
                    TextButton(
                        onClick = {
                            sosCountdownActive = false
                            sosDialogVisible = false
                            vibrate(context, 100L)
                        }
                    ) {
                        Text("Cancel SOS", color = GlimpseRed)
                    }
                }
            }
        )
    }
}

@Composable
private fun BoxScope.HomeHeader(
    userName: String,
    profilePhotoUrl: String,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    hasUnreadNotifications: Boolean = true
) {
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to GlimpseWhite.copy(alpha = 0.92f),
                    0.55f to GlimpseWhite.copy(alpha = 0.70f),
                    1f to GlimpseWhite.copy(alpha = 0f)
                )
            )
            .statusBarsPadding()
            .padding(
                start = ScreenMargin,
                end = ScreenMargin,
                top = 4.dp,
                bottom = 12.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "GLIMPSE",
                style = TextStyle(
                    brush = LogoBrush,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NotificationButton(
                    hasUnread = hasUnreadNotifications,
                    onClick = onNotifications
                )
                ProfileButton(
                    profilePhotoUrl = profilePhotoUrl,
                    onClick = onProfile
                )
            }
        }

        Text(
            text = userName,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = GlimpseNavy,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "\u201CSame place, new memories.\u201D",
            fontSize = 12.sp,
            color = GlimpseTextGray.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NotificationButton(
    hasUnread: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = GlimpseWhite.copy(alpha = 0.92f),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = GlimpseNavy,
                modifier = Modifier.size(24.dp)
            )

            if (hasUnread) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 9.dp, end = 9.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GlimpseRed)
                )
            }
        }
    }
}

@Composable
private fun ProfileButton(
    profilePhotoUrl: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = GlimpseBlueSoft,
        shadowElevation = 2.dp,
        border = BorderStroke(2.dp, GlimpseWhite)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (profilePhotoUrl.isNotBlank()) {
                AsyncImage(
                    model = profilePhotoUrl,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = GlimpseBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun MapControls(
    onMyLocation: () -> Unit,
    onNavigation: () -> Unit = {},
    onLayers: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = GlimpseWhite.copy(alpha = 0.96f),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapControlButton(
                icon = Icons.Outlined.NearMe,
                contentDescription = "Navigation",
                tint = GlimpseBlue,
                onClick = onNavigation
            )
            MapControlButton(
                icon = Icons.Outlined.Layers,
                contentDescription = "Layers",
                tint = GlimpseNavy,
                onClick = onLayers
            )
            MapControlButton(
                icon = Icons.Outlined.GpsFixed,
                contentDescription = "My location",
                tint = GlimpseNavy,
                onClick = onMyLocation
            )
        }
    }
}

@Composable
private fun MapControlButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun HomeBottomSheet(
    userName: String,
    placeName: String?,
    friends: List<ConnectionRequest>,
    userLocations: List<UserLocation>,
    onAddFriend: () -> Unit,
    onViewFriends: () -> Unit,
    onShareLocation: () -> Unit,
    onEmergencySos: () -> Unit,
    onGroups: () -> Unit,
    onLocationSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Your circle", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GlimpseNavy)
                Text(
                    text = if (friends.isEmpty()) "Build your private circle" else "${friends.size} connected ${if (friends.size == 1) "person" else "people"}",
                    fontSize = 13.sp,
                    color = GlimpseTextGray
                )
            }
            TextButton(onClick = onViewFriends) {
                Text("View all", color = GlimpseBlue, fontWeight = FontWeight.SemiBold)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.width(64.dp).clickable(role = Role.Button, onClick = onAddFriend),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(GlimpseBlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = "Add a friend", tint = GlimpseBlue, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.height(7.dp))
                Text("Add", color = GlimpseNavy, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            friends.take(5).forEach { friend ->
                val location = userLocations.find { it.uid == friend.senderUid }
                val age = location?.timestamp?.let { (System.currentTimeMillis() - it).coerceAtLeast(0L) / 60_000L }
                val status = when {
                    !friend.senderSharing.location -> "Private"
                    location == null -> "Unavailable"
                    age == 0L -> "Just now"
                    age != null && age < 60L -> "${age}m ago"
                    age != null -> "${age / 60L}h ago"
                    else -> "Unavailable"
                }
                Column(modifier = Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = GlimpseBlueSoft,
                            border = BorderStroke(
                                2.dp,
                                when {
                                    !friend.senderSharing.location -> GlimpseHandle
                                    location != null && age != null && age < 5L -> GlimpseGreen
                                    else -> GlimpseAmber
                                }
                            )
                        ) {
                            if (friend.profilePhotoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = friend.profilePhotoUrl,
                                    contentDescription = friend.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = friend.name.firstOrNull()?.uppercase() ?: "?",
                                        color = GlimpseBlue,
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (friend.senderSharing.location && location != null) {
                            Box(
                                modifier = Modifier.align(Alignment.BottomEnd).size(13.dp).clip(CircleShape)
                                    .background(if (age != null && age < 5L) GlimpseGreen else GlimpseAmber)
                                    .padding(2.dp).background(Color.White, CircleShape)
                            )
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = friend.name.ifBlank { "Friend" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlimpseNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = status, fontSize = 10.sp, color = GlimpseTextGray, maxLines = 1)
                }
            }
        }
        ActionCard(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.NearMe,
            title = "Share My Location",
            subtitle = "Manage who can see your location",
            background = GlimpseActionBlueBg,
            iconBrush = Brush.linearGradient(listOf(GlimpseBlue, GlimpseBlueLight)),
            onClick = onShareLocation
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeShortcut(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Groups,
                label = "My Groups",
                subtitle = "Your circles",
                onClick = onGroups
            )
        }
    }
}

@Composable
private fun HomeShortcut(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, GlimpseHandle.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(11.dp)).background(GlimpseBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = GlimpseBlue, modifier = Modifier.size(19.dp))
            }
            Text(
                text = label,
                color = GlimpseNavy,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = GlimpseTextGray,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeBottomNavigation(
    modifier: Modifier,
    onHome: () -> Unit,
    onGroups: () -> Unit,
    onEmergencySos: () -> Unit,
    onLongPressEmergencySos: () -> Unit
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .padding(top = 18.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.98f),
            border = BorderStroke(1.dp, Color(0xFFE8E8F2)),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BottomNavItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Home,
                    label = "Home",
                    selected = true,
                    onClick = onHome
                )
                Spacer(modifier = Modifier.weight(1f))
                BottomNavItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Groups,
                    label = "Groups",
                    selected = false,
                    onClick = onGroups
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(82.dp)
                .combinedClickable(
                    role = Role.Button,
                    onClick = onEmergencySos,
                    onLongClick = onLongPressEmergencySos
                ),
            shape = CircleShape,
            color = Color(0xFFFF515F),
            border = BorderStroke(5.dp, Color.White),
            shadowElevation = 12.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.GppMaybe,
                    contentDescription = "Emergency SOS. Tap to open Safety; press and hold to trigger SOS.",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) GlimpseBlue else GlimpseTextGray

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 28.dp)
                .clip(CircleShape)
                .background(if (selected) GlimpseBlueSoft else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = tint,
            maxLines = 1
        )
    }
}

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

private fun getGreeting(): String {
    return when (
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    ) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..20 -> "Good evening,"
        else -> "Good night,"
    }
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

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    background: Color,
    iconBrush: Brush,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = background
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = GlimpseNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = GlimpseTextGray,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

internal fun vibrate(
    context: Context,
    duration: Long = 100L
) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(
            Context.VIBRATOR_MANAGER_SERVICE
        ) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                duration,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(duration)
    }
}
