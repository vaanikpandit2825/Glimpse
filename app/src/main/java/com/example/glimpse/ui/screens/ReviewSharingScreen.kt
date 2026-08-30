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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun ReviewSharingScreen(
    navController: NavController,
    senderUid: String
) {
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val viewModel: ConnectionRequestViewModel = viewModel()
    val primary = Color(0xFF0077BE)

    var name by remember { mutableStateOf("") }
    var profilePhotoUrl by remember { mutableStateOf("") }

    val previousEntry = navController.previousBackStackEntry

    val location = previousEntry
        ?.savedStateHandle
        ?.get<Boolean>("sharing_location")
        ?: false

    val profile = previousEntry
        ?.savedStateHandle
        ?.get<Boolean>("sharing_profile")
        ?: false

    val locationHistory = previousEntry
        ?.savedStateHandle
        ?.get<Boolean>("sharing_location_history")
        ?: false

    LaunchedEffect(senderUid) {
        viewModel.getUserProfile(
            uid = senderUid,
            onResult = { userName, photoUrl ->
                name = userName
                profilePhotoUrl = photoUrl
            },
            onFailure = {
                name = "Unknown user"
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
                .height(60.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Review sharing",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (profilePhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profilePhotoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(82.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .size(82.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE1F0FA)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = name.ifEmpty { "Unknown user" },
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "You're connecting with ${name.ifEmpty { "this person" }}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "WHAT ${name.ifEmpty { "THEY" }.uppercase()} CAN SEE",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            SharingSummaryRow(
                icon = Icons.Rounded.LocationOn,
                title = "Current location",
                description = "Your real-time location while active",
                enabled = location,
                primary = primary
            )

            SharingSummaryRow(
                icon = Icons.Rounded.Person,
                title = "Name and profile photo",
                description = "Your public identity details",
                enabled = profile,
                primary = primary
            )

            SharingSummaryRow(
                icon = Icons.Rounded.History,
                title = "Previous locations",
                description = "Where you've been in the past",
                enabled = locationHistory,
                primary = primary
            )

            Spacer(modifier = Modifier.height(42.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = primary
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "You're in control",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "You can change sharing permissions anytime.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ready to connect?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = "${name.ifEmpty { "This person" }} will only receive the information shown above.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = errorMessage,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }
        }

        Button(
            onClick = {
                isLoading = true
                errorMessage = ""

                val permissions = SharingPermissions(
                    location = location,
                    locationHistory = locationHistory,
                    profile = profile
                )

                viewModel.acceptConnectionRequest(
                    senderUid = senderUid,
                    permissions = permissions,
                    onSuccess = {
                        isLoading = false
                        errorMessage="Connection succedded"
                    },
                    onFailure = { error ->
                        isLoading = false
                        errorMessage =
                            error.message ?: "Something went wrong"
                    }
                )
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 18.dp
                )
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary
            )
        ) {
            Text(
                text = if (isLoading) {
                    "Connecting..."
                } else {
                    "Confirm connection"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SharingSummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    enabled: Boolean,
    primary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = if (enabled) {
                primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        if (enabled) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Shared",
                modifier = Modifier.size(19.dp),
                tint = primary
            )
        } else {
            Text(
                text = "Not shared",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(
                        horizontal = 9.dp,
                        vertical = 5.dp
                    )
            )
        }
    }
}