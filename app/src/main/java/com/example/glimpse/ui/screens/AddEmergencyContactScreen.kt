
package com.example.glimpse.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.glimpse.firebase.FirebaseRepository
import com.example.glimpse.model.ConnectionRequest
import com.google.firebase.auth.FirebaseAuth

private val EmergencyPurple = Color(0xFF4F46E5)
private val EmergencyPurpleSoft = Color(0xFFEDEBFF)
private val EmergencyInk = Color(0xFF111827)
private val EmergencyMuted = Color(0xFF6B7280)
private val EmergencyBackground = Color(0xFFF8F9FC)
private val EmergencyBorder = Color(0xFFE7E9F0)

@Composable
fun AddEmergencyContactScreen(
    onBack: () -> Unit
) {
    val repository = remember { FirebaseRepository() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var searchQuery by remember { mutableStateOf("") }
    val selectedContactUids=remember{ mutableStateListOf<String>()}
    var connections by remember {
        mutableStateOf<List<ConnectionRequest>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showInviteNotice by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid == null) {
            isLoading = false
            errorMessage = "Please sign in to view your connections."
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = null

        repository.getConnections(
            uid = uid,
            onResult = { result ->
                connections = result
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error.localizedMessage
                    ?: "Could not load your connections."
                isLoading = false
            }
        )
    }

    val filteredConnections = connections.filter {
        it.name.contains(searchQuery.trim(), ignoreCase = true)
    }

    Scaffold(
        containerColor = EmergencyBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmergencyBackground)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = EmergencyInk
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Add Emergency Contact",
                        color = EmergencyInk,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Choose from your GLIMPSE connections",
                        color = EmergencyMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(Modifier.height(4.dp))

            // Information banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EmergencyPurpleSoft)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = EmergencyPurple,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Emergency contacts must accept your invitation before they can receive SOS alerts.",
                    color = EmergencyInk,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search connections")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmergencyPurple,
                    unfocusedBorderColor = EmergencyBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Text(
                text = "YOUR CONNECTIONS",
                color = EmergencyMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = EmergencyPurple
                        )
                    }
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                filteredConnections.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PeopleOutline,
                            contentDescription = null,
                            tint = EmergencyPurple,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = if (searchQuery.isBlank()) {
                                "No connections yet"
                            } else {
                                "No matching connections"
                            },
                            color = EmergencyInk,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = if (searchQuery.isBlank()) {
                                "Connect with someone in GLIMPSE before adding them as an emergency contact."
                            } else {
                                "Try searching with a different name."
                            },
                            color = EmergencyMuted,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = filteredConnections,
                            key = { it.senderUid }
                        ) { connection ->

                            EmergencyConnectionCard(
                                connection = connection,
                                isSelected = selectedContactUids.contains(connection.senderUid),
                                onSelectionChange = { selected ->
                                    if (selected) {
                                        if (!selectedContactUids.contains(connection.senderUid)) {
                                            selectedContactUids.add(connection.senderUid)
                                        }
                                    } else {
                                        selectedContactUids.remove(connection.senderUid)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    if (showInviteNotice) {
        AlertDialog(
            onDismissRequest = {
                showInviteNotice = false
            },
            title = {
                Text("Invitations coming next")
            },
            text = {
                Text(
                    "Your connections are ready to select. Sending emergency-contact invitations will be enabled after we add the secure Firebase invitation and acceptance flow."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showInviteNotice = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
@Composable
private fun EmergencyConnectionCard(
    connection: ConnectionRequest,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, EmergencyBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(EmergencyPurpleSoft),
                contentAlignment = Alignment.Center
            ) {
                if (connection.profilePhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = connection.profilePhotoUrl,
                        contentDescription = connection.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = connection.name
                            .firstOrNull()
                            ?.uppercase()
                            ?: "?",
                        color = EmergencyPurple,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connection.name.ifBlank { "GLIMPSE User" },
                    color = EmergencyInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Connected",
                    color = EmergencyMuted,
                    fontSize = 12.sp
                )
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = EmergencyPurple,
                    uncheckedColor = EmergencyMuted,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
