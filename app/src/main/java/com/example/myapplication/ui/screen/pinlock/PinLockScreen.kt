package com.example.myapplication.ui.screen.pinlock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.component.PinDotIndicator
import com.example.myapplication.ui.component.PinKeypad

@Composable
fun PinLockScreen(viewModel: PinLockViewModel, onUnlocked: () -> Unit) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.unlocked) {
        if (state.unlocked) onUnlocked()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = when (state.mode) {
                PinLockViewModel.Mode.SET_PIN -> "Set PIN Code"
                PinLockViewModel.Mode.CONFIRM_PIN -> "Confirm PIN Code"
                PinLockViewModel.Mode.ENTER_PIN -> "Enter PIN Code"
            },
            style = MaterialTheme.typography.headlineMedium,
        )
        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(48.dp))
        PinDotIndicator(count = state.pin.length, hasError = state.error != null)
        Spacer(modifier = Modifier.height(48.dp))
        PinKeypad(onDigit = viewModel::onDigit, onDelete = viewModel::onDelete)
    }
}
