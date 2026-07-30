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
import com.example.glimpse.ui.components.FriendRow
import com.example.glimpse.ui.components.FriendsBottomSheet
import com.example.glimpse.ui.components.MyLocationButton
import com.example.glimpse.ui.components.TopControls
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.example.glimpse.model.Friend
import com.example.glimpse.R
import com.example.glimpse.ui.components.FriendsBottomSheet
import com.example.glimpse.ui.components.MyLocationButton
import com.example.glimpse.ui.components.TopControls


@Composable
fun HomeScreen(
    navController:NavController
){
    Scaffold {  innerPadding ->
        val friends = listOf(
            Friend(
                id = "1",
                name = "Aryan",
                avatar = R.drawable.ic_launcher_foreground, // Temporary
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
            FriendsBottomSheet(
                friends=friends,
                modifier=Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}