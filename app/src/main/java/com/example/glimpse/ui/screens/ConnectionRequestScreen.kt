package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.glimpse.ui.theme.OceanBlue

@Composable
fun ConnectionRequestScreen(
    navController: NavController,
    requesterGlimpseId:String,
    requesterName: String,
    requesterPhotoUrl:String
){
    val liveLocation by remember {mutableStateOf(false)}
    val checkins by remember {mutableStateOf(false)}
}