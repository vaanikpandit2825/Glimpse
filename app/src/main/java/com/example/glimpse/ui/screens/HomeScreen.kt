package com.example.glimpse.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.glimpse.BuildConfig
import com.example.glimpse.R
import com.example.glimpse.model.Friend
import com.example.glimpse.ui.components.BottomSheets.CheckInBottomSheet
import com.example.glimpse.ui.components.FriendsBottomSheet
import com.example.glimpse.ui.components.MyLocationButton
import com.example.glimpse.ui.components.TopControls
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle

@Composable
fun HomeScreen(
    navController: NavController
) {

    var showCheckInSheet by remember {
        mutableStateOf(false)
    }

    Scaffold { innerPadding ->

        val friends = listOf(
            Friend(
                id = "1",
                name = "Aryan",
                avatar = R.drawable.ic_launcher_foreground,
                location = "SRM Library",
                lastSeen = "Just now"
            ),
            Friend(
                id = "2",
                name = "Riya",
                avatar = R.drawable.ic_launcher_foreground,
                location = "Hostel",
                lastSeen = "2 min ago"
            ),
            Friend(
                id = "3",
                name = "Sam",
                avatar = R.drawable.ic_launcher_foreground,
                location = "Main Gate",
                lastSeen = "5 min ago"
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(
                    "https://api.maptiler.com/maps/streets-v2/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
                )
            )

            TopControls(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp)
            )

            MyLocationButton(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
            )

            FriendsBottomSheet(
                friends = friends,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            FloatingActionButton(
                onClick = {
                    showCheckInSheet = true
                },
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Text("CI")
            }

            if (showCheckInSheet) {
                CheckInBottomSheet(
                    onDismiss = {
                        showCheckInSheet = false
                    }
                )
            }
        }
    }
}