package com.example.glimpse.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Boy
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glimpse.firebase.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import java.security.KeyStore
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.glimpse.cloudinary.CloudinaryRepository
import androidx.activity.result.PickVisualMediaRequest
import com.example.glimpse.ui.theme.OceanBlue

@Composable
fun EditProfileScreen(
    navController: NavController
){
    var username by remember { mutableStateOf("") }
    val firebaseRepository=remember { FirebaseRepository() }
    val cloudinaryRepository=remember{CloudinaryRepository()}
    var profilePhotoUrl by remember { mutableStateOf("") }
    val currentUser= FirebaseAuth.getInstance().currentUser

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->

        if (uri != null) {

            cloudinaryRepository.uploadProfilePhoto(
                imageUri = uri,

                onSuccess = { secureUrl ->

                    val uid = currentUser?.uid

                    if (uid != null) {

                        firebaseRepository.updateProfilePhoto(
                            uid = uid,
                            photoUrl = secureUrl,

                            onSuccess = {
                                profilePhotoUrl = secureUrl

                                Log.d(
                                    "PROFILE",
                                    "Profile photo updated"
                                )
                            },

                            onFailure = { error ->
                                Log.e(
                                    "PROFILE",
                                    "Failed to save profile photo",
                                    error
                                )
                            }
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
                username = name
                profilePhotoUrl=photoUrl
            },
            onFailure={ }
        )
    }
    Column(
        modifier=Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FA))
    ) {
        Row(
            modifier= Modifier
                .fillMaxWidth()
                .padding(
                    start=12.dp,
                    end = 12.dp,
                    top=12.dp
                ),
            verticalAlignment=Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text="Edit Profile",
                fontSize = 18.sp
            )
        }
        Spacer(modifier=Modifier.size(32.dp))


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
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
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                        .background(Color(0xFFE8EEF5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add Photo",
                        color = OceanBlue,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            ) {
                Text(
                    text = "Change Photo",
                    color = OceanBlue
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("Name")
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid

                    if (uid != null) {
                        firebaseRepository.updateProfileName(
                            uid = uid,
                            name = username,
                            onSuccess = {
                                navController.popBackStack()
                            },
                            onFailure = { error ->
                                Log.e(
                                    "PROFILE",
                                    "Failed to update name",
                                    error
                                )
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }

    }
}
