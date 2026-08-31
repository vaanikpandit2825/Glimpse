package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.glimpse.connection.ConnectionRequestViewModel
import com.example.glimpse.model.SharingPermissions

@Composable
fun SharingPermissionsScreen(
    navController: NavController,
    senderUid: String,
    senderSharing: SharingPermissions
) {
    val viewModel: ConnectionRequestViewModel = viewModel()
    val primary = Color(0xFF0077BE)

    var name by remember { mutableStateOf("") }
    var profilePhotoUrl by remember { mutableStateOf("") }
    var location by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf(true) }
    var locationHistory by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(senderUid) {
        viewModel.getUserProfile(
            uid = senderUid,
            onResult = { userName, photoUrl ->
                name = userName
                profilePhotoUrl = photoUrl
            },
            onFailure = { error ->
                errorMessage = error.message ?: "Failed to load profile"
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "Choose what to share",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (profilePhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profilePhotoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE1F0FA)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = name.ifEmpty { "Unknown user" },
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Wants to connect with you",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "WHAT ${name.ifEmpty { "THEY" }.uppercase()} SHARES WITH YOU",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${name.ifEmpty { "They" }} has chosen to share:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                SharedPermissionRow(
                    icon = Icons.Rounded.LocationOn,
                    title = "Current location",
                    enabled = senderSharing.location,
                    primary = primary
                )

                SharedPermissionRow(
                    icon = Icons.Rounded.Person,
                    title = "Name and profile photo",
                    enabled = senderSharing.profile,
                    primary = primary
                )

                SharedPermissionRow(
                    icon = Icons.Rounded.History,
                    title = "Previous locations",
                    enabled = senderSharing.locationHistory,
                    primary = primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "WHAT YOU SHARE WITH ${name.ifEmpty { "THEM" }.uppercase()}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Choose what ${name.ifEmpty { "they" }} can see:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                PermissionSwitchRow(
                    icon = Icons.Rounded.LocationOn,
                    title = "Current location",
                    description = "Your real-time location while active",
                    checked = location,
                    onCheckedChange = {
                        location = it
                    },
                    primary = primary
                )

                PermissionSwitchRow(
                    icon = Icons.Rounded.Person,
                    title = "Name and profile photo",
                    description = "Your public identity details",
                    checked = profile,
                    onCheckedChange = {
                        profile = it
                    },
                    primary = primary
                )

                PermissionSwitchRow(
                    icon = Icons.Rounded.History,
                    title = "Previous locations",
                    description = "Your location history",
                    checked = locationHistory,
                    onCheckedChange = {
                        locationHistory = it
                    },
                    primary = primary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "You're in control",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "You can change these permissions anytime.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = errorMessage,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Button(
            onClick = {
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("sharing_location", location)

                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("sharing_profile", profile)

                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("sharing_location_history", locationHistory)

                navController.navigate(
                    "reviewSharing/$senderUid"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 14.dp
                )
                .height(50.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary
            )
        ) {
            Text(
                text = "Review sharing",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SharedPermissionRow(
    icon: ImageVector,
    title: String,
    enabled: Boolean,
    primary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 11.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (enabled) {
                primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        if (enabled) {
            Text(
                text = "✓",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        } else {
            Text(
                text = "—",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    primary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 9.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = primary
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(width = 44.dp, height = 28.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primary
            )
        )
    }
}