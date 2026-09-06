package com.example.glimpse.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.glimpse.BuildConfig
import com.example.glimpse.Location.LocationRepository
import com.example.glimpse.Location.RequestLocationPermission
import com.example.glimpse.firebase.FirebaseRepository
import com.example.glimpse.model.UserLocation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

private val GlimpseBlue = Color(0xFF0077BE)
private val GlimpseNavy = Color(0xFF151C24)
private val GlimpseWhite = Color(0xFFFFFFFF)
private val GlimpseSoftBlue = Color(0xFFEAF4FA)
private val GlimpseSoftGray = Color(0xFFF4F5F4)

@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationRepository = remember {
        LocationRepository(context)
    }

    val firebaseRepository = remember {
        FirebaseRepository()
    }

    val cameraState = rememberCameraState()

    var locationPermissionGranted by remember {
        mutableStateOf(false)
    }

    var userLocations by remember {
        mutableStateOf(emptyList<UserLocation>())
    }

    var actionsExpanded by remember {
        mutableStateOf(false)
    }

    val currentUser = FirebaseAuth.getInstance().currentUser

    /*
     * =========================================================
     * LOAD USER LOCATIONS
     * =========================================================
     */

    LaunchedEffect(Unit) {

        firebaseRepository.getUsersLocations { locations ->

            userLocations = locations

            Log.d(
                "GLIMPSE_LOCATION",
                "Loaded ${locations.size} user locations"
            )
        }
    }

    /*
     * =========================================================
     * MOVE TO CURRENT LOCATION
     * =========================================================
     */

    fun moveToCurrentLocation() {

        locationRepository.getCurrentLocation { location ->

            if (location == null) {

                Log.d(
                    "GLIMPSE_LOCATION",
                    "Current location is null"
                )

                return@getCurrentLocation
            }

            scope.launch {

                cameraState.animateTo(
                    CameraPosition(
                        target = Position(
                            location.longitude,
                            location.latitude
                        ),
                        zoom = 16.0
                    )
                )
            }
        }
    }

    /*
     * =========================================================
     * SCREEN
     * =========================================================
     */

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        /*
         * =====================================================
         * MAP
         * =====================================================
         */

        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(
                "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
            ),
            cameraState = cameraState
        )

        /*
         * =====================================================
         * LOCATION PERMISSION
         * =====================================================
         */

        if (!locationPermissionGranted) {

            RequestLocationPermission(
                onPermissionGranted = {

                    locationPermissionGranted = true

                    locationRepository.getCurrentLocation { location ->

                        if (location == null) {

                            Log.d(
                                "GLIMPSE_LOCATION",
                                "Current location is null"
                            )

                            return@getCurrentLocation
                        }

                        Log.d(
                            "GLIMPSE_LOCATION",
                            "Lat: ${location.latitude}, Lng: ${location.longitude}"
                        )

                        scope.launch {

                            cameraState.animateTo(
                                CameraPosition(
                                    target = Position(
                                        location.longitude,
                                        location.latitude
                                    ),
                                    zoom = 16.0
                                )
                            )
                        }

                        val uid = currentUser?.uid

                        if (uid == null) {

                            Log.d(
                                "GLIMPSE_AUTH",
                                "No logged-in user"
                            )

                            return@getCurrentLocation
                        }

                        firebaseRepository.updateLocation(
                            uid = uid,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            onSuccess = {

                                firebaseRepository.getUsersLocations { locations ->

                                    userLocations = locations

                                    Log.d(
                                        "GLIMPSE_LOCATION",
                                        "Updated locations: ${locations.size}"
                                    )
                                }
                            }
                        )
                    }
                }
            )
        }

        /*
         * =====================================================
         * TOP BAR
         * =====================================================
         */

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            /*
             * PROFILE
             */

            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape
                    ),
                shape = CircleShape,
                color = GlimpseWhite.copy(alpha = 0.96f)
            ) {

                IconButton(
                    onClick = {
                        navController.navigate("profile")
                    }
                ) {

                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile",
                        tint = GlimpseNavy,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            /*
             * NOTIFICATIONS
             */

            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape
                    ),
                shape = CircleShape,
                color = GlimpseWhite.copy(alpha = 0.96f)
            ) {

                IconButton(
                    onClick = {
                        // Notifications will be connected later.
                    }
                ) {

                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = "Notifications",
                        tint = GlimpseNavy,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
        }

        /*
         * =====================================================
         * CURRENT LOCATION
         * =====================================================
         */

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 18.dp,
                    bottom = 190.dp
                )
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape
                ),
            shape = CircleShape,
            color = GlimpseWhite.copy(alpha = 0.97f)
        ) {

            IconButton(
                onClick = {
                    moveToCurrentLocation()
                },
                modifier = Modifier.size(50.dp)
            ) {

                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "My location",
                    tint = GlimpseBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        /*
         * =====================================================
         * EXPANDABLE ACTIONS
         * =====================================================
         */

        AnimatedVisibility(
            visible = actionsExpanded,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 18.dp,
                    bottom = 255.dp
                ),
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { 40 }
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { 40 }
            )
        ) {

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                HomeAction(
                    icon = Icons.Rounded.AddLocationAlt,
                    label = "Meet Up",
                    onClick = {
                        // Meet Up will be connected later.
                    }
                )

                HomeAction(
                    icon = Icons.Rounded.Route,
                    label = "Safe Journey",
                    onClick = {
                        // Safe Journey will be connected later.
                    }
                )

                HomeAction(
                    icon = Icons.Rounded.Notifications,
                    label = "Alerts",
                    onClick = {
                        // Alerts will be connected later.
                    }
                )

                HomeAction(
                    icon = Icons.Rounded.LocationOn,
                    label = "Places",
                    onClick = {
                        // Places will be connected later.
                    }
                )
            }
        }

        /*
         * =====================================================
         * ACTION BUTTON
         * =====================================================
         */

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 18.dp,
                    bottom = 125.dp
                )
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape
                ),
            shape = CircleShape,
            color = if (actionsExpanded) {
                GlimpseBlue
            } else {
                GlimpseWhite.copy(alpha = 0.97f)
            }
        ) {

            IconButton(
                onClick = {
                    actionsExpanded = !actionsExpanded
                },
                modifier = Modifier.size(54.dp)
            ) {

                Icon(
                    imageVector = if (actionsExpanded) {
                        Icons.Rounded.MoreHoriz
                    } else {
                        Icons.Rounded.Add
                    },
                    contentDescription = "Actions",
                    tint = if (actionsExpanded) {
                        GlimpseWhite
                    } else {
                        GlimpseBlue
                    },
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        /*
         * =====================================================
         * YOUR CIRCLE
         * =====================================================
         */

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    bottom = 12.dp
                )
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(26.dp)
                ),
            shape = RoundedCornerShape(26.dp),
            color = GlimpseWhite.copy(alpha = 0.97f)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Connections screen will be connected later.
                    }
                    .padding(
                        horizontal = 18.dp,
                        vertical = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                /*
                 * CIRCLE ICON
                 */

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = GlimpseSoftBlue,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = GlimpseBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.size(14.dp)
                )

                /*
                 * TEXT
                 */

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Your circle",
                        color = GlimpseNavy,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = "Connect with people to see them here",
                        color = Color(0xFF707980)
                    )
                }

                /*
                 * ADD PEOPLE
                 */

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GlimpseSoftGray
                ) {

                    Text(
                        text = "Add",
                        color = GlimpseBlue,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            horizontal = 13.dp,
                            vertical = 8.dp
                        )
                    )
                }
            }
        }
    }
}

/*
 * =========================================================
 * HOME ACTION
 * =========================================================
 */

@Composable
private fun HomeAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = GlimpseWhite.copy(alpha = 0.97f),
            modifier = Modifier.shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
        ) {

            Text(
                text = label,
                color = GlimpseNavy,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
            )
        }

        Surface(
            modifier = Modifier
                .size(44.dp)
                .shadow(
                    elevation = 5.dp,
                    shape = CircleShape
                ),
            shape = CircleShape,
            color = GlimpseWhite.copy(alpha = 0.97f)
        ) {

            IconButton(
                onClick = onClick
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = GlimpseBlue,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}