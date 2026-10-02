package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glimpse.BuildConfig
import com.example.glimpse.Location.LocationRepository
import com.example.glimpse.connection.ConnectionRequestViewModel
import com.example.glimpse.firebase.FirebaseRepository
import com.example.glimpse.groups.GroupsViewModel
import com.example.glimpse.model.ConnectionRequest
import com.example.glimpse.model.GlimpseGroup
import com.example.glimpse.model.GroupMember
import com.example.glimpse.model.UserLocation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.spatialk.geojson.Position
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Point
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import com.example.glimpse.model.SharingPermissions
private val GlimpseBlue = Color(0xFF0077BE)

private fun getLocationStatus(timestamp: Long,refreshTick: Int):String{
    val age = System.currentTimeMillis() - timestamp

    return when {
        age < 2 * 60 * 1000 -> "Live"
        age < 10 * 60 * 1000 -> "${age / 60000} min ago"
        age < 60 * 60 * 1000 -> "${age / 60000} min ago"
        else -> "Location is stale"
    }
}

data class GroupMapLocation(
    val member: GroupMember,
    val location: UserLocation
)

@Composable
fun GroupMapScreen(
    groupId: String,
    onBack: () -> Unit,
    groupsViewModel: GroupsViewModel = viewModel(),
    connectionViewModel: ConnectionRequestViewModel = viewModel()
) {
    val repository = remember {
        FirebaseRepository()
    }

    val context = LocalContext.current

    val locationRepository = remember {
        LocationRepository(context)
    }

    val scope = rememberCoroutineScope()

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    var group by remember {
        mutableStateOf<GlimpseGroup?>(null)
    }

    var members by remember {
        mutableStateOf(emptyList<GroupMember>())
    }

    var locations by remember {
        mutableStateOf(emptyList<UserLocation>())
    }


    var groupLoaded by remember {
        mutableStateOf(false)
    }

    var membersLoaded by remember {
        mutableStateOf(false)
    }

    var locationsLoaded by remember {
        mutableStateOf(false)
    }

    var freshnessTick by remember {
        mutableStateOf(0)
    }

    var selectedMember by remember {
        mutableStateOf<GroupMapLocation?>(null)
    }

    var selectedMemberPermission by remember{
        mutableStateOf<SharingPermissions?>(null)
    }

    var groupSharingPermissions by remember {
        mutableStateOf<Map<String, SharingPermissions>>(emptyMap())
    }

    if (currentUid == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("User is not logged in")
        }
        return
    }

    LaunchedEffect(groupId) {

        groupsViewModel.getGroup(
            uid = currentUid,
            onSuccess = { groups ->

                val selectedGroup =
                    groups.firstOrNull { it.id == groupId }

                group = selectedGroup
                groupLoaded = true

                if (selectedGroup != null) {

                    groupsViewModel.getGroupMembers(
                        memberIds = selectedGroup.members.keys.toList(),
                        onSuccess = {
                            members = it
                            membersLoaded = true
                        },
                        onFailure = {
                            membersLoaded = true
                        }
                    )

                    selectedGroup.members.keys.forEach { memberUid ->
                        groupsViewModel.getGroupSharingPermissions(
                            groupId = selectedGroup.id,
                            uid = memberUid,
                            onSuccess = { permissions ->
                                groupSharingPermissions =
                                    groupSharingPermissions + (
                                            memberUid to permissions
                                            )
                            },
                            onFailure = {
                                groupSharingPermissions =
                                    groupSharingPermissions + (
                                            memberUid to SharingPermissions(
                                                location = false,
                                                profile = true,
                                                locationHistory = false
                                            )
                                            )
                            }
                        )
                    }


                } else {
                    membersLoaded = true
                }
            },
            onFailure = {
                groupLoaded = true
                membersLoaded = true
            }
        )

        repository.getUsersLocations { result ->
            locations = result
            locationsLoaded = true
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            freshnessTick++
        }
    }

    val isLoading =
        !groupLoaded ||
                !membersLoaded ||
                !locationsLoaded

    val visibleLocations = remember(
        group,
        members,
        locations,
        groupSharingPermissions,
        currentUid
    ){
        val currentGroup = group
            ?: return@remember emptyList()

        val groupMemberIds =
            currentGroup.members.keys

        members.mapNotNull { member ->

            if (member.uid !in groupMemberIds) {
                return@mapNotNull null
            }

            val location =
                locations.firstOrNull {
                    it.uid == member.uid
                }
                    ?: return@mapNotNull null

            val canShowLocation =
                member.uid == currentUid ||
                        groupSharingPermissions[member.uid]?.location == true

            if (!canShowLocation) {
                return@mapNotNull null
            }

            GroupMapLocation(
                member = member,
                location = location
            )
        }
    }

    val cameraState = rememberCameraState()

    LaunchedEffect(visibleLocations) {

        if (visibleLocations.isNotEmpty()) {

            val latitude =
                visibleLocations
                    .map { it.location.latitude }
                    .average()

            val longitude =
                visibleLocations
                    .map { it.location.longitude }
                    .average()

            cameraState.animateTo(
                CameraPosition(
                    target = Position(
                        longitude,
                        latitude
                    ),
                    zoom = if (visibleLocations.size == 1) {
                        15.0
                    } else {
                        12.5
                    }
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(
                "https://api.maptiler.com/maps/01a06f93-3199-72ed-900a-c45024b0e205/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
            ),
            cameraState = cameraState
        ) {

            for (item in visibleLocations) {

                val source = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        Point(
                            Position(
                                longitude = item.location.longitude,
                                latitude = item.location.latitude
                            )
                        )
                    )
                )

                CircleLayer(
                    id = "group-location-${item.member.uid}",
                    source = source,
                    color = const(
                        if (
                            System.currentTimeMillis() - item.location.timestamp <
                            2 * 60 * 1000
                        ) {
                            GlimpseBlue
                        } else if (
                            System.currentTimeMillis() - item.location.timestamp <
                            10 * 60 * 1000
                        ) {
                            Color.Gray
                        } else {
                            Color.LightGray
                        }
                    )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {

                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
                    .background(Color.White)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    )
            ) {

                Text(
                    text = group?.name ?: "Group Map",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = 180.dp
                )
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {

            IconButton(
                onClick = {

                    locationRepository.getCurrentLocation { location ->

                        if (location != null) {

                            scope.launch {

                                cameraState.animateTo(
                                    CameraPosition(
                                        target = Position(
                                            location.longitude,
                                            location.latitude
                                        ),
                                        zoom = 15.5
                                    )
                                )
                            }
                        }
                    }
                }
            ) {

                Icon(
                    imageVector = Icons.Rounded.MyLocation,
                    contentDescription = "My location",
                    tint = GlimpseBlue
                )
            }
        }

        if (isLoading) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator(
                    color = GlimpseBlue
                )
            }
        }

        if (!isLoading && visibleLocations.isEmpty()) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .clip(
                        RoundedCornerShape(24.dp)
                    )
                    .background(Color.White)
                    .padding(20.dp)
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector = Icons.Rounded.LocationOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "No shared locations",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "No group member is currently sharing their location with you.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        if (selectedMember != null) {
            MemberLocationSheet(
                member = selectedMember!!.member,
                location = selectedMember!!.location,
                permissions = selectedMemberPermission ?: SharingPermissions(
                    location = false,
                    profile=true,
                    locationHistory = false
                ),
                placeName = null,
                onDismiss = {
                    selectedMember = null
                },
                onViewProfile = {
                    // connect later
                },
                onGetDirections = {
                    // connect later
                }
            )
        }

        if (!isLoading && visibleLocations.isNotEmpty()) {

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .clip(
                        RoundedCornerShape(24.dp)
                    )
                    .background(Color.White)
                    .padding(16.dp)
            ) {

                Text(
                    text = "${visibleLocations.size} sharing location",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                visibleLocations.forEach { item ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                selectedMember = item
                                selectedMemberPermission =
                                    groupSharingPermissions[item.member.uid]
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(GlimpseBlue)
                        )

                        Spacer(
                            modifier = Modifier.size(10.dp)
                        )

                        Column {
                            Text(
                                text = item.member.name.ifBlank {
                                    "Unknown"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = getLocationStatus(item.location.timestamp,freshnessTick),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (
                                    System.currentTimeMillis() - item.location.timestamp <
                                    2 * 60 * 1000
                                ) {
                                    GlimpseBlue
                                } else {
                                    Color.Gray
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}