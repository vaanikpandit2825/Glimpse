package com.example.glimpse.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.glimpse.model.Friend

@Composable
fun FriendRow(
    friend:Friend,
    modifier: Modifier=Modifier
){
    Row(
        modifier= Modifier
            .fillMaxWidth()
            .padding(vertical=12.dp),
            verticalAlignment=Alignment.CenterVertically
    ){
         Image(
             painter=painterResource(friend.avatar),
             contentDescription=friend.name,
             modifier=Modifier
             .size(48.dp)
             .clip(CircleShape)
         )
        Spacer(modifier=Modifier.width(16.dp))
        Column(modifier=Modifier.weight(1f)){
            Text(
                text=friend.name,
                style=MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier=Modifier.height(2.dp))

            Text(
                text=friend.lastSeen,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}