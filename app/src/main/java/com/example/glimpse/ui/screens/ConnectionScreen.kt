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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
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
import com.example.glimpse.model.ConnectionRequest

@Composable
fun ConnectionsScreen(
    navController: NavController
) {
    val viewModel: ConnectionRequestViewModel = viewModel()

    var connections by remember {
        mutableStateOf<List<ConnectionRequest>>(emptyList())
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    val primary = Color(0xFF0077BE)

    LaunchedEffect(Unit) {
        viewModel.getConnections(
            onResult = { result:List<ConnectionRequest> ->
                connections = result
                errorMessage = ""
            },
            onFailure = { error ->
                errorMessage =
                    error.message ?: "Failed to load connections"
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
                .height(64.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Connections",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "GLIMPSE Network",
                    fontSize = 11.sp,
                    color = primary
                )
            }

            IconButton(
                onClick = {

                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = {
                    navController.navigate("addperson")
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.PersonAdd,
                    contentDescription = "Add connection",
                    tint = primary
                )
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 8.dp
                ),
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        if (connections.isEmpty() && errorMessage.isEmpty()) {

            EmptyConnectionsState(
                primary = primary,
                onAddConnection = {
                    navController.navigate("addperson")
                }
            )

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 8.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                item {

                    Text(
                        text = "Your trusted circle",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    Text(
                        text = "People you choose to share with",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    TrustedCircleVisual(
                        connectionCount = connections.size,
                        primary = primary
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONNECTED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Tap to view sharing",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )
                }

                items(
                    items = connections,
                    key = { it.senderUid }
                ) { connection ->

                    ConnectionItem(
                        connection = connection,
                        primary = primary,
                        onClick = {

                        }
                    )
                }

                item {

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    AddConnectionButton(
                        primary = primary,
                        onClick = {
                            navController.navigate("addperson")
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )

                        Text(
                            text = "Connections can only see what you intentionally share",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrustedCircleVisual(
    connectionCount: Int,
    primary: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp),
        contentAlignment = Alignment.Center
    ) {


        Box(
            modifier = Modifier
                .size(118.dp)
                .border(
                    width = 1.dp,
                    color = primary.copy(alpha = 0.16f),
                    shape = CircleShape
                )
        )


        Box(
            modifier = Modifier
                .size(94.dp)
                .border(
                    width = 1.dp,
                    color = primary.copy(alpha = 0.22f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(70.dp)
                .border(
                    width = 1.dp,
                    color = primary.copy(alpha = 0.30f),
                    shape = CircleShape
                )
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = connectionCount.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = primary
                )

                Text(
                    text = "CONNECTIONS",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }


        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(primary.copy(alpha = 0.45f))
                .align(Alignment.CenterStart)
                .padding(start = 28.dp)
        )

        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(primary.copy(alpha = 0.45f))
                .align(Alignment.CenterEnd)
                .padding(end = 28.dp)
        )
    }
}

@Composable
private fun ConnectionItem(
    connection: ConnectionRequest,
    primary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (connection.profilePhotoUrl.isNotEmpty()) {

            AsyncImage(
                model = connection.profilePhotoUrl,
                contentDescription = "Profile photo",
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

        } else {

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE1F0FA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = primary
                )
            }
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = connection.name.ifEmpty {
                        "Unknown user"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(
                    modifier = Modifier.width(7.dp)
                )

                Text(
                    text = "Connected",
                    fontSize = 8.sp,
                    color = Color(0xFF16865B),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE3F7EF))
                        .padding(
                            horizontal = 6.dp,
                            vertical = 3.dp
                        )
                )
            }

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            SharingSummary(
                connection = connection,
                primary = primary
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = "View connection",
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SharingSummary(
    connection: ConnectionRequest,
    primary: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (connection.senderSharing.location) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = primary
            )

            Spacer(
                modifier = Modifier.width(3.dp)
            )

            Text(
                text = "Location",
                fontSize = 10.sp,
                color = primary
            )
        }

        if (
            connection.senderSharing.location &&
            connection.senderSharing.profile
        ) {
            Text(
                text = "  •  ",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (connection.senderSharing.profile) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = primary
            )

            Spacer(
                modifier = Modifier.width(3.dp)
            )

            Text(
                text = "Profile",
                fontSize = 10.sp,
                color = primary
            )
        }

        if (
            !connection.senderSharing.location &&
            !connection.senderSharing.profile
        ) {
            Text(
                text = "Nothing shared",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddConnectionButton(
    primary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = primary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Rounded.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = primary
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = "Add connection",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = primary
        )
    }
}

@Composable
private fun EmptyConnectionsState(
    primary: Color,
    onAddConnection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(110.dp)
                .border(
                    width = 1.dp,
                    color = primary.copy(alpha = 0.20f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = primary
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "No connections yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Add someone to your trusted circle to start sharing with people you trust.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(primary)
                .clickable(onClick = onAddConnection)
                .padding(
                    horizontal = 22.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Rounded.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "Add a connection",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}