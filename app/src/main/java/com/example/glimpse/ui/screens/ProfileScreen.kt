package com.example.glimpse.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.glimpse.cloudinary.CloudinaryRepository
import com.example.glimpse.firebase.FirebaseRepository
import com.example.glimpse.ui.theme.OceanBlue
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    navController: NavController
) {

    var userName by remember { mutableStateOf("") }
    var profilePhotoUrl by remember { mutableStateOf("") }

    val firebaseRepository = remember { FirebaseRepository() }
    val cloudinaryRepository = remember { CloudinaryRepository() }
    val currentUser = FirebaseAuth.getInstance().currentUser

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->

        if (uri != null) {

            cloudinaryRepository.uploadProfilePhoto(
                imageUri = uri,

                onSuccess = { secureUrl ->

                    Log.d(
                        "CLOUDINARY",
                        "Upload successful: $secureUrl"
                    )

                    val uid = currentUser?.uid

                    if (uid != null) {

                        firebaseRepository.updateProfilePhoto(
                            uid = uid,
                            photoUrl = secureUrl,

                            onSuccess = {
                                Log.d(
                                    "PROFILE",
                                    "Profile photo URL saved to Firebase"
                                )

                                profilePhotoUrl = secureUrl
                            },

                            onFailure = { error ->
                                Log.e(
                                    "PROFILE",
                                    "Failed to save profile photo URL",
                                    error
                                )
                            }
                        )

                    } else {

                        Log.e(
                            "PROFILE",
                            "Current user UID is null"
                        )
                    }
                },

                onFailure = { error ->

                    Log.e(
                        "CLOUDINARY",
                        "Image upload failed: $error"
                    )
                }
            )
        }
    }

    LaunchedEffect(Unit) {

        firebaseRepository.getCurrentUserProfile(

            onResult = { name, photoUrl ->

                userName = name
                profilePhotoUrl = photoUrl

                Log.d(
                    "PROFILE",
                    "Name: $name, photo url: $photoUrl"
                )
            },

            onFailure = { error ->

                Log.e(
                    "PROFILE",
                    "Failed to load profile",
                    error
                )
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FA))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {

                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { }
                ) {

                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (profilePhotoUrl.isNotEmpty()) {

                    AsyncImage(
                        model = profilePhotoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        contentScale = ContentScale.Crop
                    )

                } else {

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8EEF5))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userName,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "● Sharing location",
                    fontSize = 13.sp,
                    color = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Text(
                        text = "Edit Profile",
                        color = OceanBlue
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "Your Glimpse",
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Who can currently see your location",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = OceanBlue.copy(alpha = 0.08f)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "Your location is private",
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Nobody can see your location until you choose to share it.",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            TextButton(
                                onClick = { }
                            ) {

                                Text(
                                    text = "Manage sharing →",
                                    color = OceanBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "People You Trust",
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "People you've approved as trusted connections",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8EEF5)),
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = "+",
                                    fontSize = 24.sp,
                                    color = OceanBlue
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = "No trusted people yet",
                                    fontSize = 15.sp
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = "Add someone you trust to start sharing.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            TextButton(
                                onClick = { }
                            ) {

                                Text(
                                    text = "Add",
                                    color = OceanBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Privacy",
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { },
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8EEF5)),
                                contentAlignment = Alignment.Center
                            ) {

                                Icon(
                                    imageVector = Icons.Rounded.Security,
                                    contentDescription = "Privacy and Security",
                                    tint = OceanBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = "Privacy & Security",
                                    fontSize = 15.sp
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = "Control who can access your Glimpse",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = "›",
                                fontSize = 24.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}