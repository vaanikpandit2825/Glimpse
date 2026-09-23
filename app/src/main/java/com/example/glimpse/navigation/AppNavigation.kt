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
import com.example.glimpse.ui.screens.PlaceDetailsScreen

@Composable
fun AppNavigation(){
    val navController= rememberNavController()
    var selectedPlace by remember{
        mutableStateOf<PlaceSearchResult?>(null)
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
        composable(route="addPlace"){
            AddPlaceScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPlaceSelected = { place->
                    selectedPlace=place
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
                    onUseLocation = {
                        navController.navigate("placeDetails")
                    }
                )
            }
        }
        composable("placeDetails"){
            val place=selectedPlace
            if(place!=null){
                PlaceDetailsScreen(
                    place=place,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}