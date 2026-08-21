package com.example.glimpse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glimpse.ui.theme.OceanBlue

@Composable
fun AddPersonScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FA))
            .padding(horizontal = 32.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
                    contentDescription = "Back",
                    tint = OceanBlue
                )
            }

            Text(
                text = "Add Person",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = OceanBlue
            )

            Spacer(
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Add someone to your circle",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101820)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Connect with someone you trust using their\nGlimpse Code or QR code.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = Color(0xFF607D8B)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(198.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFE8EEF5)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(82.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(22.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(22.dp)
                                .background(OceanBlue)
                        )
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(2.dp)
                                .background(OceanBlue)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(22.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .width(22.dp)
                                .height(2.dp)
                                .background(OceanBlue)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .width(2.dp)
                                .height(22.dp)
                                .background(OceanBlue)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(22.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .width(22.dp)
                                .height(2.dp)
                                .background(OceanBlue)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .width(2.dp)
                                .height(22.dp)
                                .background(OceanBlue)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .width(22.dp)
                                .height(2.dp)
                                .background(OceanBlue)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .width(2.dp)
                                .height(22.dp)
                                .background(OceanBlue)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(52.dp)
                            .height(2.dp)
                            .background(OceanBlue)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Scan QR Code",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF101820)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Scan their Glimpse QR to connect instantly.",
                fontSize = 10.sp,
                color = Color(0xFF607D8B),
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFFD9DEE3))
            )

            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 10.dp),
                fontSize = 9.sp,
                color = Color(0xFF78909C)
            )

            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFFD9DEE3))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFFE8EEF5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Keyboard,
                    contentDescription = "Enter Glimpse Code",
                    modifier = Modifier.size(20.dp),
                    tint = OceanBlue
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Enter Glimpse Code",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF101820)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Enter their unique code manually.",
                    fontSize = 9.sp,
                    color = Color(0xFF607D8B)
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF90A4AE),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔒 Private by design",
                fontSize = 9.sp,
                color = Color(0xFF78909C)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "People can only connect with you using your\nGlimpse Code or QR code.",
                fontSize = 8.sp,
                lineHeight = 11.sp,
                color = Color(0xFF90A4AE),
                textAlign = TextAlign.Center
            )
        }
    }
}