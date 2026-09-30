package com.example.glimpse.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.glimpse.Location.LocationRepository
import com.example.glimpse.Location.RequestLocationPermission
import com.example.glimpse.auth.LoginScreen
import com.example.glimpse.auth.SignupScreen
import com.example.glimpse.groups.GroupsViewModel
import com.example.glimpse.model.SavedPlace
import com.example.glimpse.model.SharingPermissions
import com.example.glimpse.places.PlaceSearchResult
import com.example.glimpse.places.SavedPlaceViewModel
import com.example.glimpse.ui.screens.AddPeopleToGroupScreen
import com.example.glimpse.ui.screens.AddPersonScreen
import com.example.glimpse.ui.screens.AddPlaceScreen
import com.example.glimpse.ui.screens.ConfirmPlaceScreen
import com.example.glimpse.ui.screens.ConnectionRequestScreen
import com.example.glimpse.ui.screens.ConnectionsRequestScreen
import com.example.glimpse.ui.screens.ConnectionsScreen
import com.example.glimpse.ui.screens.CreateGroupScreen
import com.example.glimpse.ui.screens.EditProfileScreen
import com.example.glimpse.ui.screens.GlimpseCodeScreen
import com.example.glimpse.ui.screens.GroupDetailScreen
import com.example.glimpse.ui.screens.GroupMapScreen
import com.example.glimpse.ui.screens.GroupsScreen
import com.example.glimpse.ui.screens.HomeScreen
import com.example.glimpse.ui.screens.PickPlaceOnMapScreen
import com.example.glimpse.ui.screens.PlaceDetailsScreen
import com.example.glimpse.ui.screens.ProfileScreen
import com.example.glimpse.ui.screens.ReviewSharingScreen
import com.example.glimpse.ui.screens.SavedPlacesScreen
import com.example.glimpse.ui.screens.SendConnectionPermissionsScreen
import com.example.glimpse.ui.screens.SharingPermissionsScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current

    var currentLatitude by remember {
        mutableStateOf<Double?>(null)
    }

    var currentLongtitude by remember {
        mutableStateOf<Double?>(null)
    }

    val locationRepository = remember {
        LocationRepository(context)
    }

    val savedPlaceViewModel: SavedPlaceViewModel = viewModel()
    val groupsViewModel: GroupsViewModel = viewModel()

    RequestLocationPermission(
        onPermissionGranted = {
            locationRepository.getCurrentLocation { location ->
                currentLatitude = location?.latitude
                currentLongtitude = location?.longitude
            }
        }
    )

    var selectedPlace by remember {
        mutableStateOf<PlaceSearchResult?>(null)
    }

    var selectedLatitude by remember {
        mutableStateOf<Double?>(null)
    }

    var selectedLongtitude by remember {
        mutableStateOf<Double?>(null)
    }

    var editingPlace by remember {
        mutableStateOf<SavedPlace?>(null)
    }

    val user = FirebaseAuth.getInstance().currentUser

    NavHost(
        navController = navController,
        startDestination = if (user != null) "home" else "login"
    ) {


        composable("signup") {
            SignupScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }


        composable("profile") {
            ProfileScreen(navController)
        }

        composable("editProfile") {
            EditProfileScreen(
                navController = navController
            )
        }


        composable("glimpseCode") {
            GlimpseCodeScreen(
                navController = navController
            )
        }


        composable("addperson") {
            AddPersonScreen(
                navController = navController
            )
        }

        composable(
            route = "connectionRequest/{receiverUid}"
        ) { backStackEntry ->

            val receiverUid =
                backStackEntry.arguments?.getString("receiverUid")

            if (receiverUid != null) {
                ConnectionRequestScreen(
                    navController = navController,
                    receiverUid = receiverUid
                )
            }
        }

        composable("connectionRequests") {
            ConnectionsRequestScreen(
                navController = navController
            )
        }

        composable(
            route = "sharingPermissions/{senderUid}?location={location}&profile={profile}&locationHistory={locationHistory}"
        ) { backStackEntry ->

            val senderUid =
                backStackEntry.arguments?.getString("senderUid") ?: ""

            val location =
                backStackEntry.arguments
                    ?.getString("location")
                    ?.toBoolean() ?: false

            val profile =
                backStackEntry.arguments
                    ?.getString("profile")
                    ?.toBoolean() ?: false

            val locationHistory =
                backStackEntry.arguments
                    ?.getString("locationHistory")
                    ?.toBoolean() ?: false

            SharingPermissionsScreen(
                navController = navController,
                senderUid = senderUid,
                senderSharing = SharingPermissions(
                    location = location,
                    profile = profile,
                    locationHistory = locationHistory
                )
            )
        }

        composable(
            route = "reviewSharing/{senderUid}?location={location}&profile={profile}&locationHistory={locationHistory}"
        ) { backStackEntry ->

            val senderUid =
                backStackEntry.arguments?.getString("senderUid") ?: ""

            val location =
                backStackEntry.arguments
                    ?.getString("location")
                    ?.toBoolean() ?: false

            val profile =
                backStackEntry.arguments
                    ?.getString("profile")
                    ?.toBoolean() ?: false

            val locationHistory =
                backStackEntry.arguments
                    ?.getString("locationHistory")
                    ?.toBoolean() ?: false

            ReviewSharingScreen(
                navController = navController,
                senderUid = senderUid,
                senderSharing = SharingPermissions(
                    location = location,
                    profile = profile,
                    locationHistory = locationHistory
                )
            )
        }

        composable(
            route = "sendConnectionPermission/{receiverUid}"
        ) { backStackEntry ->

            val receiverUid =
                backStackEntry.arguments?.getString("receiverUid")

            if (receiverUid != null) {
                SendConnectionPermissionsScreen(
                    navController = navController,
                    receiverUid = receiverUid
                )
            }
        }

        composable("connections") {
            ConnectionsScreen(navController)
        }

        composable("savedPlaces") {

            SavedPlacesScreen(
                onBack = {
                    navController.popBackStack()
                },

                onAddPlace = {
                    editingPlace = null
                    navController.navigate("addPlace")
                },

                onEditPlace = { place ->

                    editingPlace = place

                    selectedPlace = PlaceSearchResult(
                        id = place.id,
                        name = place.name,
                        address = "Saved place",
                        latitude = place.latitude,
                        longitude = place.longtitude
                    )

                    navController.navigate("placeDetails")
                }
            )
        }

        composable("addPlace") {

            AddPlaceScreen(

                onBack = {
                    navController.popBackStack()
                },

                onUseCurrentLocation = {

                    if (
                        currentLatitude != null &&
                        currentLongtitude != null
                    ) {

                        selectedPlace = PlaceSearchResult(
                            id = "current_${System.currentTimeMillis()}",
                            name = "Current location",
                            address = "Your current location",
                            latitude = currentLatitude!!,
                            longitude = currentLongtitude!!
                        )

                        navController.navigate("confirmPlace")
                    }
                },

                onPickOnMap = {
                    navController.navigate("pickPlaceOnMap")
                },

                onPlaceSelected = { place ->

                    selectedPlace = place

                    navController.navigate("confirmPlace")
                }
            )
        }


        composable("pickPlaceOnMap") {

            PickPlaceOnMapScreen(

                latitude =
                    selectedPlace?.latitude
                        ?: currentLatitude,

                longitude =
                    selectedPlace?.longitude
                        ?: currentLongtitude,

                onBack = {
                    navController.popBackStack()
                },

                onLocationSelected = { latitude, longtitude ->

                    selectedLatitude = latitude
                    selectedLongtitude = longtitude

                    val currentPlace = selectedPlace

                    selectedPlace = PlaceSearchResult(
                        id = currentPlace?.id
                            ?: "picked_${System.currentTimeMillis()}",

                        name = currentPlace?.name
                            ?: "Selected location",

                        address = currentPlace?.address
                            ?: "Location picked on map",

                        latitude = latitude,
                        longitude = longtitude
                    )

                    navController.navigate("confirmPlace")
                }
            )
        }


        composable("confirmPlace") {

            val place = selectedPlace

            if (place != null) {

                ConfirmPlaceScreen(

                    place = place,

                    onBack = {
                        navController.popBackStack()
                    },

                    onUseLocation = { confirmedPlace ->

                        selectedPlace = confirmedPlace

                        navController.navigate("placeDetails")
                    },

                    onAdjustPin = {
                        navController.navigate("pickPlaceOnMap")
                    }
                )
            }
        }


        composable("placeDetails") {

            val place = selectedPlace
            val existingPlace = editingPlace

            if (place != null) {

                PlaceDetailsScreen(

                    place = place,

                    existingPlace = existingPlace,

                    onBack = {
                        navController.popBackStack()
                    },

                    onSave = { name, type, radius ->

                        if (existingPlace != null) {

                            val updatedPlace = existingPlace.copy(
                                name = name,
                                type = type,
                                radius = radius
                            )

                            savedPlaceViewModel.updateSavedPlace(
                                place = updatedPlace,

                                onSuccess = {

                                    editingPlace = null

                                    navController.navigate("savedPlaces") {
                                        popUpTo("placeDetails") {
                                            inclusive = true
                                        }
                                    }
                                },

                                onFailure = {
                                }
                            )

                        } else {

                            val savedPlace = SavedPlace(
                                id = java.util.UUID.randomUUID().toString(),
                                name = name,
                                type = type,
                                latitude = place.latitude,
                                longtitude = place.longitude,
                                radius = radius,
                                createdAt = System.currentTimeMillis()
                            )

                            savedPlaceViewModel.savePlace(
                                place = savedPlace,

                                onSuccess = {

                                    navController.navigate("savedPlaces") {
                                        popUpTo("addPlace") {
                                            inclusive = true
                                        }
                                    }
                                },

                                onFailure = {
                                }
                            )
                        }
                    }
                )
            }
        }

        composable("groups") {

            GroupsScreen(
                onBack = {
                    navController.popBackStack()
                },

                onCreateGroup = {
                    navController.navigate("createGroup")
                },

                onGroupClick = { group ->
                    navController.navigate(
                        "groupDetail/${group.id}"
                    )
                },

                groupsViewModel = groupsViewModel
            )
        }

        composable("groupDetail/{groupId}") { backStackEntry ->

            val groupId =
                backStackEntry.arguments?.getString("groupId")
                    ?: return@composable

            GroupDetailScreen(
                groupId = groupId,

                onBack = {
                    navController.popBackStack()
                },

                onAddPeople = {
                    navController.navigate(
                        "addPeopleToGroup/$groupId"
                    )
                },

                onViewMap = {
                    navController.navigate(
                        "groupMap/$groupId"
                    )
                }
            )
        }



        composable("groupMap/{groupId}") { backStackEntry ->

            val groupId =
                backStackEntry.arguments?.getString("groupId")
                    ?: return@composable

            GroupMapScreen(
                groupId = groupId,

                onBack = {
                    navController.popBackStack()
                }
            )
        }


        composable("createGroup") {

            CreateGroupScreen(

                onBack = {
                    navController.popBackStack()
                },

                onGroupCreated = {

                    navController.navigate("groups") {

                        popUpTo("createGroup") {
                            inclusive = true
                        }
                    }
                },

                groupsViewModel = groupsViewModel
            )
        }

        composable(
            "addPeopleToGroup/{groupId}"
        ) { backStackEntry ->

            val groupId =
                backStackEntry.arguments?.getString("groupId")
                    ?: return@composable

            AddPeopleToGroupScreen(

                groupId = groupId,

                onBack = {
                    navController.popBackStack()
                },

                onPeopleAdded = {

                    navController.navigate(
                        "groupDetail/$groupId"
                    ) {
                        popUpTo(
                            "addPeopleToGroup/{groupId}"
                        ) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}