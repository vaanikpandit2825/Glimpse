package com.example.glimpse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun MyLocationButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
)   {
    Box(
        modifier=Modifier
            .size(56.dp)
            .shadow(
                elevation = 8.dp,
                shape=CircleShape,
                clip=false
            )
            .background(
                color=MaterialTheme.colorScheme.surface,
                shape=CircleShape
            )
            .clickable{onClick()},
        contentAlignment=Alignment.Center
    ){
        Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = "My Location",
            tint=MaterialTheme.colorScheme.primary
        )
    }
}