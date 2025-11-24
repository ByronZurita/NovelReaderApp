package com.byron.novelreaderapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.byron.novelreaderapp.viewmodel.SettingsViewModel

private val Steel = darkColorScheme(
    primary = Steel2,
    onPrimary = Steel1,
    primaryContainer = Steel4,
    onPrimaryContainer = Steel1,

    secondary = Steel3,
    onSecondary = Steel1,
    secondaryContainer = Steel4,
    onSecondaryContainer = Steel1,

    tertiary = Steel4,
    onTertiary = Steel1,
    tertiaryContainer = Steel3,
    onTertiaryContainer = Steel1,

    background = Steel1,
    onBackground = Steel2,

    surface = Steel4,
    onSurface = Steel2,

    surfaceVariant = Steel3,
    onSurfaceVariant = Steel1,

    outline = Steel2,
    outlineVariant = Steel3,

    inverseSurface = Steel2,
    inverseOnSurface = Steel1,
    inversePrimary = Steel3,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFB4AB),

    scrim = Color.Black,
    surfaceTint = Steel2
)


private val Violet = darkColorScheme(
    primary = Violet2,
    onPrimary = Violet1,
    secondary = Violet3,
    onSecondary = Violet1,
    tertiary = Violet4,
    background = Violet1,
    onBackground = Violet2,
    surface = Violet4,
    onSurface = Violet2
)

/**
 * App Theme supporting:
 * - Light / Dark mode
 * - User-selected color palette (Night Sands / Gothic Noir)
 */
@Composable
fun NovelReaderAppTheme(
    settingsViewModel: SettingsViewModel,
    content: @Composable () -> Unit
) {
    val selectedTheme by settingsViewModel.selectedTheme.collectAsState()

    val colorScheme = when (selectedTheme) {
        "steel" -> Steel
        "violet" -> Violet
        else -> Steel
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

/*
HomeScreen.kt
| Component           | Color(s) used                                       | Typography              | Notes                             |
| ------------------- | --------------------------------------------------- | ----------------------- | --------------------------------- |
| Scaffold            | background                                          | —                       | Whole screen background           |
| Hero Header Box     | background, onBackground                            | headlineSmall           | Welcome header area               |
| Hero subtext        | onBackground (alpha 0.8f)                           | bodyMedium (implicit)   | Description under the title       |
| ProjectRepoMiniCard | secondary, onSecondary, onBackground                | titleMedium, bodyMedium | Card linking to GitHub repo       |
| FeatureButton       | secondary, onSecondary, onBackground                | —                       | Circular feature buttons          |
| FilterChip          | primary/onPrimary (selected)                        | bodyMedium (implicit)   | Language filter chips             |
|                     | tertiary/onBackground (unselected)                  |                         |                                   |
| ScraperCard         | secondary, onSecondary, onBackground                | titleMedium             | Sourced scraper list item         |
| “Sources” text      | onBackground                                        | titleMedium             | Filter section label              |

Takeaways:
Typography uses mostly headlineSmall and titleMedium for headings; bodyMedium for smaller card texts.
Components that change appearance based on state (FilterChip).
*/
