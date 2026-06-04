package com.example.myapplication.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.component.PinDotIndicator
import com.example.myapplication.ui.component.PinKeypad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.success) { if (state.success) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change PIN") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                when (state.step) {
                    SettingsViewModel.Step.CURRENT -> "Enter Current PIN"
                    SettingsViewModel.Step.NEW -> "Enter New PIN"
                    SettingsViewModel.Step.CONFIRM -> "Confirm New PIN"
                },
                style = MaterialTheme.typography.headlineSmall,
            )
            if (state.error != null) { Spacer(Modifier.height(8.dp)); Text(state.error!!, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(32.dp))
            PinDotIndicator(
                when (state.step) {
                    SettingsViewModel.Step.CURRENT -> state.currentPin.length
                    SettingsViewModel.Step.NEW -> state.newPin.length
                    SettingsViewModel.Step.CONFIRM -> state.confirmPin.length
                }
            )
            Spacer(Modifier.height(32.dp))
            PinKeypad(onDigit = viewModel::onDigit, onDelete = viewModel::onDelete)
        }
    }
}
