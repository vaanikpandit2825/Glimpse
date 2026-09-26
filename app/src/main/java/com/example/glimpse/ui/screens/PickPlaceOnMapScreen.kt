package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimpse.BuildConfig
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material3.Button
import androidx.compose.material3.Text

private val GlimpseNavy = Color(0xFF14202B)
private val GlimpseBlue = Color(0xFF0077BE)

@Composable
fun PickPlaceOnMapScreen(
    latitude: Double?,
    longitude: Double?,
    onBack: () -> Unit = {},
    onLocationSelected: (latitude: Double, longtitude: Double) -> Unit = { _, _ -> }
) {
    val cameraState = rememberCameraState()
    var selectedLatitude by remember{
        mutableStateOf(latitude)
    }
    var selectedLongtitude by remember{
        mutableStateOf(longitude)
    }
    LaunchedEffect(cameraState) {
        snapshotFlow { cameraState.position }
            .collect { position ->
                selectedLatitude = position.target.latitude
                selectedLongtitude = position.target.longitude
            }
    }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            cameraState.animateTo(
                CameraPosition(
                    target = Position(
                        longitude = longitude,
                        latitude = latitude
                    ),
                    zoom = 15.0
                )
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        if (latitude != null && longitude != null) {

            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(
                    "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
                ),
                cameraState = cameraState
            )

        } else {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAFBFD)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Getting your location...",
                    fontSize = 14.sp,
                    color = GlimpseNavy
                )
            }
        }

        Surface(
            modifier = Modifier
                .padding(
                    top = 18.dp,
                    start = 16.dp,
                    end = 16.dp
                )
                .size(48.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 5.dp
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = GlimpseNavy
                )
            }
        }

        if (latitude != null && longitude != null) {

            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Selected location",
                        tint = GlimpseBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                ),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = {
                    if (selectedLatitude != null && selectedLongtitude != null) {
                        onLocationSelected(
                            selectedLatitude!!,
                            selectedLongtitude!!
                        )
                    }
                },
                enabled = selectedLatitude != null && selectedLongtitude != null,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Use this location"
                )
            }
        }
    }
}