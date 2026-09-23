package com.example.glimpse.ui.screens

import android.graphics.Camera
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimpse.BuildConfig
import com.example.glimpse.places.PlaceSearchResult
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.location.Location
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

private val GlimpseNavy = Color(0xFF14202B)
private val GlimpseBlue = Color(0xFF0077BE)
private val GlimpseBlueLight = Color(0xFFE8F4FB)
private val GlimpseTextGray = Color(0xFF718096)
private val GlimpseBackground = Color(0xFFFAFBFD)

@Composable
fun ConfirmPlaceScreen(
    place: PlaceSearchResult,
    onBack:()->Unit={},
    onUseLocation: (PlaceSearchResult)->Unit={},
    onAdjustPin:()->Unit={}
){
    val scope= rememberCoroutineScope()
    val cameraState=rememberCameraState()
    val placeSource=rememberGeoJsonSource(
        data= GeoJsonData.Features(
            Point(
                Position(
                    longitude = place.longitude,
                    latitude = place.latitude
                )
            )
        )
    )
    LaunchedEffect(place) {
        cameraState.animateTo(
            CameraPosition(
                target = Position(
                    longitude = place.longitude,
                    latitude = place.latitude
                ),
                zoom=15.5
            )
        )
    }
    Box(
        modifier=Modifier.fillMaxSize()
    ){
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(
                "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
            ),
            cameraState = cameraState
        ){
            CircleLayer(
                id="selected-place",
                source=placeSource,
                color=const(GlimpseBlue)
            )
        }
        Surface(
            modifier=Modifier
                .padding(
                    start=16.dp,
                    end=16.dp,
                    top=18.dp
                )
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color= Color.White.copy(alpha = 0.96f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 6.dp,
                        vertical = 4.dp
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

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Confirm location",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlimpseNavy
                    )

                    Text(
                        text = "Make sure this is the right place",
                        fontSize = 11.sp,
                        color = GlimpseTextGray
                    )
                }
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 5.dp
        ) {

            IconButton(
                onClick = {
                    scope.launch {
                        cameraState.animateTo(
                            CameraPosition(
                                target = Position(
                                    longitude = place.longitude,
                                    latitude = place.latitude
                                ),
                                zoom = 16.0
                            )
                        )
                    }
                }
            ) {

                Icon(
                    imageVector = Icons.Outlined.GpsFixed,
                    contentDescription = "Center location",
                    tint = GlimpseNavy
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp
                ),
                color = GlimpseBackground,
                shadowElevation = 12.dp
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 18.dp
                        )
                ) {

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(38.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(
                                Color(0xFFD2D9DE)
                            )
                    )

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GlimpseBlueLight),
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = GlimpseBlue,
                                modifier = Modifier.size(25.dp)
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(14.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = place.name,
                                fontSize = 15.sp,
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

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onUseLocation(place)
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = GlimpseBlue
                    ) {

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "Use this location  →",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text = "Adjust pin position",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAdjustPin()
                            }
                            .padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlimpseBlue
                    )
                }
            }
        }
    }
}
