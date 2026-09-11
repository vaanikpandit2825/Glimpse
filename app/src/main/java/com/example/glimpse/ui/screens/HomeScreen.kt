package com.example.glimpse.ui.screens

import android.util.Log
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
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
private val GlimpseTextGray = Color(0xFF707980)

@OptIn(ExperimentalMaterial3Api::class)
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

    val currentUser = FirebaseAuth.getInstance().currentUser

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    LaunchedEffect(Unit) {
        firebaseRepository.getUsersLocations { locations ->
            userLocations = locations
            Log.d(
                "GLIMPSE_LOCATION",
                "Loaded ${locations.size} user locations"
            )
        }
    }

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

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 112.dp,
        sheetContainerColor = GlimpseWhite,
        sheetShape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
        sheetShadowElevation = 12.dp,
        sheetContent = {
            CircleSheet(
                hasConnections = userLocations.size > 1,
                onAddPeople = {
                    navController.navigate("find_people")
                },
                onOpenConnections = {
                    navController.navigate("connections")
                }
            )
        }
    ) { _ ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(
                    "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
                ),
                cameraState = cameraState
            )

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
                            navController.navigate("connectionRequests")
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

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 18.dp,
                        bottom = 135.dp
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
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = "My location",
                        tint = GlimpseBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleSheet(
    hasConnections: Boolean,
    onAddPeople: () -> Unit,
    onOpenConnections: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = 18.dp,
                end = 18.dp,
                bottom = 12.dp
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 10.dp,
                    bottom = 12.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = 42.dp,
                        height = 4.dp
                    )
                    .background(
                        color = Color(0xFFD5D9DB),
                        shape = RoundedCornerShape(50)
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onOpenConnections()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Your Circle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GlimpseNavy
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = if (hasConnections) {
                        "People in your circle"
                    } else {
                        "See your people on the map"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = GlimpseTextGray
                )
            }

            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        onAddPeople()
                    },
                shape = CircleShape,
                color = GlimpseSoftBlue
            ) {
                IconButton(
                    onClick = onAddPeople
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add people",
                        tint = GlimpseBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        if (!hasConnections) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onAddPeople()
                    },
                shape = RoundedCornerShape(20.dp),
                color = GlimpseSoftGray
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 15.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = GlimpseWhite,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = GlimpseBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.size(12.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Find people",
                            color = GlimpseNavy,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text = "Add friends and family to your circle",
                            color = GlimpseTextGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = GlimpseBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

        } else {

            Text(
                text = "People nearby",
                color = GlimpseTextGray,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(
                    start = 4.dp,
                    bottom = 10.dp
                )
            )

            CirclePersonPlaceholder(
                name = "Connected person",
                distance = "Location available"
            )
        }
    }
}

@Composable
private fun CirclePersonPlaceholder(
    name: String,
    distance: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(46.dp)
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
                modifier = Modifier.size(23.dp)
            )
        }

        Spacer(
            modifier = Modifier.size(12.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                color = GlimpseNavy,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = distance,
                color = GlimpseTextGray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}