package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.glimpse.firebase.FirebaseRepository
import androidx.navigation.NavController
import android.util.Log

@Composable
fun ProfileScreen(
    navController: NavController
){

    var userName by remember { mutableStateOf("") }
    var profilePhotoUrl by remember { mutableStateOf("") }
    val firebaseRepository = remember { FirebaseRepository() }

    LaunchedEffect(Unit) {
        firebaseRepository.getCurrentUserProfile(
            onResult = { name, photoUrl ->
                userName = name
                profilePhotoUrl = photoUrl

                Log.d(
                    "PROFILE",
                    "Name: $name ,photo url : $photoUrl"
                )
            },
            onFailure = { error ->
                Log.e(
                    "PROFILE",
                    "Failed to load profile",
                    error
                )
            }
        )
    }

    Box(
        modifier=Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FA))
    ){
        Column(
             modifier=Modifier.fillMaxWidth()
        ) {
             Row(
                 modifier= Modifier
                     .fillMaxWidth()
                     .padding(
                         start=12.dp,
                         end=12.dp,
                         top=12.dp
                     ),
                 verticalAlignment=Alignment.CenterVertically
             ) {
               IconButton(
                   onClick={
                       navController.popBackStack()
                   }
               ){
                   Icon(
                       imageVector = Icons.Rounded.ArrowBack,
                       contentDescription = "Back"
                   )
               }
                 Text(
                     text="You",
                     modifier=Modifier.weight(1f),
                     fontSize = 14.sp
                 )
                 IconButton(
                     onClick = { }
                 ) {
                     Icon(
                         imageVector = Icons.Rounded.MoreVert,
                         contentDescription = "More"
                     )
                 }
             }
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = userName,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "● Sharing location",
                    fontSize = 13.sp,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}