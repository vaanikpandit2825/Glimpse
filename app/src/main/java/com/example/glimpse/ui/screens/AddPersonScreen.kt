package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glimpse.ui.theme.OceanBlue
import androidx.compose.material3.MaterialTheme
import com.example.glimpse.firebase.FirebaseRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonScreen(
    navController: NavController
) {

    var showCodeSheet by remember {
        mutableStateOf(false)
    }

    var glimpseCode by remember {
        mutableStateOf("")
    }
    val firebaseRepository =  remember { FirebaseRepository() }

    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outline
    val qrBackground = Color(0xFFE8EEF5)
    val primaryContainer = Color(0xFF0077BE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Go back",
                    tint = primaryContainer
                )
            }

            Text(
                text = "Add Person",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )

            Spacer(
                modifier = Modifier.size(40.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(
                    top = 8.dp,
                    bottom = 32.dp
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Add someone to your circle",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Connect with someone you trust using their Glimpse Code or QR code.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(surface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(192.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(qrBackground),
                    contentAlignment = Alignment.Center
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight()
                                    .background(primaryContainer)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(primaryContainer)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .width(3.dp)
                                    .fillMaxHeight()
                                    .background(primaryContainer)
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(primaryContainer)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .size(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .width(3.dp)
                                    .fillMaxHeight()
                                    .background(primaryContainer)
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(primaryContainer)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .width(3.dp)
                                    .fillMaxHeight()
                                    .background(primaryContainer)
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(primaryContainer)
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.QrCode2,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp),
                            tint = primaryContainer
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .height(2.dp)
                                .background(primaryContainer)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Scan QR Code",
                    fontSize = 22.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Scan their Glimpse QR to connect instantly.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = secondary,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 24.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(outlineVariant)
                )

                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondary
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(outlineVariant)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(surface)
                    .height(60.dp)
                    .clickable {
                        glimpseCode = ""
                        showCodeSheet = true
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(qrBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Keyboard,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = primaryContainer
                    )
                }

                Spacer(
                    modifier = Modifier.width(16.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Enter Glimpse Code",
                        fontSize = 16.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )

                    Text(
                        text = "Enter their unique code manually.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = secondary
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = outlineVariant
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 32.dp,
                        bottom = 16.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = secondary
                    )

                    Spacer(
                        modifier = Modifier.width(15.dp)
                    )

                    Text(
                        text = "Private by design",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = secondary
                    )
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "People can only connect with you using your Glimpse Code or QR code.",
                    modifier = Modifier.widthIn(max = 280.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    color = secondary
                )
            }
        }
    }

    if (showCodeSheet) {

        ModalBottomSheet(
            onDismissRequest = {
                showCodeSheet = false
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp
            ),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFD9DDE2))
                )
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 28.dp
                    )
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Enter Glimpse Code",
                        modifier = Modifier.weight(1f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )

                    IconButton(
                        onClick = {
                            showCodeSheet = false
                        }
                    ) {
                        Text(
                            text = "×",
                            fontSize = 28.sp,
                            color = Color(0xFF59636D)
                        )
                    }
                }

                Text(
                    text = "Enter the 8-character code shared with\nyou to find your connection.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = secondary
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                OutlinedTextField(
                    value = glimpseCode,
                    onValueChange = { value ->
                        glimpseCode = value
                            .uppercase()
                            .filter {
                                it.isLetterOrDigit()
                            }
                            .take(8)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "AB12CD34",
                            color = Color(0xFF7C8792),
                            fontWeight = FontWeight.Medium
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = "${glimpseCode.length}/8",
                            fontSize = 11.sp,
                            color = Color(0xFF7C8792)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OceanBlue,
                        unfocusedBorderColor = Color(0xFF90CAF9),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = {
                        firebaseRepository.findUidByGlimpseId(
                            glimpseId=glimpseCode,
                            onSuccess = {
                                receiverUid ->
                                if(receiverUid==null){
                                    return@findUidByGlimpseId
                                }
                                showCodeSheet=false

                                navController.navigate(
                                    "connectionRequest/\$receiverUid"
                                )
                            },
                            onFailure = {

                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = glimpseCode.length == 8,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}