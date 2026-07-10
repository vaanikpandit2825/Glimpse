package com.example.glimpse.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.glimpse.auth.SignupScreen
import com.example.glimpse.auth.LoginScreen
import com.example.glimpse.ui.HomeScreen

@Composable
fun AppNavigation(){
    val navController= rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "signup"
    )
    {
        composable("signup"){
            SignupScreen()
        }
        composable("login"){
            LoginScreen()
        }
        composable("home"){
            HomeScreen()
        }
    }
}