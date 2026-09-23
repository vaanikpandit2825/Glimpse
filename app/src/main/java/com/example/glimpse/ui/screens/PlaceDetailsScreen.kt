package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimpse.places.PlaceSearchResult
import kotlin.math.roundToInt

private val GlimpseNavy = Color(0xFF14202B)
private val GlimpseBlue = Color(0xFF0077BE)
private val GlimpseBlueLight = Color(0xFFE8F4FB)
private val GlimpseTextGray = Color(0xFF718096)
private val GlimpseBackground = Color(0xFFFAFBFD)
private val GlimpseBorder = Color(0xFFE6EDF1)

@Composable
fun PlaceDetailsScreen(
    place: PlaceSearchResult,
    onBack: () -> Unit = {},
    onSave: (
        name: String,
        type: String,
        radius: Double
    ) -> Unit = { _, _, _ -> }
) {

    var placeName by remember {
        mutableStateOf(place.name)
    }

    var selectedType by remember {
        mutableStateOf("Custom")
    }

    var radius by remember {
        mutableFloatStateOf(200f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlimpseBackground)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
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

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Column {

                Text(
                    text = "Place details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlimpseNavy
                )

                Text(
                    text = "Make this place yours",
                    fontSize = 12.sp,
                    color = GlimpseTextGray
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )


            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White
            ) {

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GlimpseBlueLight),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = GlimpseBlue,
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = place.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlimpseNavy
                        )

                        Spacer(
                            modifier = Modifier.height(3.dp)
                        )

                        Text(
                            text = place.address,
                            fontSize = 12.sp,
                            color = GlimpseTextGray
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )


            Text(
                text = "Place name",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlimpseNavy
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            TextField(
                value = placeName,
                onValueChange = {
                    placeName = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(
                modifier = Modifier.height(26.dp)
            )

            Text(
                text = "Category",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlimpseNavy
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            val categories = listOf(
                "Home" to Icons.Outlined.Home,
                "Work" to Icons.Outlined.Work,
                "College" to Icons.Outlined.School,
                "Gym" to Icons.Outlined.FitnessCenter,
                "Custom" to Icons.Outlined.Business
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                categories.chunked(3).forEach { rowCategories ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        rowCategories.forEach { (type, icon) ->

                            CategoryItem(
                                modifier = Modifier.weight(1f),
                                type = type,
                                icon = icon,
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                }
                            )
                        }

                        repeat(3 - rowCategories.size) {
                            Spacer(
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Radius",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlimpseNavy
                    )

                    Text(
                        text = "Area around this place",
                        fontSize = 11.sp,
                        color = GlimpseTextGray
                    )
                }

                Text(
                    text = formatRadius(radius),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlimpseBlue
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Slider(
                value = radius,
                onValueChange = {
                    radius = it
                },
                valueRange = 50f..1000f,
                steps = 18,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "50 m",
                    fontSize = 11.sp,
                    color = GlimpseTextGray
                )

                Text(
                    text = "1 km",
                    fontSize = 11.sp,
                    color = GlimpseTextGray
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                )
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    if (placeName.isNotBlank()) {
                        onSave(
                            placeName.trim(),
                            selectedType,
                            radius.toDouble()
                        )
                    }
                },
            shape = RoundedCornerShape(16.dp),
            color = GlimpseBlue
        ) {

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Save place",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    modifier: Modifier,
    type: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            GlimpseBlueLight
        } else {
            Color.White
        }
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = type,
                tint = if (selected) {
                    GlimpseBlue
                } else {
                    GlimpseTextGray
                },
                modifier = Modifier.size(21.dp)
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = type,
                fontSize = 11.sp,
                fontWeight = if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },
                color = if (selected) {
                    GlimpseBlue
                } else {
                    GlimpseTextGray
                }
            )
        }
    }
}

private fun formatRadius(radius: Float): String {
    return if (radius >= 1000f) {
        "1 km"
    } else {
        "${radius.roundToInt()} m"
    }
}