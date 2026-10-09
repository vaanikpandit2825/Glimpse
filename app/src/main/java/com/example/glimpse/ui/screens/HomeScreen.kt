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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import coil.compose.AsyncImage
import com.example.glimpse.BuildConfig
import com.example.glimpse.Location.AdaptiveLocationEngine
import com.example.glimpse.Location.AdaptiveLocationTracker
import com.example.glimpse.Location.LocationRepository
import com.example.glimpse.Location.RequestLocationPermission
import com.example.glimpse.firebase.FirebaseRepository
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

private val SheetPeekHeight = 260.dp

private val MapStyleUrl =
    "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json"+"?key=${BuildConfig.MAPTILER_API_KEY}"

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
                    onSafety = { navController.navigate("safety") }
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
                    placeName = placeName,
                    batteryLevel = batteryLevel,
                    networkStatus = networkStatus,
                    weather = weather,
                    onShareLocation = { navController.navigate("connections") },
                    onEmergencySos = { navController.navigate("safety") }
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
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = ScreenMargin,
                            bottom = mapPadding.calculateBottomPadding() + 16.dp
                        )
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
}

@Composable
private fun BoxScope.HomeHeader(
    userName: String,
    profilePhotoUrl: String,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    hasUnreadNotifications: Boolean = true
) {
    val greeting = remember { getGreeting() }

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
                top = 8.dp,
                bottom = 20.dp
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

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = greeting,
            fontSize = 14.sp,
            color = GlimpseTextGray
        )

        Text(
            text = userName,
            fontSize = 24.sp,
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
    placeName: String?,
    batteryLevel: Int,
    networkStatus: NetworkStatus,
    weather: WeatherData?,
    onShareLocation: () -> Unit,
    onEmergencySos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenMargin)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(RowSpacing)
    ) {

        LocationRow(
            modifier = Modifier.fillMaxWidth(),
            placeName = placeName
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(CardGap)
        ) {
            StatusCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Rounded.WbSunny,
                iconTint = GlimpseAmber,
                value = weather?.let { "${it.temperature}°C" } ?: "—",
                label = "Weather"
            )
            StatusCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Rounded.BatteryFull,
                iconTint = GlimpseGreen,
                value = "$batteryLevel%",
                label = "Battery"
            )
            StatusCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Rounded.SignalCellularAlt,
                iconTint = if (networkStatus == NetworkStatus.OFFLINE) GlimpseRed else GlimpseGreen,
                value = networkStatus.label,
                label = "Network"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(CardGap)
        ) {
            ActionCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Outlined.NearMe,
                title = "Share My Location",
                background = GlimpseActionBlueBg,
                iconBrush = Brush.linearGradient(listOf(GlimpseBlue, GlimpseBlueLight)),
                onClick = onShareLocation
            )
            ActionCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                icon = Icons.Rounded.GppMaybe,
                title = "Emergency SOS",
                background = GlimpseRedSoft,
                iconBrush = Brush.linearGradient(listOf(GlimpseRed, Color(0xFFFF7A87))),
                onClick = onEmergencySos
            )
        }
    }
}

@Composable
private fun LocationRow(
    modifier: Modifier,
    placeName: String?
) {
    Surface(
        modifier = modifier,
        shape = CardShape,
        color = GlimpseWhite,
        border = CardBorder
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = GlimpseBlueLight,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = placeName ?: "Current location",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlimpseNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (placeName != null) "Current location" else "Finding your location…",
                    fontSize = 12.sp,
                    color = GlimpseTextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = GlimpseTextGray,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun StatusCard(
    modifier: Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String
) {
    Surface(
        modifier = modifier
            .heightIn(min = 60.dp)
            .semantics(mergeDescendants = true) {},
        shape = CardShape,
        color = GlimpseWhite,
        border = CardBorder
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlimpseNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = GlimpseTextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    background: Color,
    iconBrush: Brush,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Surface(
        modifier = modifier
            .heightIn(min = 72.dp)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick),
        shape = shape,
        color = background
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GlimpseWhite,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlimpseNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = GlimpseTextGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeBottomNavigation(
    modifier: Modifier,
    onHome: () -> Unit,
    onGroups: () -> Unit,
    onSafety: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = GlimpseNavTint
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Home,
                label = "Home",
                selected = true,
                onClick = onHome
            )
            BottomNavItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Groups,
                label = "Groups",
                selected = false,
                onClick = onGroups
            )
            BottomNavItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Shield,
                label = "Safety",
                selected = false,
                onClick = onSafety
            )
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


