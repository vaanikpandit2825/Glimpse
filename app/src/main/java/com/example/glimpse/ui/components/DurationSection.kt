package com.example.glimpse.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DurationSection(
    modifier: Modifier= Modifier,
    onDurationSelected:(String)->Unit={}
){
    val durations=listOf(
        " 15 min",
        "30 min",
        "1 hour",
        "Until I Leave",
        "Custom"
    )

    var selectedDuration by remember{
        mutableStateOf("30 min")
    }
    Column(
        modifier=Modifier.fillMaxWidth()
    ){
        Text(
            text="How Long?",
            style=MaterialTheme.typography.titleMedium
        )
        FlowRow(
            modifier=Modifier.padding(top=12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        )
        {
            durations.forEach { duration ->
                DurationChip(
                    text = duration,
                    selected = duration == selectedDuration,
                    onClick = {
                        selectedDuration = duration
                        onDurationSelected(duration)
                    }
                )
            }
        }
    }
}
