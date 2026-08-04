package com.example.glimpse.ui.components.BottomSheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.glimpse.ui.components.BottomSheetHeader
import com.example.glimpse.ui.components.CurrentLocationCard
import com.example.glimpse.ui.components.DurationSection
import com.example.glimpse.ui.components.PrimaryActionButton
import com.example.glimpse.ui.theme.GlimpseDimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInBottomSheet(
    onDismiss:()->Unit
){
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(GlimpseDimens.PaddingLarge),
            verticalArrangement = Arrangement.spacedBy(GlimpseDimens.PaddingLarge)
        ) {

            BottomSheetHeader(
                title = "Check In"
            )

            CurrentLocationCard(
                location = "SRM University Library",
                onChangeLocation = { }
            )

            DurationSection()

            OutlinedTextField(
                value = "",
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Add a note (Optional)")
                }
            )

            PrimaryActionButton(
                text = "Share Check-In",
                onClick = {
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                }
            )
        }
    }
}
