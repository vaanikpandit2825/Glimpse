package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import coil.compose.AsyncImage
import com.example.glimpse.connection.ConnectionRequestViewModel
import com.example.glimpse.groups.GroupsViewModel
import com.example.glimpse.model.ConnectionRequest

private val GlimpseBlue = Color(0xFF0077BE)

@Composable
fun AddPeopleToGroupScreen(
    groupId: String,
    onBack: () -> Unit,
    onPeopleAdded: () -> Unit,
    connectionViewModel: ConnectionRequestViewModel = viewModel(),
    groupsViewModel: GroupsViewModel = viewModel()
) {
    var connections by remember {
        mutableStateOf<List<ConnectionRequest>>(emptyList())
    }

    var selectedUids by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isAdding by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        connectionViewModel.getConnections(
            onResult = {
                connections = it
                isLoading = false
                errorMessage = ""
            },
            onFailure = {
                isLoading = false
                errorMessage = it.message ?: "Failed to load connections"
            }
        )
    }

    val filteredConnections = connections.filter {
        searchQuery.isBlank() ||
                it.name.contains(
                    searchQuery,
                    ignoreCase = true
                )
    }

    fun toggleSelection(uid: String) {
        selectedUids =
            if (uid in selectedUids) {
                selectedUids - uid
            } else {
                selectedUids + uid
            }
    }

    fun addSelectedPeople() {
        if (selectedUids.isEmpty() || isAdding) return

        isAdding = true
        errorMessage = ""

        val selected = selectedUids.toList()
        var completed = 0
        var failed = false

        selected.forEach { uid ->
            groupsViewModel.addMemberToGroup(
                uid = uid,
                groupId = groupId,
                onSuccess = {
                    if (failed) return@addMemberToGroup

                    completed++

                    if (completed == selected.size) {
                        isAdding = false
                        onPeopleAdded()
                    }
                },
                onFailure = {
                    if (failed) return@addMemberToGroup

                    failed = true
                    isAdding = false
                    errorMessage =
                        it.message ?: "Failed to add people"
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FBFC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF17212B)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Add people",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF17212B)
                )

                Text(
                    text = "Choose people to add to your group",
                    fontSize = 11.sp,
                    color = GlimpseBlue
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 8.dp
                ),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Search people",
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = GlimpseBlue
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GlimpseBlue,
                unfocusedBorderColor = Color(0xFFD8E0E5)
            )
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 8.dp
                ),
                color = Color(0xFFD32F2F),
                fontSize = 13.sp
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GlimpseBlue
                    )
                }
            }

            connections.isEmpty() -> {
                EmptyGroupPeopleState()
            }

            filteredConnections.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No people found",
                        fontSize = 15.sp,
                        color = Color(0xFF71808A)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = 18.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "${connections.size} connections",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF71808A),
                            modifier = Modifier.padding(
                                bottom = 2.dp
                            )
                        )
                    }

                    items(
                        items = filteredConnections,
                        key = { it.senderUid }
                    ) { connection ->

                        PersonSelectionItem(
                            connection = connection,
                            selected =
                                connection.senderUid in selectedUids,
                            onClick = {
                                toggleSelection(
                                    connection.senderUid
                                )
                            }
                        )
                    }
                }
            }
        }

        if (!isLoading && connections.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 18.dp,
                        vertical = 14.dp
                    )
            ) {
                Text(
                    text = if (selectedUids.isEmpty()) {
                        "Select people to continue"
                    } else {
                        "${selectedUids.size} selected"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF71808A),
                    modifier = Modifier.padding(
                        bottom = 8.dp
                    )
                )

                Button(
                    onClick = {
                        addSelectedPeople()
                    },
                    enabled =
                        selectedUids.isNotEmpty() &&
                                !isAdding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlimpseBlue,
                        disabledContainerColor = Color(0xFFD5DEE4)
                    )
                ) {
                    if (isAdding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text =
                                if (selectedUids.isEmpty()) {
                                    "Add people"
                                } else {
                                    "Add people (${selectedUids.size})"
                                },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonSelectionItem(
    connection: ConnectionRequest,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) {
                    GlimpseBlue.copy(alpha = 0.06f)
                } else {
                    Color.White
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    GlimpseBlue.copy(alpha = 0.35f)
                } else {
                    Color(0xFFE2E7EA)
                },
                shape = RoundedCornerShape(16.dp)
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
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE1F0FA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    modifier = Modifier.size(27.dp),
                    tint = GlimpseBlue
                )
            }
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = connection.name.ifEmpty {
                    "Unknown user"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF17212B)
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = "Connected",
                fontSize = 11.sp,
                color = Color(0xFF71808A)
            )
        }

        Box(
            modifier = Modifier
                .size(25.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        GlimpseBlue
                    } else {
                        Color.Transparent
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = if (selected) {
                        GlimpseBlue
                    } else {
                        Color(0xFFB7C2C9)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun EmptyGroupPeopleState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .border(
                    width = 1.dp,
                    color = GlimpseBlue.copy(alpha = 0.20f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = GlimpseBlue
            )
        }

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        Text(
            text = "No connections yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF17212B)
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Connect with people on GLIMPSE before adding them to a group.",
            fontSize = 13.sp,
            color = Color(0xFF71808A)
        )
    }
}