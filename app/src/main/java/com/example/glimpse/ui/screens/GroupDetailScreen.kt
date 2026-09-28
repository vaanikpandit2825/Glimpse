package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.glimpse.groups.GroupsViewModel
import com.example.glimpse.model.GlimpseGroup
import com.example.glimpse.model.GroupMember
import com.google.firebase.auth.FirebaseAuth


private val GlimpseBlue = Color(0xFF0077BE)
private val Background = Color(0xFFF7F9FA)
private val TextPrimary = Color(0xFF17202A)
private val TextSecondary = Color(0xFF6F7D89)

@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit,
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
                    tint = TextPrimary
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group?.name ?: "Group",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    text = group?.type ?: "",
                    fontSize = 11.sp,
                    color = GlimpseBlue
                )
            }

            IconButton(
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Group options",
                    tint = TextPrimary
                )
            }
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
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 18.dp,
                        end = 18.dp,
                        top = 12.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        GroupHeader(
                            group = group!!,
                            memberCount = members.size
                        )
                    }

                    item {
                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = "Members",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text = "${members.size} people in this group",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    items(
                        items = members,
                        key = { it.uid }
                    ) { member ->
                        GroupMemberItem(
                            member = member,
                            isCreator =
                                member.uid == group!!.createdBy
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(
    group: GlimpseGroup,
    memberCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE3E8EC),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(22.dp)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFE5F2FC)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.People,
                contentDescription = null,
                modifier = Modifier.size(31.dp),
                tint = GlimpseBlue
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = group.name,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = group.type,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = GlimpseBlue
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.People,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = TextSecondary
            )

            Spacer(
                modifier = Modifier.width(7.dp)
            )

            Text(
                text = "$memberCount members",
                fontSize = 13.sp,
                color = TextSecondary
            )
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
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE3E8EC),
                shape = RoundedCornerShape(16.dp)
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
    }
}