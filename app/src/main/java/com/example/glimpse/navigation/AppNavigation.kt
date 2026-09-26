package com.example.glimpse.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.glimpse.auth.SignupScreen
import com.example.glimpse.auth.LoginScreen
import com.example.glimpse.ui.screens.EditProfileScreen
import com.example.glimpse.ui.screens.HomeScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.glimpse.ui.screens.ProfileScreen
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import com.example.glimpse.model.SharingPermissions
import com.example.glimpse.ui.screens.AddPersonScreen
import com.example.glimpse.ui.screens.GlimpseCodeScreen
import com.example.glimpse.ui.screens.ConnectionRequestScreen
import okhttp3.Connection
import com.example.glimpse.ui.screens.ConnectionsRequestScreen
import com.example.glimpse.ui.screens.ReviewSharingScreen
import com.example.glimpse.ui.screens.SharingPermissionsScreen
import com.example.glimpse.ui.screens.SendConnectionPermissionsScreen
import com.example.glimpse.ui.screens.ConnectionsScreen
import com.example.glimpse.ui.screens.SavedPlacesScreen
import com.example.glimpse.ui.screens.AddPlaceScreen
import com.example.glimpse.ui.screens.ConfirmPlaceScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.glimpse.places.PlaceSearchResult
import com.example.glimpse.ui.screens.PickPlaceOnMapScreen
import com.example.glimpse.ui.screens.PlaceDetailsScreen
import androidx.compose.ui.platform.LocalContext
import com.example.glimpse.Location.LocationRepository
import com.example.glimpse.Location.RequestLocationPermission
import com.example.glimpse.places.SavedPlaceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glimpse.model.SavedPlace
@Composable
fun AppNavigation(){
    val navController= rememberNavController()
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

    RequestLocationPermission(
        onPermissionGranted = {
            locationRepository.getCurrentLocation { location ->
                currentLatitude = location?.latitude
                currentLongtitude = location?.longitude
            }
        }
    )
    var selectedPlace by remember{
        mutableStateOf<PlaceSearchResult?>(null)
    }

    var selectedLatitude by remember {
        mutableStateOf<Double?>(null)
    }

    var selectedLongtitude by remember {
        mutableStateOf<Double?>(null)
    }
    val user = FirebaseAuth.getInstance().currentUser

    val startDestination = if(user!=null){
        "login"
    }
    else{
        "signup"
    }
    NavHost(
        navController = navController,
        startDestination = "savedPlaces"
    )
    {
        composable("signup"){
            SignupScreen(navController)
        }
        composable("login"){
            LoginScreen(navController)
        }
        composable("home"){
            HomeScreen(navController)
        }
        composable("profile"){
            ProfileScreen(navController)
        }
        composable("editProfile"){
            EditProfileScreen(
                navController=navController
            )
        }
        composable ("glimpseCode" ){
            GlimpseCodeScreen(
                navController=navController
            )
        }
        composable("addperson"){
            AddPersonScreen(
                navController=navController
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
                    receiverUid = receiverUid,
                )
            }
        }
        composable("connectionRequests"){
            ConnectionsRequestScreen(
                navController=navController
            )
        }

        composable(
            route = "sharingPermissions/{senderUid}?location={location}&profile={profile}&locationHistory={locationHistory}"
        ) { backStackEntry ->
            val senderUid =
                backStackEntry.arguments?.getString("senderUid") ?: ""

            val location =
                backStackEntry.arguments?.getString("location")?.toBoolean() ?: false

            val profile =
                backStackEntry.arguments?.getString("profile")?.toBoolean() ?: false

            val locationHistory =
                backStackEntry.arguments?.getString("locationHistory")?.toBoolean() ?: false

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
                backStackEntry.arguments?.getString("location")?.toBoolean() ?: false

            val profile =
                backStackEntry.arguments?.getString("profile")?.toBoolean() ?: false

            val locationHistory =
                backStackEntry.arguments?.getString("locationHistory")?.toBoolean() ?: false

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
            route="sendConnectionPermission/{receiverUid}"
        ){
            backStackEntry ->
            val receiverUid=backStackEntry.arguments?.getString("receiverUid")

            if(receiverUid!=null){
                SendConnectionPermissionsScreen(
                    navController=navController,
                    receiverUid=receiverUid
                )
            }
        }
        composable(route= "connections"){
            ConnectionsScreen(navController)
        }

        composable(route = "connections") {
            ConnectionsScreen(navController)
        }

        composable(route = "savedPlaces") {
            SavedPlacesScreen(
                onBack = {
                    navController.popBackStack()
                },
                onAddPlace = {
                    navController.navigate("addPlace")
                }
            )
        }
        composable("addPlace") {
            AddPlaceScreen(
                onBack = {
                    navController.popBackStack()
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
        composable("pickPlaceOnMap"){
            PickPlaceOnMapScreen(
                latitude = currentLatitude,
                longitude = currentLongtitude,
                onBack = {
                    navController.popBackStack()
                },
                onLocationSelected = { latitude, longtitude ->

                    selectedLatitude = latitude
                    selectedLongtitude = longtitude

                    selectedPlace = PlaceSearchResult(
                        id = "picked_${System.currentTimeMillis()}",
                        name = "Selected location",
                        address = "Location picked on map",
                        latitude = latitude,
                        longitude = longtitude
                    )

                    navController.navigate("confirmPlace")
                }
            )
        }
        composable("confirmPlace"){
            val place=selectedPlace

            if(place!=null){
                ConfirmPlaceScreen(
                    place=place,
                    onBack={
                        navController.popBackStack()
                    },
                    onUseLocation = {confirmedPlace->
                        selectedPlace=confirmedPlace
                        navController.navigate("placeDetails")
                    }
                )
            }
        }
        composable("placeDetails") {
            val place = selectedPlace

            if (place != null) {
                PlaceDetailsScreen(
                    place = place,
                    onBack = {
                        navController.popBackStack()
                    },
                    onSave = { name, type, radius ->

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
                                // We'll handle the error UI properly afterward.
                            }
                        )
                    }
                )
            }
        }
    }
}