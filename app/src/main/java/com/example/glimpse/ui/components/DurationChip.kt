package com.example.glimpse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.glimpse.ui.theme.GlimpseDimens
import com.example.glimpse.ui.theme.OceanBlue
import androidx.compose.ui.unit.dp

@Composable

fun DurationChip(
    text:String,
    onClick:()->Unit,
    selected: Boolean
){
    val backgroundColor by animateColorAsState(
        if(selected) OceanBlue
        else MaterialTheme.colorScheme.surface,
        label="chipBackground"
    )
    val textColor by animateColorAsState(
            if(selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
            label="chipText"
    )
    val borderColor by animateColorAsState(
        if(selected)OceanBlue
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        label="chipBorder"
    )
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .clickable{onClick()}
            .height(GlimpseDimens.ChipHeight),
        color=backgroundColor,
        shape= RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
           Box(
                  modifier = Modifier.padding(horizontal = 18.dp),
               contentAlignment = Alignment.Center
           )
           {
                 Text(
                     text=text,
                     color=textColor,
                     style = MaterialTheme.typography.labelLarge,
                     fontWeight = FontWeight.SemiBold

                 )
           }
    }

}