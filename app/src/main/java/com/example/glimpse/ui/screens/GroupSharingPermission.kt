package com.example.glimpse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glimpse.model.SharingPermissions
import com.example.glimpse.groups.GroupsViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.ExperimentalMaterial3Api

@androidx.compose.runtime.Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GroupSharingPermissionsScreen(
    groupId: String,
    onBack: () -> Unit,
    groupsViewModel: GroupsViewModel = viewModel()
) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    var location by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf(true) }
    var locationHistory by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(groupId, currentUid) {
        if (currentUid == null) {
            errorMessage = "Unable to identify your account."
            isLoading = false
            return@LaunchedEffect
        }

        groupsViewModel.getGroupSharingPermissions(
            groupId = groupId,
            uid = currentUid,
            onSuccess = { permissions ->
                location = permissions.location
                profile = permissions.profile
                locationHistory = permissions.locationHistory
                isLoading = false
            },
            onFailure = {
                errorMessage = it.message ?: "Failed to load sharing permissions."
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sharing Permissions",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {

                Text(
                    text = "What you share with this group",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose what members of this group can access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                SharingPermissionItem(
                    title = "Current location",
                    description = "Allow this group to see your current location.",
                    checked = location,
                    onCheckedChange = { location = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SharingPermissionItem(
                    title = "Name & profile photo",
                    description = "Allow this group to see your name and profile photo.",
                    checked = profile,
                    onCheckedChange = { profile = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SharingPermissionItem(
                    title = "Previous locations",
                    description = "Allow this group to access your location history.",
                    checked = locationHistory,
                    onCheckedChange = { locationHistory = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (currentUid == null) {
                            errorMessage = "Unable to identify your account."
                            return@Button
                        }

                        isSaving = true
                        errorMessage = null

                        groupsViewModel.updateGroupSharingPermissions(
                            groupId = groupId,
                            uid = currentUid,
                            permissions = SharingPermissions(
                                location = location,
                                profile = profile,
                                locationHistory = locationHistory
                            ),
                            onSuccess = {
                                isSaving = false
                                onBack()
                            },
                            onFailure = {
                                isSaving = false
                                errorMessage =
                                    it.message ?: "Failed to save sharing permissions."
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save changes")
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun SharingPermissionItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}