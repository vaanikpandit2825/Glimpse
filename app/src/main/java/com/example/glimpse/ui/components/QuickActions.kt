package com.example.glimpse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.glimpse.ui.theme.OceanBlue

@Composable
fun QuickActions(
    modifier: Modifier = Modifier,
    onCheckInClick: () -> Unit = {},
    onSOSClick: () -> Unit = {},
    onDrivingClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        QuickActionChip(
            text = "Check In",
            icon = Icons.Rounded.Place,
            contentColor = OceanBlue,
            onClick = onCheckInClick
        )

        QuickActionChip(
            text = "SOS",
            icon = Icons.Rounded.Warning,
            containerColor = Color(0xFFFFEAEA),
            contentColor = Color(0xFFD32F2F),
            onClick = onSOSClick
        )

        QuickActionChip(
            text = "Driving",
            icon = Icons.Rounded.DirectionsCar,
            contentColor = OceanBlue,
            onClick = onDrivingClick
        )
    }
}