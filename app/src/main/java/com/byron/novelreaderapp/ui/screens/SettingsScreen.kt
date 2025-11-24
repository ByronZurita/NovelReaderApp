package com.byron.novelreaderapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.byron.novelreaderapp.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    onTTSStart: () -> Unit,
) {
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val isSpeaking by settingsViewModel.isSpeaking.collectAsState()
    val htmlContent by settingsViewModel.htmlContent.collectAsState()
    val selectedTheme by settingsViewModel.selectedTheme.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // ============================
            //       FONT SIZE SECTION
            // ============================
            SettingsCard(
                icon = Icons.Default.FontDownload,
                title = "Font Size",
            ) {
                Text(
                    "${fontSize.toInt()}sp",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Slider(
                    value = fontSize,
                    onValueChange = { settingsViewModel.setFontSize(it) },
                    valueRange = 12f..32f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ============================
            //       TEXT TO SPEECH
            // ============================
            SettingsCard(
                icon = Icons.Default.RecordVoiceOver,
                title = "Text-to-Speech"
            ) {
                Button(
                    onClick = {
                        settingsViewModel.toggleTTS(htmlContent)
                        if (!isSpeaking) onTTSStart()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isSpeaking) "Stop Reading" else "Start Reading",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // ============================
            //          THEME
            // ============================
            SettingsCard(
                icon = Icons.Default.ColorLens,
                title = "Theme"
            ) {
                ThemeChips(
                    current = selectedTheme,
                    onSelect = { settingsViewModel.setTheme(it) }
                )
            }
        }
    }
}

@Composable
fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            content()
        }
    }
}

@Composable
fun ThemeChips(current: String, onSelect: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ThemeChip("steel", "Steel", current == "steel") { onSelect("steel") }
        ThemeChip("violet", "Violet", current == "violet") { onSelect("violet") }
    }
}

@Composable
fun ThemeChip(
    value: String,
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(22.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            Color.Transparent,
        border = if (!selected)
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        else null,
        tonalElevation = if (selected) 6.dp else 0.dp,
        modifier = Modifier
            .padding(end = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
