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

@Composable
fun AppNavigation(){
    val navController= rememberNavController()
    val user = FirebaseAuth.getInstance().currentUser

    val startDestination = if(user!=null){
        "connections"
    }
    else{
        "signup"
    }
    NavHost(
        navController = navController,
        startDestination = "connections"
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
        composable("connections"){
            ConnectionsScreen(navController)
        }
    }
}