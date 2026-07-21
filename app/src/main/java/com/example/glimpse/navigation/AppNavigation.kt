package com.example.glimpse.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.glimpse.auth.SignupScreen
import com.example.glimpse.auth.LoginScreen
import com.example.glimpse.ui.HomeScreen
import com.google.firebase.auth.FirebaseAuth

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
        startDestination = "signup"
    )
    {
        composable("signup"){
            SignupScreen(navController)
        }
        composable("login"){
            LoginScreen(navController)
        }
        composable("home"){
            HomeScreen()
        }
    }
}