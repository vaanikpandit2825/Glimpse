package com.example.glimpse.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp

@Composable
fun CurrentLocationCard(
    location:String,
    onChangeLocation:()->Unit
){
    Card(
        modifier= Modifier
            .fillMaxWidth()
            .clickable { onChangeLocation() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation= CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ){
          Row(
              modifier= Modifier
                  .fillMaxWidth()
                  .padding(18.dp),
              verticalAlignment = Alignment.CenterVertically
          ){
              Icon(
                  imageVector = Icons.Rounded.LocationOn,
                  contentDescription = null,
                  tint=MaterialTheme.colorScheme.primary
              )
              Column(
                  modifier = Modifier
                      .padding(start=16.dp)
                      .weight(1f),
                  verticalArrangement = Arrangement.spacedBy(2.dp)
              ){
                  Text(
                      text="Current Location",
                      style=MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                      text=location,
                      style = MaterialTheme.typography.titleMedium
                  )
                  Row(
                      verticalAlignment = Alignment.CenterVertically
                  ){
                      Text(
                          text="Change",
                          style = MaterialTheme.typography.labelLarge,
                          color = MaterialTheme.colorScheme.primary
                      )
                      Icon(
                          imageVector = Icons.Rounded.ChevronRight,
                          contentDescription = null,
                          tint= MaterialTheme.colorScheme.primary
                      )
                  }
              }
          }
    }
}