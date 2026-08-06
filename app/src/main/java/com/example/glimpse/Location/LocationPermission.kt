package com.example.glimpse.Location

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import android.health.connect.datatypes.ExerciseRoute.Location

@Composable
fun RequestLocationPermission(
    onPermissionGranted:()-> Unit
){
    var permissionGranted by remember { mutableStateOf(false) }

    val launcher= rememberLauncherForActivityResult(
        contract= ActivityResultContracts.RequestPermission()
    ) { granted->
        permissionGranted=granted

        if(granted){
            onPermissionGranted()
        }
    }
    LaunchedEffect(Unit) {
        launcher.launch(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}