package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.glimpse.groups.GroupsViewModel
import com.example.glimpse.model.GlimpseGroup
import com.example.glimpse.model.GroupMember
import com.google.firebase.auth.FirebaseAuth

private val GlimpseBlue = Color(0xFF0077BE)
private val Background = Color(0xFFF7F9FA)
private val TextPrimary = Color(0xFF17202A)
private val TextSecondary = Color(0xFF71808A)
private val CardBorder = Color(0xFFE5EAEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit,
    onAddPeople: () -> Unit,
    groupsViewModel: GroupsViewModel = viewModel()
) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    var group by remember {
        mutableStateOf<GlimpseGroup?>(null)
    }

    var members by remember {
        mutableStateOf<List<GroupMember>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var showOptions by remember {
        mutableStateOf(false)
    }

    var showRemoveMemberSheet by remember {
        mutableStateOf(false)
    }

    var memberToRemove by remember {
        mutableStateOf<GroupMember?>(null)
    }

    var showLeaveDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var actionLoading by remember {
        mutableStateOf(false)
    }

    val isCreator = currentUid != null &&
            currentUid == group?.createdBy

    LaunchedEffect(groupId) {
        currentUid?.let { uid ->
            groupsViewModel.getGroup(
                uid = uid,
                onSuccess = { groups ->
                    val selectedGroup =
                        groups.firstOrNull {
                            it.id == groupId
                        }

                    if (selectedGroup == null) {
                        isLoading = false
                        errorMessage = "Group not found"
                        return@getGroup
                    }

                    group = selectedGroup

                    groupsViewModel.getGroupMembers(
                        memberIds = selectedGroup.members.keys.toList(),
                        onSuccess = {
                            members = it
                            isLoading = false
                        },
                        onFailure = {
                            isLoading = false
                            errorMessage =
                                it.message ?: "Failed to load members"
                        }
                    )
                },
                onFailure = {
                    isLoading = false
                    errorMessage =
                        it.message ?: "Failed to load group"
                }
            )
        } ?: run {
            isLoading = false
            errorMessage = "User is not logged in"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        GroupTopBar(
            group = group,
            onBack = onBack,
            onOptions = {
                showOptions = true
            }
        )

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

            errorMessage.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp
                    )
                }
            }

            group != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 18.dp,
                        end = 18.dp,
                        top = 12.dp,
                        bottom = 30.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        GroupOverview(
                            group = group!!,
                            members = members
                        )
                    }

                    item {
                        Column {
                            Text(
                                text = "Members",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(
                                modifier = Modifier.height(3.dp)
                            )

                            Text(
                                text = "${members.size} people in this group",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    items(
                        items = members,
                        key = { it.uid }
                    ) { member ->
                        GroupMemberItem(
                            member = member,
                            isCreator = member.uid == group!!.createdBy
                        )
                    }
                }
            }
        }
    }

    if (showOptions && group != null) {
        GroupOptionsSheet(
            isCreator = isCreator,
            onDismiss = {
                showOptions = false
            },
            onAddPeople = {
                showOptions = false
                onAddPeople()
            },
            onRemoveMember = {
                showOptions = false
                showRemoveMemberSheet = true
            },
            onLeaveGroup = {
                showOptions = false
                showLeaveDialog = true
            },
            onDeleteGroup = {
                showOptions = false
                showDeleteDialog = true
            },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )
        )
    }

    if (showRemoveMemberSheet) {
        RemoveMemberSheet(
            members = members.filter {
                it.uid != currentUid
            },
            onDismiss = {
                showRemoveMemberSheet = false
            },
            onMemberSelected = { member ->
                showRemoveMemberSheet = false
                memberToRemove = member
            }
        )
    }

    memberToRemove?.let { member ->
        AlertDialog(
            onDismissRequest = {
                if (!actionLoading) {
                    memberToRemove = null
                }
            },
            title = {
                Text("Remove member?")
            },
            text = {
                Text(
                    "Remove ${member.name.ifEmpty { "this member" }} from the group?"
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !actionLoading,
                    onClick = {
                        val uid = currentUid ?: return@TextButton

                        actionLoading = true

                        groupsViewModel.removeMemberFromGroup(
                            uid = member.uid,
                            groupId = groupId,
                            onSuccess = {
                                members = members.filter {
                                    it.uid != member.uid
                                }

                                actionLoading = false
                                memberToRemove = null
                            },
                            onFailure = {
                                actionLoading = false
                                errorMessage =
                                    it.message ?: "Failed to remove member"
                            }
                        )
                    }
                ) {
                    Text(
                        text = if (actionLoading) {
                            "Removing..."
                        } else {
                            "Remove"
                        },
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !actionLoading,
                    onClick = {
                        memberToRemove = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!actionLoading) {
                    showLeaveDialog = false
                }
            },
            title = {
                Text("Leave group?")
            },
            text = {
                Text(
                    "You will no longer be a member of this group."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !actionLoading,
                    onClick = {
                        val uid = currentUid ?: return@TextButton

                        actionLoading = true

                        groupsViewModel.removeMemberFromGroup(
                            uid = uid,
                            groupId = groupId,
                            onSuccess = {
                                actionLoading = false
                                showLeaveDialog = false
                                onBack()
                            },
                            onFailure = {
                                actionLoading = false
                                errorMessage =
                                    it.message ?: "Failed to leave group"
                            }
                        )
                    }
                ) {
                    Text(
                        text = if (actionLoading) {
                            "Leaving..."
                        } else {
                            "Leave"
                        },
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !actionLoading,
                    onClick = {
                        showLeaveDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!actionLoading) {
                    showDeleteDialog = false
                }
            },
            title = {
                Text("Delete group?")
            },
            text = {
                Text(
                    "This will permanently delete the group for everyone."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !actionLoading,
                    onClick = {
                        actionLoading = true

                        groupsViewModel.deleteGroup(
                            groupId = groupId,
                            onSuccess = {
                                actionLoading = false
                                showDeleteDialog = false
                                onBack()
                            },
                            onFailure = {
                                actionLoading = false
                                errorMessage =
                                    it.message ?: "Failed to delete group"
                            }
                        )
                    }
                ) {
                    Text(
                        text = if (actionLoading) {
                            "Deleting..."
                        } else {
                            "Delete"
                        },
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !actionLoading,
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemoveMemberSheet(
    members: List<GroupMember>,
    onDismiss: () -> Unit,
    onMemberSelected: (GroupMember) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 28.dp
                )
        ) {
            Text(
                text = "Remove member",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Choose someone to remove from this group.",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            if (members.isEmpty()) {
                Text(
                    text = "There are no other members to remove.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(
                        vertical = 20.dp
                    )
                )
            } else {
                members.forEach { member ->
                    RemoveMemberItem(
                        member = member,
                        onClick = {
                            onMemberSelected(member)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoveMemberItem(
    member: GroupMember,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF7F9FA))
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(50.dp)
        ) {
            if (member.profilePhotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = member.profilePhotoUrl,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE1F0FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp),
                        tint = GlimpseBlue
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Text(
            text = member.name.ifEmpty {
                "Unknown user"
            },
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }

    Spacer(
        modifier = Modifier.height(8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupOptionsSheet(
    isCreator: Boolean,
    onDismiss: () -> Unit,
    onAddPeople: () -> Unit,
    onRemoveMember: () -> Unit,
    onLeaveGroup: () -> Unit,
    onDeleteGroup: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 28.dp
                )
        ) {
            Text(
                text = "Group options",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            GroupOptionItem(
                icon = Icons.Rounded.GroupAdd,
                title = "Add people",
                onClick = onAddPeople
            )

            if (isCreator) {
                GroupOptionItem(
                    icon = Icons.Rounded.RemoveCircleOutline,
                    title = "Remove member",
                    onClick = onRemoveMember
                )

                GroupOptionItem(
                    icon = Icons.Rounded.DeleteOutline,
                    title = "Delete group",
                    destructive = true,
                    onClick = onDeleteGroup
                )
            } else {
                GroupOptionItem(
                    icon = Icons.Rounded.ExitToApp,
                    title = "Leave group",
                    destructive = true,
                    onClick = onLeaveGroup
                )
            }
        }
    }
}

@Composable
private fun GroupOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (destructive) {
                    Color(0xFFFFF5F5)
                } else {
                    Color(0xFFF7F9FA)
                }
            )
            .padding(
                horizontal = 16.dp,
                vertical = 15.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (destructive) {
                    Color(0xFFD32F2F)
                } else {
                    GlimpseBlue
                }
            )
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (destructive) {
                Color(0xFFD32F2F)
            } else {
                TextPrimary
            }
        )
    }
}

@Composable
private fun GroupTopBar(
    group: GlimpseGroup?,
    onBack: () -> Unit,
    onOptions: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = group?.name ?: "Group",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )

            if (!group?.type.isNullOrEmpty()) {
                Text(
                    text = group?.type ?: "",
                    fontSize = 11.sp,
                    color = GlimpseBlue
                )
            }
        }

        IconButton(
            onClick = onOptions,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Group options",
                tint = TextPrimary
            )
        }
    }
}

@Composable
private fun GroupOverview(
    group: GlimpseGroup,
    members: List<GroupMember>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = CardBorder,
                shape = RoundedCornerShape(26.dp)
            )
            .padding(22.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(Color(0xFFE5F2FC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.People,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = GlimpseBlue
                )
            }

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group.name,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = group.type,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlimpseBlue
                )
            }
        }

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemberAvatarStack(
                members = members
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column {
                Text(
                    text = "${members.size} members",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    text = "Your GLIMPSE circle",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MemberAvatarStack(
    members: List<GroupMember>
) {
    val visibleMembers = members.take(4)

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleMembers.forEachIndexed { index, member ->
            Box(
                modifier = Modifier
                    .padding(
                        start = if (index == 0) {
                            0.dp
                        } else {
                            (-8).dp
                        }
                    )
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
            ) {
                if (member.profilePhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = member.profilePhotoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE1F0FA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = GlimpseBlue
                        )
                    }
                }
            }
        }

        if (members.size > 4) {
            Box(
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAF3F8)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${members.size - 4}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlimpseBlue
                )
            }
        }
    }
}

@Composable
private fun GroupMemberItem(
    member: GroupMember,
    isCreator: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = CardBorder,
                shape = RoundedCornerShape(17.dp)
            )
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (member.profilePhotoUrl.isNotEmpty()) {
            AsyncImage(
                model = member.profilePhotoUrl,
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
                text = member.name.ifEmpty {
                    "Unknown user"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = if (isCreator) {
                    "Group creator"
                } else {
                    "Member"
                },
                fontSize = 11.sp,
                color = if (isCreator) {
                    GlimpseBlue
                } else {
                    TextSecondary
                }
            )
        }

        if (isCreator) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE5F2FC))
                    .padding(
                        horizontal = 8.dp,
                        vertical = 5.dp
                    )
            ) {
                Text(
                    text = "Creator",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlimpseBlue
                )
            }
        }
    }
}