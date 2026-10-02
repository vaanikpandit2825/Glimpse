package com.example.glimpse.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.glimpse.BuildConfig
import com.example.glimpse.Location.LocationRepository
import com.example.glimpse.Location.RequestLocationPermission
import com.example.glimpse.firebase.FirebaseRepository
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

private val GlimpseBlue = Color(0xFF4F46E5)
private val GlimpseBlueLight = Color(0xFF6C7BFF)
private val GlimpseViolet = Color(0xFF8B5CF6)
private val GlimpseBlueSoft = Color(0xFFEDEBFF)
private val GlimpseActionBlueBg = Color(0xFFEEF0FF)
private val GlimpseRowTint = Color(0xFFF4F4FD)
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

@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val locationRepository = remember {
        LocationRepository(context)
    }

    val firebaseRepository = remember {
        FirebaseRepository()
    }

    val cameraState = rememberCameraState()

    val currentUser = FirebaseAuth.getInstance().currentUser

    var locationPermissionGranted by remember {
        mutableStateOf(false)
    }

    var userLocations by remember {
        mutableStateOf(emptyList<UserLocation>())
    }

    var placeName by remember {
        mutableStateOf<String?>(null)
    }

    var userName by remember {
        mutableStateOf(
            currentUser?.displayName
                ?.takeIf { it.isNotBlank() }
                ?: "there"
        )
    }

    var profilePhotoUrl by remember {
        mutableStateOf(
            currentUser?.photoUrl?.toString() ?: ""
        )
    }

    var batteryLevel by remember {
        mutableStateOf(getBatteryLevel(context))
    }

    var networkStatus by remember {
        mutableStateOf(getNetworkStatus(context))
    }

    LaunchedEffect(Unit) {
        firebaseRepository.getCurrentUserProfile(
            onResult = { name, photoUrl ->
                if (name.isNotBlank()) {
                    userName = name
                }

                if (photoUrl.isNotBlank()) {
                    profilePhotoUrl = photoUrl
                }
            },
            onFailure = {
                Log.e(
                    "GLIMPSE_PROFILE",
                    "Failed to load profile",
                    it
                )
            }
        )
    }

    LaunchedEffect(Unit) {
        firebaseRepository.getUsersLocations { locations ->
            userLocations = locations
        }
    }

    LaunchedEffect(Unit) {
        batteryLevel = getBatteryLevel(context)
        networkStatus = getNetworkStatus(context)
    }

    fun moveToCurrentLocation() {
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
                cameraState.animateTo(
                    CameraPosition(
                        target = Position(
                            location.longitude,
                            location.latitude
                        ),
                        zoom = 15.5
                    )
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlimpseBackground)
    ) {

        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(
                "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
            ),
            cameraState = cameraState
        ) {

            val myLocation = userLocations.find {
                it.uid == currentUser?.uid
            }

            if (myLocation != null) {

                val source = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        Point(
                            Position(
                                myLocation.longitude,
                                myLocation.latitude
                            )
                        )
                    )
                )

                // Soft accuracy halo
                CircleLayer(
                    id = "home-my-location-halo",
                    source = source,
                    radius = const(58.dp),
                    color = const(
                        Color(0xFF1976F3)
                    ),
                    opacity = const(0.14f)
                )

                // Location dot
                CircleLayer(
                    id = "home-my-location",
                    source = source,
                    radius = const(9.dp),
                    color = const(
                        Color(0xFF1976F3)
                    ),
                    strokeColor = const(Color.White),
                    strokeWidth = const(3.dp)
                )
            }
        }

        if (!locationPermissionGranted) {

            RequestLocationPermission(
                onPermissionGranted = {

                    locationPermissionGranted = true

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
                            cameraState.animateTo(
                                CameraPosition(
                                    target = Position(
                                        location.longitude,
                                        location.latitude
                                    ),
                                    zoom = 15.5
                                )
                            )
                        }

                        val uid = currentUser?.uid

                        if (uid == null) {
                            return@getCurrentLocation
                        }

                        firebaseRepository.updateLocation(
                            uid = uid,
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

        HomeHeader(
            userName = userName,
            profilePhotoUrl = profilePhotoUrl,
            onNotifications = {
                navController.navigate("connectionRequests")
            },
            onProfile = {
                navController.navigate("profile")
            }
        )

        MapControls(
            onMyLocation = {
                moveToCurrentLocation()
            }
        )

        HomeBottomSheet(
            placeName = placeName,
            batteryLevel = batteryLevel,
            networkStatus = networkStatus,
            onShareLocation = {
                navController.navigate("connections")
            },
            onEmergencySos = {
                navController.navigate("safety")
            },
            onHome = {},
            onGroups = {
                navController.navigate("groups")
            },
            onSafety = {
                navController.navigate("safety")
            }
        )
    }
}

/* ───────────────────────── HEADER ───────────────────────── */

@Composable
private fun BoxScope.HomeHeader(
    userName: String,
    profilePhotoUrl: String,
    onNotifications: () -> Unit,
    onProfile: () -> Unit
) {
    val greeting = getGreeting()
    val emoji = getGreetingEmoji()

    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlimpseWhite.copy(alpha = 0.94f),
                        GlimpseWhite.copy(alpha = 0.80f),
                        GlimpseWhite.copy(alpha = 0f)
                    )
                )
            )
            .padding(
                top = WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding() + 8.dp,
                start = 24.dp,
                end = 18.dp,
                bottom = 40.dp
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
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GlimpseBlueLight,
                            GlimpseViolet
                        )
                    ),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                NotificationButton(
                    onClick = onNotifications
                )

                ProfileButton(
                    profilePhotoUrl = profilePhotoUrl,
                    onClick = onProfile
                )
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = greeting,
            fontSize = 17.sp,
            color = GlimpseNavy
        )

        Text(
            text = "$userName $emoji",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = GlimpseNavy,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = "\"Same place, new memories.\"",
            fontSize = 14.sp,
            color = GlimpseTextGray
        )
    }
}

@Composable
private fun NotificationButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = GlimpseNavy,
            modifier = Modifier.size(26.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 9.dp, end = 10.dp)
                .size(9.dp)
                .clip(CircleShape)
                .background(GlimpseRed)
        )
    }
}

@Composable
private fun ProfileButton(
    profilePhotoUrl: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(46.dp),
        shape = CircleShape,
        color = GlimpseBlueSoft,
        shadowElevation = 4.dp,
        border = BorderStroke(2.dp, GlimpseWhite)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
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

/* ─────────────────────── MAP CONTROLS ─────────────────────── */

@Composable
private fun BoxScope.MapControls(
    onMyLocation: () -> Unit,
    onNavigation: () -> Unit = {},
    onLayers: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(
                top = WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding() + 128.dp,
                end = 16.dp
            ),
        shape = RoundedCornerShape(50),
        color = GlimpseWhite.copy(alpha = 0.96f),
        shadowElevation = 8.dp
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 2.dp,
                vertical = 4.dp
            ),
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
            .size(46.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
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

/* ───────────────────── BOTTOM SHEET ───────────────────── */

@Composable
private fun BoxScope.HomeBottomSheet(
    placeName: String?,
    batteryLevel: Int,
    networkStatus: String,
    onShareLocation: () -> Unit,
    onEmergencySos: () -> Unit,
    onHome: () -> Unit,
    onGroups: () -> Unit,
    onSafety: () -> Unit
) {
    Surface(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 32.dp
        ),
        color = GlimpseSheet.copy(alpha = 0.97f),
        shadowElevation = 16.dp
    ) {

        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(GlimpseHandle)
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            LocationRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                placeName = placeName
            )

            Spacer(
                modifier = Modifier.height(11.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                StatusCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.WbSunny,
                    iconTint = GlimpseAmber,
                    value = "Fetching Data",
                    label = "Weather"
                )

                StatusCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.BatteryFull,
                    iconTint = GlimpseGreen,
                    value = "$batteryLevel%",
                    label = "Battery"
                )

                StatusCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.SignalCellularAlt,
                    iconTint = if (networkStatus == "Offline") {
                        GlimpseRed
                    } else {
                        GlimpseGreen
                    },
                    value = networkStatus,
                    label = "Network"
                )
            }

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.NearMe,
                    title = "Share My Location",
                    subtitle = "Share live location with contacts",
                    background = GlimpseActionBlueBg,
                    iconBrush = Brush.linearGradient(
                        listOf(GlimpseBlue, GlimpseBlueLight)
                    ),
                    onClick = onShareLocation
                )

                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.GppMaybe,
                    title = "Emergency SOS",
                    subtitle = "Tap and hold to alert contacts",
                    background = GlimpseRedSoft,
                    iconBrush = Brush.linearGradient(
                        listOf(GlimpseRed, Color(0xFFFF7A87))
                    ),
                    onClick = onEmergencySos
                )
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            HomeBottomNavigation(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                onHome = onHome,
                onGroups = onGroups,
                onSafety = onSafety
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
        shape = RoundedCornerShape(20.dp),
        color = GlimpseRowTint
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 11.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = GlimpseBlueLight,
                modifier = Modifier.size(28.dp)
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = placeName ?: "Current location",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlimpseNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (placeName != null) {
                        "Current location"
                    } else {
                        "Finding your location..."
                    },
                    fontSize = 13.sp,
                    color = GlimpseTextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = GlimpseNavy,
                modifier = Modifier.size(24.dp)
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
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = GlimpseWhite,
        shadowElevation = 2.dp
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Column {

                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlimpseNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = GlimpseTextGray,
                    maxLines = 1
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
    subtitle: String,
    background: Color,
    iconBrush: Brush,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = background
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 12.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBrush),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GlimpseWhite,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlimpseNavy,
                    maxLines = 2
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = GlimpseTextGray,
                    maxLines = 2
                )
            }
        }
    }
}

/* ─────────────────── BOTTOM NAVIGATION ─────────────────── */

@Composable
private fun HomeBottomNavigation(
    modifier: Modifier,
    onHome: () -> Unit,
    onGroups: () -> Unit,
    onSafety: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = GlimpseNavTint
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 8.dp
                ),
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
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(
                if (selected) GlimpseBlueSoft.copy(alpha = 0.7f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) {
                FontWeight.Medium
            } else {
                FontWeight.Normal
            },
            color = tint
        )
    }
}

/* ───────────────────────── HELPERS ───────────────────────── */

private fun getGreeting(): String {
    return when (
        java.util.Calendar
            .getInstance()
            .get(java.util.Calendar.HOUR_OF_DAY)
    ) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..20 -> "Good evening,"
        else -> "Good night,"
    }
}

private fun getGreetingEmoji(): String {
    return when (
        java.util.Calendar
            .getInstance()
            .get(java.util.Calendar.HOUR_OF_DAY)
    ) {
        in 5..11 -> "☀️"
        in 12..16 -> "🌤️"
        in 17..20 -> "🌆"
        else -> "🌙"
    }
}

private fun getBatteryLevel(
    context: Context
): Int {
    val batteryManager =
        context.getSystemService(
            Context.BATTERY_SERVICE
        ) as BatteryManager

    return batteryManager
        .getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY
        )
        .coerceIn(0, 100)
}

private fun getNetworkStatus(
    context: Context
): String {
    val connectivityManager =
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    val network =
        connectivityManager.activeNetwork
            ?: return "Offline"

    val capabilities =
        connectivityManager
            .getNetworkCapabilities(network)
            ?: return "Offline"

    return when {
        capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI
        ) -> "Wi-Fi"

        capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR
        ) -> "Mobile"

        else -> "Connected"
    }
}