package com.example.glimpse.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import com.example.glimpse.BuildConfig
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.glimpse.ui.components.MyLocationButton
import com.example.glimpse.ui.components.TopControls


@Composable
fun HomeScreen(
    navController:NavController
){
    Scaffold {  innerPadding ->
        Box(
            modifier=Modifier.fillMaxSize()
                .padding(innerPadding)
        ) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(
                    "https://api.maptiler.com/maps/streets-v2/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
                )
            )
            TopControls(
                modifier=Modifier
                    .align(Alignment.TopCenter)
                    .padding(top=12.dp)

            )
            MyLocationButton(
                modifier= Modifier
                    .align(Alignment.Center)
                    .padding(
                        end=20.dp,
                        bottom=100.dp
                    ),
                onClick = {

                }
            )
        }
    }
}