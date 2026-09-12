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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
private val GlimpseNavy = Color(0xFF14202B)
private val GlimpseWhite = Color(0xFFFFFFFF)
private val GlimpseSoftBlue = Color(0xFFEAF4FA)
private val GlimpseSoftGray = Color(0xFFF5F7F8)
private val GlimpseBorder = Color(0xFFE5EAED)
private val GlimpseTextGray = Color(0xFF69757D)
private val GlimpseGreen = Color(0xFF20B878)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

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
                Log.d("GLIMPSE_LOCATION", "Current location is null")
                return@getCurrentLocation
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

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 108.dp,
        sheetContainerColor = GlimpseWhite,
        sheetShape = RoundedCornerShape(
            topStart = 30.dp,
            topEnd = 30.dp
        ),
        sheetShadowElevation = 16.dp,
        sheetContent = {
            CircleSheet(
                hasConnections = userLocations.size > 1,
                onAddPeople = {
                    navController.navigate("addperson")
                },
                onOpenConnections = {
                    navController.navigate("connections")
                }
            )
        }
    ) {
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
                        horizontal = 18.dp,
                        vertical = 12.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingHomeButton(
                    icon = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    onClick = {
                        navController.navigate("profile")
                    }
                )

                FloatingHomeButton(
                    icon = Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    onClick = {
                        navController.navigate("connectionRequests")
                    }
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 18.dp,
                        bottom = 124.dp
                    )
                    .shadow(
                        elevation = 10.dp,
                        shape = CircleShape
                    ),
                shape = CircleShape,
                color = GlimpseWhite.copy(alpha = 0.97f)
            ) {
                IconButton(
                    onClick = {
                        moveToCurrentLocation()
                    },
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = "My location",
                        tint = GlimpseBlue,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingHomeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .shadow(
                elevation = 9.dp,
                shape = CircleShape
            ),
        shape = CircleShape,
        color = GlimpseWhite.copy(alpha = 0.96f)
    ) {
        IconButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = GlimpseNavy,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun CircleSheet(
    hasConnections: Boolean,
    onAddPeople: () -> Unit,
    onOpenConnections: () -> Unit
) {
    var selectedTab by remember {
        mutableStateOf(0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = 20.dp,
                end = 20.dp,
                bottom = 16.dp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 10.dp,
                    bottom = 17.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(4.dp)
                    .background(
                        Color(0xFFD0D7DC),
                        RoundedCornerShape(50)
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Your Circle",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlimpseNavy
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = if (hasConnections) {
                        "${if (selectedTab == 0) "People" else if (selectedTab == 1) "Places" else "Groups"} in your circle"
                    } else {
                        "People who matter. Closer."
                    },
                    fontSize = 13.sp,
                    color = GlimpseTextGray
                )
            }

            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clickable(onClick = onAddPeople),
                shape = CircleShape,
                color = GlimpseSoftBlue
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add people",
                        tint = GlimpseBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        CircleTabs(
            selectedTab = selectedTab,
            onTabSelected = {
                selectedTab = it
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        when (selectedTab) {
            0 -> PeopleContent(
                hasConnections = hasConnections,
                onAddPeople = onAddPeople,
                onOpenConnections = onOpenConnections
            )

            1 -> PlacesContent()

            2 -> GroupsContent()
        }
    }
}

@Composable
private fun CircleTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                GlimpseSoftGray,
                RoundedCornerShape(18.dp)
            )
            .padding(4.dp)
    ) {
        CircleTab(
            text = "People",
            selected = selectedTab == 0,
            modifier = Modifier.weight(1f),
            onClick = {
                onTabSelected(0)
            }
        )

        CircleTab(
            text = "Places",
            selected = selectedTab == 1,
            modifier = Modifier.weight(1f),
            onClick = {
                onTabSelected(1)
            }
        )

        CircleTab(
            text = "Groups",
            selected = selectedTab == 2,
            modifier = Modifier.weight(1f),
            onClick = {
                onTabSelected(2)
            }
        )
    }
}

@Composable
private fun CircleTab(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = if (selected) {
            GlimpseWhite
        } else {
            Color.Transparent
        }
    ) {
        Box(
            modifier = Modifier.padding(
                vertical = 10.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Medium
                },
                color = if (selected) {
                    GlimpseBlue
                } else {
                    GlimpseTextGray
                }
            )
        }
    }
}

@Composable
private fun PeopleContent(
    hasConnections: Boolean,
    onAddPeople: () -> Unit,
    onOpenConnections: () -> Unit
) {
    if (!hasConnections) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAddPeople),
            shape = RoundedCornerShape(22.dp),
            color = GlimpseSoftBlue
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = GlimpseWhite
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            tint = GlimpseBlue,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(13.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Add people you trust",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlimpseNavy
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = "Start building your circle",
                        fontSize = 12.sp,
                        color = GlimpseTextGray
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = GlimpseTextGray,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    } else {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenConnections),
            shape = RoundedCornerShape(22.dp),
            color = GlimpseWhite
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        GlimpseSoftGray,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 15.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = GlimpseSoftBlue
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = GlimpseBlue,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(13.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Your people",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlimpseNavy
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    GlimpseGreen,
                                    CircleShape
                                )
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text = "Tap to view your connections",
                            fontSize = 12.sp,
                            color = GlimpseTextGray
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = GlimpseTextGray,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

@Composable
private fun PlacesContent() {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = GlimpseSoftGray
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = GlimpseWhite
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Place,
                        contentDescription = null,
                        tint = GlimpseBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(13.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Saved places",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlimpseNavy
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = "Home, College, Work and more",
                    fontSize = 12.sp,
                    color = GlimpseTextGray
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = GlimpseTextGray,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun GroupsContent() {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = GlimpseSoftGray
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = GlimpseSoftBlue
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Groups,
                        contentDescription = null,
                        tint = GlimpseBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(13.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Create a group",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlimpseNavy
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = "Family, Friends, College, Trips",
                    fontSize = 12.sp,
                    color = GlimpseTextGray
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = GlimpseTextGray,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}