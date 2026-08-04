package com.example.glimpse.ui.components

import android.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.glimpse.ui.theme.GlimpseDimens
import com.example.glimpse.ui.theme.OceanBlue

@Composable
fun PrimaryActionButton(
    modifier: Modifier=Modifier,
    text: String,
    enabled:Boolean=true,
    onClick:()->Unit
    )
        {
            Button(
                onClick=onClick,
                modifier= Modifier
                    .fillMaxWidth()
                    .height(GlimpseDimens.ButtonHeight),
                enabled=enabled,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OceanBlue
                )
            ){
              Text(
                  text=text,
                  style= MaterialTheme.typography.titleMedium
              )
                Icon(
                    imageVector=Icons.Rounded.ArrowForward,
                    contentDescription = null
                )
            }
    }