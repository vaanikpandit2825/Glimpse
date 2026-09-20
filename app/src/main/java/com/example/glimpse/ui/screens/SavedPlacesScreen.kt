package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GlimpseNavy = Color(0xFF14202B)
private val GlimpseBlue = Color(0xFF0077BE)
private val GlimpseTextGray = Color(0xFF718096)
private val GlimpseBackground = Color(0xFFFAFBFD)

@Composable
fun SavedPlacesScreen(
    onBack: () -> Unit = {},
    onAddPlace: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlimpseBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 16.dp
                ),
            verticalArrangement = Arrangement.Top
        ) {

            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = GlimpseNavy
                )
            }

            Text(
                text = "GLIMPSE",
                color = GlimpseBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 18.dp)
            )

            Text(
                text = "Your Places",
                color = GlimpseNavy,
                fontSize = 42.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 46.sp,
                modifier = Modifier.padding(top = 12.dp)
            )

            Text(
                text = "Save the places that matter.",
                color = GlimpseTextGray,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 14.dp)
            )

            Text(
                text = "Used for arrivals, departures and proximity alerts.",
                color = GlimpseTextGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            SavedPlaceRow(
                icon = Icons.Rounded.Home,
                name = "Home",
                location = "Tambaram, Chennai",
                detail = "200 m radius",
                active = true
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
        ) {
            val path = Path()

            path.moveTo(
                0f,
                size.height
            )

            path.lineTo(
                0f,
                size.height * 0.65f
            )

            path.quadraticBezierTo(
                size.width * 0.18f,
                size.height * 0.35f,
                size.width * 0.35f,
                size.height * 0.65f
            )

            path.quadraticBezierTo(
                size.width * 0.55f,
                size.height * 0.95f,
                size.width * 0.72f,
                size.height * 0.45f
            )

            path.quadraticBezierTo(
                size.width * 0.86f,
                size.height * 0.15f,
                size.width,
                size.height * 0.5f
            )

            path.lineTo(
                size.width,
                size.height
            )

            path.close()

            drawPath(
                path = path,
                color = Color(0xFFEAF4FA)
            )
        }

        IconButton(
            onClick = onAddPlace,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add a place",
                tint = GlimpseBlue,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Composable
private fun SavedPlaceRow(
    icon: ImageVector,
    name: String,
    location: String,
    detail: String,
    active: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GlimpseNavy,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(18.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = name,
                    color = GlimpseNavy,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Medium
                )

                if (active) {
                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = Color(0xFF16A979),
                                shape = CircleShape
                            )
                    )
                }
            }

            Text(
                text = location,
                color = GlimpseTextGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 3.dp)
            )

            Text(
                text = detail,
                color = GlimpseTextGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
        }

        Text(
            text = "›",
            color = GlimpseTextGray,
            fontSize = 30.sp
        )
    }
}