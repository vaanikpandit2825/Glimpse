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

@Composable
fun AppNavigation(){
    val navController= rememberNavController()
    val user = FirebaseAuth.getInstance().currentUser

    val startDestination = if(user!=null){
        "home"
    }
    else{
        "signup"
    }
    NavHost(
        navController = navController,
        startDestination = "connectionRequests"
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
            route = "sharingPermissions/{senderUid}"
        ) { backStackEntry ->

            val senderUid =
                backStackEntry.arguments?.getString("senderUid")

            SharingPermissionsScreen(
                navController = navController,
                senderUid = senderUid ?: ""
            )
        }
        composable(
            route = "reviewSharing/{senderUid}"
        ) { backStackEntry ->
            val senderUid =
                backStackEntry.arguments?.getString("senderUid")

            if (senderUid != null) {
                ReviewSharingScreen(
                    navController = navController,
                    senderUid = senderUid
                )
            }
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
    }
}