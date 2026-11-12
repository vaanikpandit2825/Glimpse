package com.example.glimpse.ui.screens

import android.webkit.WebSettings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.datatransport.TransportBackend
import org.maplibre.compose.expressions.dsl.image

private val Purple = Color(0xFF4F46E5)
private val PurpleSoft = Color(0xFFEDEBFF)
private val Ink = Color(0xFF111827)
private val Muted = Color(0xFF6B7280)
private val Background = Color(0xFFF8F9FC)
private val CardBorder = Color(0xFFE7E9F0)

@Composable
fun EmergencyContactsScreen(
    onBack: ()->Unit,
    onAddContact:()->Unit
){
    Scaffold(
        containerColor=Background,
        topBar={
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(onClick = onBack){
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint=Ink
                    )
                }
                Column{
                    Text(
                        text="Emergency Contacts",
                        color=Ink,
                        fontSize = 20.sp,
                        fontWeight= FontWeight.Bold
                    )
                    Text (
                        text = "Your trusted emergency circle",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    ){ innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Information banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PurpleSoft)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Only people who accept your invitation can become emergency contacts.",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Ink
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 22.dp,
                            vertical = 36.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(PurpleSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PeopleOutline,
                            contentDescription = null,
                            tint = Purple,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "No emergency contacts yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Choose people you trust to respond when you need help.",
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = Muted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = onAddContact,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "Add Emergency Contact",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }


            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = Purple
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = "How it works",
                            color = Ink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "1. Choose someone from your GLIMPSE connections.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Muted
                    )

                    Text(
                        text = "2. Send an emergency contact invitation.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Muted
                    )

                    Text(
                        text = "3. They must accept before becoming an active emergency contact.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Muted
                    )

                    Text(
                        text = "4. Either person can remove the emergency contact relationship later.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Muted
                    )
                }
            }
        }
    }
}