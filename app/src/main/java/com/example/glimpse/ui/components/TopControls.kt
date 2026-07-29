package com.example.glimpse.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import com.example.glimpse.ui.theme.OceanBlue
import com.example.glimpse.ui.theme.TextPrimary
import androidx.compose.material.icons.outlined.ChatBubbleOutline

@Composable
private fun FloatingIconButton(
    icon: @Composable () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        IconButton(
            onClick = {}
        ) {
            icon()
        }
    }
}

@Composable
fun TopControls(
    modifier:Modifier=Modifier
)
    {
        Row(
            modifier=modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement= Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            FloatingIconButton(
                icon={
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings"
                    )
                }
            )
            Card(
                shape= RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation=6.dp
                ),
                colors= CardDefaults.cardColors(
                    containerColor=Color.White
                )
            ){
                  Row(
                      modifier=Modifier
                          .padding(horizontal=18.dp,vertical=10.dp),
                          verticalAlignment = Alignment.CenterVertically
                  ){
                      Text(
                      text="Friends",
                      style=MaterialTheme.typography.titleMedium,
                      color=TextPrimary
                      )
                      Spacer(modifier=Modifier.width(4.dp))

                      Icon(
                          imageVector=Icons.Rounded.KeyboardArrowDown,
                          contentDescription = null,
                          tint=OceanBlue
                      )
                  }
            }
            FloatingIconButton(
                icon={
                    Icon(
                        imageVector=Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Messages"
                    )
                }
            )
        }
    }

