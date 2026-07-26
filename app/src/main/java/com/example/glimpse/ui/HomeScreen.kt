package com.example.glimpse.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import com.example.glimpse.BuildConfig


@Composable
    fun HomeScreen(
    navController:NavController
){
    Scaffold {  innerPadding ->
        Box(
             modifier=Modifier.fillMaxSize()
                 .padding(innerPadding)
                ) {

            android.util.Log.d("MAP_KEY", BuildConfig.MAPTILER_API_KEY)

            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(
                    "https://api.maptiler.com/maps/streets-v2/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
                )
            )
        }

    }
}