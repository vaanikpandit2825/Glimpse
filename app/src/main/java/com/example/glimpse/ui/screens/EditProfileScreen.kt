package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Boy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glimpse.firebase.FirebaseRepository

@Composable
fun EditProfileScreen(
    navController: NavController
){
    var username by remember { mutableStateOf("") }
    val firebaseRepository=remember { FirebaseRepository() }

    LaunchedEffect(Unit) {
        firebaseRepository.getCurrentUserProfile(
            onResult = { name, _ ->
                username = name
            },
            onFailure={ }
        )
    }
    Column(
        modifier=Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FA))
    ) {
        Row(
            modifier= Modifier
                .fillMaxWidth()
                .padding(
                    start=12.dp,
                    end = 12.dp,
                    top=12.dp
                ),
            verticalAlignment=Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text="Edit Profile",
                fontSize = 18.sp
            )
        }
        Spacer(modifier=Modifier.size(32.dp))

        Column(
            modifier= Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text="Name",
                fontSize = 14.sp
            )
            Spacer(modifier=Modifier.size(8.dp))

            Text(
                text=username,
                fontSize = 18.sp
            )
        }

    }
}
