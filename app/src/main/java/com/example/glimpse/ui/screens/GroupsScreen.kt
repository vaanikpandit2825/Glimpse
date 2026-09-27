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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glimpse.model.GroupsViewModel
import com.example.glimpse.model.GlimpseGroup
import com.google.firebase.auth.FirebaseAuth

private val GlimpseBlue = Color(0xFF0077BE)
private val Background = Color(0xFFF7F9FA)
private val TextPrimary = Color(0xFF17202A)
private val TextSecondary = Color(0xFF6F7D89)

@Composable
fun GroupsScreen(
    onBack: () -> Unit = {},
    onCreateGroup: () -> Unit = {},
    onGroupClick: (GlimpseGroup) -> Unit = {},
    groupsViewModel: GroupsViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    var groupList by remember {
        mutableStateOf<List<GlimpseGroup>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid

        if (uid == null) {
            isLoading = false
            errorMessage = "Please log in to view your groups."
            return@LaunchedEffect
        }

        groupsViewModel.getGroup(
            uid = uid,
            onSuccess = { loadedGroups ->
                groupList = loadedGroups
                isLoading = false
                errorMessage = null
            },
            onFailure = { exception ->
                errorMessage =
                    exception.message ?: "Unable to load groups."
                isLoading = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            GroupsTopBar(
                onBack = onBack,
                onCreateGroup = onCreateGroup
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

                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 32.dp,
                                end = 32.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Something went wrong.",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                groupList.isEmpty() -> {
                    EmptyGroupsState(
                        onCreateGroup = onCreateGroup
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 16.dp,
                                end = 16.dp
                            )
                    ) {
                        Text(
                            text = "Your groups",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(
                                start = 4.dp,
                                bottom = 12.dp
                            )
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                bottom = 100.dp
                            )
                        ) {
                            items(
                                items = groupList,
                                key = { group -> group.id }
                            ) { group ->
                                GroupCard(
                                    group = group,
                                    onClick = {
                                        onGroupClick(group)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!isLoading && groupList.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 20.dp,
                        bottom = 24.dp
                    )
                    .navigationBarsPadding()
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(GlimpseBlue)
                    .clickable {
                        onCreateGroup()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create group",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun GroupsTopBar(
    onBack: () -> Unit,
    onCreateGroup: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
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

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = "Groups",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Organize your connections\nand see them together on the map.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(GlimpseBlue)
                .clickable {
                    onCreateGroup()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create group",
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

@Composable
private fun GroupCard(
    group: GlimpseGroup,
    onClick: () -> Unit
) {
    val groupIcon = when (group.type.lowercase()) {
        "family" -> Icons.Default.FamilyRestroom
        "friends" -> Icons.Default.People
        "college" -> Icons.Default.School
        "work" -> Icons.Default.BusinessCenter
        "trips" -> Icons.Default.CardTravel
        else -> Icons.Default.Groups
    }

    val memberCount = group.members.size

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable {
                onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFE5F0F5),
                            Color(0xFFE8F0E8)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = groupIcon,
                    contentDescription = null,
                    tint = GlimpseBlue,
                    modifier = Modifier.size(25.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = "$memberCount ${
                        if (memberCount == 1) "member" else "members"
                    }",
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                MemberCountPreview(
                    count = memberCount
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Open group",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MemberCountPreview(
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val visibleCount = minOf(count, 4)

        repeat(visibleCount) { index ->
            Box(
                modifier = Modifier
                    .padding(
                        start = if (index == 0) 0.dp else 4.dp
                    )
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        when (index) {
                            0 -> Color(0xFF7A9CAF)
                            1 -> Color(0xFFB78972)
                            2 -> Color(0xFF78937C)
                            else -> Color(0xFF8A7899)
                        }
                    )
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
            )
        }

        if (count > 4) {
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${count - 4}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlimpseBlue
                )
            }
        }
    }
}

@Composable
private fun EmptyGroupsState(
    onCreateGroup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(GlimpseBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text = "Create your first group",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Organize your GLIMPSE connections\ninto groups.",
            fontSize = 15.sp,
            lineHeight = 21.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = Color(0xFFE5E9EC),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            FeatureRow(
                icon = Icons.Default.People,
                text = "Keep your people organized"
            )

            FeatureRow(
                icon = Icons.Default.Groups,
                text = "See groups together on the map"
            )

            FeatureRow(
                icon = Icons.Default.CardTravel,
                text = "Perfect for family, friends, trips and more"
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(GlimpseBlue)
                .clickable {
                    onCreateGroup()
                },
            contentAlignment = Alignment.Center
        ) {
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
                        .size(36.dp)
                        .clip(CircleShape)
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

@Composable
private fun FeatureRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAF5FC)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GlimpseBlue,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(13.dp)
        )

        Text(
            text = text,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}