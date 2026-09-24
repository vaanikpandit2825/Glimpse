package com.example.glimpse.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.glimpse.Location.RequestLocationPermission
import androidx.compose.ui.platform.LocalContext
import com.example.glimpse.places.PlaceSearchRepository
import com.example.glimpse.places.PlaceSearchResult
import kotlinx.coroutines.delay
import com.example.glimpse.Location.LocationRepository
import androidx.compose.ui.platform.LocalContext

private val GlimpseNavy = Color(0xFF14202B)
private val GlimpseBlue = Color(0xFF0077BE)
private val GlimpseBlueLight = Color(0xFFE8F4FB)
private val GlimpseTextGray = Color(0xFF718096)
private val GlimpseBackground = Color(0xFFFAFBFD)
private val GlimpseBorder = Color(0xFFE6EDF1)

data class PlaceSearchItem(
    val name: String,
    val address: String
)

@Composable
fun AddPlaceScreen(
    onBack: () -> Unit = {},
    onUseCurrentLocation: () -> Unit = {},
    onPickOnMap: () -> Unit = {},
    onPlaceSelected: (PlaceSearchResult) -> Unit = {}
) {
    var searchQuery by remember {
        mutableStateOf("")
    }

    var searchResults by remember {
        mutableStateOf<List<PlaceSearchResult>>(emptyList())
    }

    var isSearching by remember {
        mutableStateOf(false)
    }

    var searchError by remember {
        mutableStateOf<String?>(null)
    }

    var currentLatitude by remember {
        mutableStateOf<Double?>(null)
    }

    var currentLongtitude by remember{
        mutableStateOf<Double?>(null)
    }

    val searchRepository = remember {
        PlaceSearchRepository()
    }

    val context= LocalContext.current
    val locationRepository=remember{
        LocationRepository(context)
    }

    RequestLocationPermission(
        onPermissionGranted = {
            locationRepository.getCurrentLocation { location ->
                currentLatitude = location?.latitude
                currentLongtitude = location?.longitude
            }
        }
    )

    LaunchedEffect(searchQuery) {

        if (searchQuery.trim().length < 2) {
            searchResults = emptyList()
            isSearching = false
            searchError = null
            return@LaunchedEffect
        }

        delay(400)

        isSearching = true
        searchError = null

        searchRepository.searchPlaces(
            query = searchQuery,
            latitude = currentLatitude,
            longitude = currentLongtitude,
            onResult = { results ->
                searchResults = results
                isSearching = false
            },
            onFailure = { exception ->
                searchResults = emptyList()
                isSearching = false
                searchError = exception.message
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = GlimpseBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = GlimpseNavy
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "G L I M P S E",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = GlimpseNavy
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Add a place",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = GlimpseNavy
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Find somewhere you want to keep close.",
                fontSize = 15.sp,
                color = GlimpseTextGray
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = {
                    Text(
                        text = "Search places",
                        color = GlimpseTextGray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = GlimpseNavy
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Clear search",
                                tint = GlimpseTextGray
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                QuickAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.LocationOn,
                    title = "Use current location",
                    subtitle = "Use your location",
                    onClick = onUseCurrentLocation
                )

                QuickAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Map,
                    title = "Pick on map",
                    subtitle = "Choose manually",
                    onClick = onPickOnMap
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Search results

            if (isSearching) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Searching...",
                        fontSize = 14.sp,
                        color = GlimpseTextGray
                    )
                }

            } else if (searchError != null) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Couldn't search for places",
                        fontSize = 14.sp,
                        color = GlimpseTextGray
                    )
                }

            } else if (searchResults.isNotEmpty()) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    Text(
                        text = "Matched locations",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlimpseTextGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(searchResults) { place ->

                            SearchResultItem(
                                place = place,
                                onClick = {
                                    onPlaceSelected(place)
                                }
                            )
                        }
                    }
                }

            } else if (searchQuery.trim().length >= 2) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No places found",
                        fontSize = 14.sp,
                        color = GlimpseTextGray
                    )
                }

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(GlimpseBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                tint = GlimpseBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Search for a place",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlimpseNavy
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Or choose a location directly on the map.",
                            fontSize = 13.sp,
                            color = GlimpseTextGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = GlimpseBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 14.dp,
                vertical = 13.dp
            )
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlimpseBlue,
            modifier = Modifier.size(21.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = GlimpseNavy
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = GlimpseTextGray
        )
    }
}

@Composable
private fun SearchResultItem(
    place: PlaceSearchResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(
                horizontal = 10.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GlimpseBlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Place,
                contentDescription = null,
                tint = GlimpseBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = place.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlimpseNavy
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = place.address,
                fontSize = 12.sp,
                color = GlimpseTextGray
            )
        }
    }
}