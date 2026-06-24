package com.example.glimpse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import android.R.attr.x

@Composable
fun OnboardingScreen1(){
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF050816))
    ){
        PresenceOrb(
            color = Color(0xFFFF8FAB),
            x=80.dp,
            y=250.dp,
            pulseDuration=1500
        )
        PresenceOrb(
            color = Color(0xFFFFD6A5),
            x=180.dp,
            y=420.dp,
            pulseDuration=3000
        )
        PresenceOrb(
            color = Color(0xFFB8A1FF),
            x=50.dp,
            y=600.dp,
            pulseDuration=2200
        )
            PresenceOrb(
            color = Color(0xFF8ECAE6),
            x=260.dp,
            y=650.dp,
            pulseDuration=1800
        )
        PresenceOrb(
            color = Color(0xFFF4F7FF),
            x=280.dp,
            y=120.dp,
            pulseDuration = 2700
        )

    }
}

@Composable
fun PresenceOrb(
    color: Color,
    x: androidx.compose.ui.unit.Dp,
    y:androidx.compose.ui.unit.Dp,
    pulseDuration:Int
){
   val infiniteTransition = rememberInfiniteTransition(label ="")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1F,
        targetValue = 1.3F,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration),
            repeatMode = RepeatMode.Reverse
        )
    )
    val driftX by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000),
            repeatMode = RepeatMode.Reverse
        ),
        label=""

    )
    Box(
        modifier = Modifier
            .offset(
                x=x+driftX.dp,
                y=y
            )
            .scale(scale)
            .size(80.dp),
                contentAlignment = Alignment.Center
    ){
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color.copy(.2f),
                    CircleShape
                )
        )
        Box(
            modifier=Modifier
                .size(60.dp)
                .background(
                    color,
                    CircleShape
                )
        )
    }

}