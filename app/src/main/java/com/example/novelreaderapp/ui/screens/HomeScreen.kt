package com.example.novelreaderapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.example.novelreaderapp.ui.screens.common.AppRoutes
import com.example.novelreaderapp.viewmodel.AuthViewModel

/**
 * Represents a web scraper source that the user can choose from.
 */
data class ScraperSource(
    val id: String,
    val flagEmoji: String,
    val title: String,
    val language: String // "en" or "es"
)

/**
 * List of available web scrapers.
 */
val scraperSources = listOf(
    ScraperSource("royalroad", "🇺🇸", "Royal Road", "en"),
    ScraperSource("novelbin", "🇺🇸", "NovelBin", "en"),
    ScraperSource("novelasligera", "🇪🇸", "Novelas Ligera", "es"),
    ScraperSource("empty3", "📙", "Empty Source", "es"),
    ScraperSource("empty4", "📒", "Empty Source", "en"),
    ScraperSource("empty5", "📕", "Empty Source", "en")
)

/**
 * Main home screen showing welcome message, scraper list, and top app bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onScraperClick: (ScraperSource) -> Unit,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val token by authViewModel.authToken.collectAsState()

    // Selected language filter
    var selectedLanguage by remember { mutableStateOf("all") }

    // Filtered scraper sources
    val filteredSources = if (selectedLanguage == "all") {
        scraperSources
    } else {
        scraperSources.filter { it.language == selectedLanguage }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novel Reader App") },
                scrollBehavior = scrollBehavior,
                actions = {
                    TextButton(onClick = {
                        if (token.isNullOrEmpty()) {
                            onNavigateTo("${AppRoutes.AuthScreen}?autoNavigate=false")
                        } else {
                            onNavigateTo(AppRoutes.AuthScreen) // Replace with profile/dashboard route
                        }
                    }) {
                        Text(if (token.isNullOrEmpty()) "Login / Register" else "Profile")
                    }
                    IconButton(onClick = { onNavigateTo(AppRoutes.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Welcome card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Welcome to the World of Novels", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Find and read novels with ease from your smartphone",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Language filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                listOf(
                    "all" to "🌐 All",
                    "en" to "🇺🇸 English",
                    "es" to "🇪🇸 Español"
                ).forEach { (langCode, label) ->
                    FilterChip(
                        selected = selectedLanguage == langCode,
                        onClick = { selectedLanguage = langCode },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Section title
            Text("Web Scrapers", style = MaterialTheme.typography.titleLarge)

            // Grid of scraper cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredSources) { source ->
                    ScraperCard(source = source) {
                        onScraperClick(source)
                    }
                }
            }
        }
    }
}

/**
 * Displays a card representing a single scraper source.
 */
@Composable
fun ScraperCard(source: ScraperSource, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.3f),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = source.flagEmoji,
                fontSize = 32.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = source.title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
