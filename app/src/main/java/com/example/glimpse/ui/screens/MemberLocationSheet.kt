package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glimpse.model.GroupMember
import com.example.glimpse.model.SharingPermissions
import com.example.glimpse.model.UserLocation

private val GlimpseBlue = Color(0xFF0077BE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberLocationSheet(
    member: GroupMember,
    location: UserLocation,
    permissions: SharingPermissions,
    placeName: String?,
    onDismiss: () -> Unit,
    onViewProfile: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val isLive =
        System.currentTimeMillis() - location.timestamp <
                2 * 60 * 1000

    val locationStatus = remember(location.timestamp) {
        if (isLive) {
            "Live"
        } else {
            val age = System.currentTimeMillis() - location.timestamp

            when {
                age < 60 * 60 * 1000 ->
                    "${age / 60000} min ago"

                else ->
                    "Location is stale"
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(
                        width = 40.dp,
                        height = 4.dp
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.LightGray)
            )
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 24.dp
                )
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(
                            GlimpseBlue.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.name
                            .ifBlank { "U" }
                            .first()
                            .uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = GlimpseBlue
                    )
                }

                Spacer(
                    modifier = Modifier.size(14.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = member.name.ifBlank {
                            "Unknown"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isLive) {
                                        GlimpseBlue
                                    } else {
                                        Color.Gray
                                    }
                                )
                        )

                        Spacer(
                            modifier = Modifier.size(6.dp)
                        )

                        Text(
                            text = locationStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isLive) {
                                GlimpseBlue
                            } else {
                                Color.Gray
                            }
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // Location
            Text(
                text = "Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            GlimpseBlue.copy(alpha = 0.10f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = GlimpseBlue
                    )
                }

                Spacer(
                    modifier = Modifier.size(12.dp)
                )

                Column {
                    Text(
                        text = placeName?.takeIf {
                            it.isNotBlank()
                        } ?: "Current location",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = if (isLive) {
                            "Last updated just now"
                        } else {
                            "Last updated $locationStatus"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Divider(
                color = Color(0xFFEAEAEA)
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // Sharing
            Text(
                text = "Sharing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            SharingRow(
                title = "Current location",
                enabled = permissions.location
            )

            SharingRow(
                title = "Previous locations",
                enabled = permissions.locationHistory
            )

            SharingRow(
                title = "Name & profile",
                enabled = permissions.profile
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            // Actions
            Button(
                onClick = onViewProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GlimpseBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text(
                    text = "View profile",
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )
        }
    }
}

@Composable
private fun SharingRow(
    title: String,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) {
                        GlimpseBlue
                    } else {
                        Color.LightGray
                    }
                )
        )

        Spacer(
            modifier = Modifier.size(12.dp)
        )

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = if (enabled) "Shared" else "Not shared",
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) {
                GlimpseBlue
            } else {
                Color.Gray
            }
        )
    }
}