package com.example.novelreaderapp.ui.screens

import com.example.novelreaderapp.viewmodel.SettingsViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    onTTSStart: () -> Unit,
) {
    // Collect current font size from the ViewModel's StateFlow
    val fontSize by settingsViewModel.fontSize.collectAsState()

    // Collect TTS speaking state (true if currently speaking)
    val isSpeaking by settingsViewModel.isSpeaking.collectAsState()

    // Collect current HTML content to be read by TTS
    val htmlContent by settingsViewModel.htmlContent.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                // You can add navigation icons here if needed
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)  // respect scaffold padding (e.g., status bar)
                .padding(16.dp)          // add standard padding inside the screen
                .fillMaxWidth(),         // fill the width of the screen
            horizontalAlignment = Alignment.Start // left align children
        ) {
            // ===== FONT SIZE SETTING =====
            Text("Font Size: ${fontSize.toInt()}sp")  // display current font size as integer

            Slider(
                value = fontSize,
                onValueChange = { settingsViewModel.setFontSize(it) }, // update ViewModel on slide
                valueRange = 12f..32f,  // limits slider between 12sp and 32sp font size
                steps = 9               // intermediate steps between min and max
            )

            Spacer(modifier = Modifier.height(24.dp)) // space between sections

            // ===== TEXT-TO-SPEECH CONTROL =====
            Text("Text-to-Speech", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = {
                    // Toggle TTS playback with current HTML content
                    settingsViewModel.toggleTTS(htmlContent)

                    // If TTS is not already speaking, trigger the start callback
                    if (!isSpeaking) onTTSStart()
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Button label switches based on TTS speaking state
                Text(if (isSpeaking) "Stop TTS" else "Start TTS")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
