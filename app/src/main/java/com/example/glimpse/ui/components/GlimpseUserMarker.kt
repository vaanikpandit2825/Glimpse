package com.example.glimpse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
@Composable
fun GlimpseUserMarker(
    name: String,
    markerColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = 4.dp,
                    color = markerColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                color = markerColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.size(6.dp))

        Text(
            text = name,
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 7.dp
                ),
            color = Color(0xFF222222),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.size(5.dp))

        Box(
            modifier = Modifier
                .size(10.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(markerColor)
                .border(
                    width = 2.dp,
                    color = Color.White,
                    shape = CircleShape
                )
        )
    }
}
@Preview(showBackground = true)
@Composable
private fun GlimpseUserMarkerPreview() {
    GlimpseUserMarker(
        name = "Vaani",
        markerColor = Color(0xFF8B7CF6)
    )
}
