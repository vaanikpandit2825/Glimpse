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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimpse.model.GlimpseGroup
import com.example.glimpse.model.GroupMember
import com.example.glimpse.groups.GroupsViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

private val GlimpseBlue = Color(0xFF0077BE)
private val Background = Color(0xFFF7F9FA)
private val TextPrimary = Color(0xFF17202A)
private val TextSecondary = Color(0xFF6F7D89)

@Composable
fun CreateGroupScreen(
    onBack: () -> Unit,
    onGroupCreated: (GlimpseGroup) -> Unit,
    groupsViewModel: GroupsViewModel
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    var groupName by remember {
        mutableStateOf("")
    }

    var selectedType by remember {
        mutableStateOf("Friends")
    }

    var isCreating by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val groupTypes = listOf(
        GroupTypeOption(
            type = "Family",
            icon = Icons.Default.FamilyRestroom
        ),
        GroupTypeOption(
            type = "Friends",
            icon = Icons.Default.Groups
        ),
        GroupTypeOption(
            type = "College",
            icon = Icons.Default.School
        ),
        GroupTypeOption(
            type = "Work",
            icon = Icons.Default.BusinessCenter
        ),
        GroupTypeOption(
            type = "Trips",
            icon = Icons.Default.CardTravel
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 14.dp,
                        bottom = 20.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .clickable {
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.width(14.dp)
                )

                Text(
                    text = "Create group",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Group name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = groupName,
                    onValueChange = {
                        groupName = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Group name",
                            color = TextSecondary.copy(alpha = 0.55f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Type",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GroupTypeCard(
                            option = groupTypes[0],
                            selected = selectedType == groupTypes[0].type,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedType = groupTypes[0].type
                            }
                        )

                        GroupTypeCard(
                            option = groupTypes[1],
                            selected = selectedType == groupTypes[1].type,
                            modifier = Modifier.weight(1.45f),
                            onClick = {
                                selectedType = groupTypes[1].type
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GroupTypeCard(
                            option = groupTypes[2],
                            selected = selectedType == groupTypes[2].type,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedType = groupTypes[2].type
                            }
                        )

                        GroupTypeCard(
                            option = groupTypes[3],
                            selected = selectedType == groupTypes[3].type,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedType = groupTypes[3].type
                            }
                        )

                        GroupTypeCard(
                            option = groupTypes[4],
                            selected = selectedType == groupTypes[4].type,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedType = groupTypes[4].type
                            }
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 18.dp)
                    .height(58.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        if (groupName.trim().isNotEmpty() && !isCreating) {
                            GlimpseBlue
                        } else {
                            Color(0xFFB8C7D1)
                        }
                    )
                    .clickable(
                        enabled = groupName.trim().isNotEmpty() && !isCreating
                    ) {
                        val uid = currentUser?.uid

                        if (uid == null) {
                            errorMessage = "Please log in to create a group."
                            return@clickable
                        }

                        isCreating = true
                        errorMessage = null

                        val group = GlimpseGroup(
                            id = UUID.randomUUID().toString(),
                            name = groupName.trim(),
                            type = selectedType,
                            createdBy = uid,
                            createdAt = System.currentTimeMillis(),
                            members = mapOf(
                                uid to true
                            )
                        )

                        groupsViewModel.createGroup(
                            group = group,
                            onSuccess = {
                                isCreating = false
                                onGroupCreated(group)
                            },
                            onFailure = { exception ->
                                isCreating = false
                                errorMessage =
                                    exception.message
                                        ?: "Unable to create group."
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(23.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Create group",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(
                                    Color.White.copy(alpha = 0.18f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class GroupTypeOption(
    val type: String,
    val icon: ImageVector
)

@Composable
private fun GroupTypeCard(
    option: GroupTypeOption,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) {
                    Color(0xFFE5F2FC)
                } else {
                    Color.White.copy(alpha = 0.7f)
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    GlimpseBlue
                } else {
                    Color(0xFFE4E9ED)
                },
                shape = RoundedCornerShape(18.dp)
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.type,
                tint = if (selected) {
                    GlimpseBlue
                } else {
                    Color(0xFF45627D)
                },
                modifier = Modifier.size(25.dp)
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = option.type,
                fontSize = 13.sp,
                fontWeight = if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Medium
                },
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}