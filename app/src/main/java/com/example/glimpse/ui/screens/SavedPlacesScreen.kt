package com.example.glimpse.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glimpse.BuildConfig
import com.example.glimpse.model.SavedPlace
import com.example.glimpse.places.SavedPlaceViewModel
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// GLIMPSE brand colors (see project design spec)
private val GlimpseNavy = Color(0xFF14202B)
private val GlimpseBlue = Color(0xFF0077BE)
private val GlimpseBlueLight = Color(0xFFE8F4FB)
private val GlimpseTextGray = Color(0xFF718096)
private val GlimpseBackground = Color(0xFFFAFBFD)
private val GlimpseDelete = Color(0xFFD94B4B)
private val GlimpseDeleteBackground = Color(0xFFFFF1F1)
private val GlimpseBorder = Color(0xFFE6EDF1)

/**
 * Saved Places screen.
 *
 * All data shown here (markers, counts, selected place details) comes from
 * Firebase via [SavedPlaceViewModel]. Nothing is hardcoded — an account
 * with zero saved places renders the empty state, one place renders one
 * marker/chip, N places render N of them.
 *
 * [onEditPlace] and [onAddPlace] are clean navigation hooks. Neither
 * AddSavedPlaceScreen nor an edit flow exist yet in the project, so this
 * screen does not fake saving/editing — it just calls back out so those
 * flows can be wired up next.
 */
@Composable
fun SavedPlacesScreen(
    onBack: () -> Unit = {},
    onAddPlace: () -> Unit = {},
    onEditPlace: (SavedPlace) -> Unit = {}
) {
    val viewModel: SavedPlaceViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val cameraState = rememberCameraState()

    var places by remember { mutableStateOf<List<SavedPlace>>(emptyList()) }
    var selectedPlace by remember { mutableStateOf<SavedPlace?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadPlaces() {
        isLoading = true
        errorMessage = null

        viewModel.getSavedPlaces(
            onResult = { result ->
                places = result
                selectedPlace = result.firstOrNull()
                isLoading = false
            },
            onFailure = { exception ->
                Log.e("GLIMPSE_SAVED_PLACES", "Failed to load saved places", exception)
                errorMessage = exception.message ?: "Couldn't load your places"
                isLoading = false
            }
        )
    }

    fun deletePlace(place: SavedPlace) {
        viewModel.deleteSavedPlace(
            placeId = place.id,
            onSuccess = {
                val updated = places.filterNot { it.id == place.id }
                places = updated

                selectedPlace = if (selectedPlace?.id == place.id) {
                    updated.firstOrNull()
                } else {
                    selectedPlace
                }
            },
            onFailure = { exception ->
                Log.e("GLIMPSE_SAVED_PLACES", "Failed to delete place", exception)
                errorMessage = exception.message ?: "Couldn't delete that place"
            }
        )
    }

    LaunchedEffect(Unit) {
        loadPlaces()
    }

    // Keep the camera centered on whichever place is selected.
    LaunchedEffect(selectedPlace) {
        val place = selectedPlace ?: return@LaunchedEffect

        cameraState.animateTo(
            CameraPosition(
                target = Position(
                    longitude = place.longtitude,
                    latitude = place.latitude
                ),
                zoom = 15.5
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlimpseBackground)
    ) {

        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(
                "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
            ),
            cameraState = cameraState
        ) {
            // One real marker per saved place, at its actual coordinates.
            places.forEach { place ->
                val isSelected = place.id == selectedPlace?.id

                val placeSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        Point(
                            Position(
                                longitude = place.longtitude,
                                latitude = place.latitude
                            )
                        )
                    )
                )

                CircleLayer(
                    id = "saved-place-${place.id}",
                    source = placeSource,
                    color = const(if (isSelected) GlimpseBlue else Color.White)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 6.dp
                )
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "GLIMPSE",
                        color = GlimpseNavy,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Your Places",
                        color = GlimpseNavy,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-1.2).sp
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${places.size} saved",
                            color = GlimpseTextGray,
                            fontSize = 15.sp
                        )

                        val selected = selectedPlace
                        if (selected != null) {
                            Text(
                                text = "  •  ",
                                color = GlimpseTextGray,
                                fontSize = 14.sp
                            )

                            Text(
                                text = "${selected.name} selected",
                                color = GlimpseBlue,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(width = 1.dp, color = Color.White, shape = CircleShape)
                        .shadow(elevation = 5.dp, shape = CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Back",
                        tint = GlimpseNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White)
                    .border(width = 1.dp, color = GlimpseBorder, shape = RoundedCornerShape(50.dp))
                    .clickable(onClick = onAddPlace)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(GlimpseBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(9.dp))

                Text(
                    text = "Save a place",
                    color = GlimpseNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (places.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(places, key = { it.id }) { place ->
                        PlaceChip(
                            place = place,
                            selected = place.id == selectedPlace?.id,
                            onClick = { selectedPlace = place }
                        )
                    }
                }
            }
        }

        // Recenter on the currently selected place — real behavior, not decorative.
        IconButton(
            onClick = {
                val place = selectedPlace
                if (place != null) {
                    scope.launch {
                        cameraState.animateTo(
                            CameraPosition(
                                target = Position(
                                    longitude = place.longtitude,
                                    latitude = place.latitude
                                ),
                                zoom = 16.0
                            )
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp, bottom = 260.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White)
                .shadow(elevation = 4.dp, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Outlined.MyLocation,
                contentDescription = "Recenter",
                tint = GlimpseNavy,
                modifier = Modifier.size(20.dp)
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GlimpseBlue)
                }
            }

            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Color.White)
                        .shadow(elevation = 14.dp, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Something went wrong",
                        color = GlimpseNavy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = errorMessage ?: "",
                        color = GlimpseTextGray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(13.dp))
                            .background(GlimpseNavy)
                            .clickable { loadPlaces() }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Try again",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            places.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Color.White)
                        .shadow(elevation = 14.dp, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No saved places yet",
                        color = GlimpseNavy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Save a place to make it part of your GLIMPSE.",
                        color = GlimpseTextGray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(13.dp))
                            .background(GlimpseBlue)
                            .clickable(onClick = onAddPlace)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Save a place",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            else -> {
                val selected = selectedPlace
                if (selected != null) {
                    SavedPlaceBottomSheet(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        place = selected,
                        onEdit = { onEditPlace(selected) },
                        onDelete = { deletePlace(selected) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceChip(
    place: SavedPlace,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) GlimpseBlue else Color.White)
            .border(
                width = 1.dp,
                color = if (selected) GlimpseBlue else GlimpseBorder,
                shape = RoundedCornerShape(50.dp)
            )
            .shadow(elevation = if (selected) 4.dp else 2.dp, shape = RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = iconForType(place.type),
            contentDescription = null,
            tint = if (selected) Color.White else GlimpseNavy,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = place.name,
            color = if (selected) Color.White else GlimpseNavy,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SavedPlaceBottomSheet(
    modifier: Modifier = Modifier,
    place: SavedPlace,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 20.dp)
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(42.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(GlimpseBorder)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(GlimpseBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForType(place.type),
                    contentDescription = null,
                    tint = GlimpseBlue,
                    modifier = Modifier.size(27.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    color = GlimpseNavy,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = place.type.replaceFirstChar { it.uppercase() },
                    color = GlimpseTextGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PlaceInfo(title = "Radius", value = "${place.radius.toInt()} m")
            PlaceInfo(title = "Type", value = place.type.replaceFirstChar { it.uppercase() })
            PlaceInfo(title = "Saved", value = formatSavedDate(place.createdAt))
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(GlimpseNavy)
                    .clickable(onClick = onEdit),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Edit place",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier
                    .weight(0.75f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(GlimpseDeleteBackground)
                    .border(width = 1.dp, color = Color(0xFFF5D4D4), shape = RoundedCornerShape(13.dp))
                    .clickable(onClick = onDelete),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = GlimpseDelete,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = "Delete",
                    color = GlimpseDelete,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PlaceInfo(title: String, value: String) {
    Column {
        Text(text = title, color = GlimpseTextGray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = value, color = GlimpseNavy, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

private fun iconForType(type: String): ImageVector {
    return when (type.lowercase(Locale.ROOT)) {
        "home" -> Icons.Outlined.Home
        "work" -> Icons.Outlined.Work
        "college", "school", "university" -> Icons.Outlined.School
        "gym" -> Icons.Outlined.People
        else -> Icons.Outlined.Place
    }
}

private fun formatSavedDate(timestamp: Long): String {
    if (timestamp <= 0L) return "—"
    val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
    return formatter.format(Date(timestamp))
}