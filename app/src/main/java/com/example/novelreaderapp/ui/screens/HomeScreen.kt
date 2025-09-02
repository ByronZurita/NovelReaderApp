package com.example.novelreaderapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.novelreaderapp.ui.screens.common.AppRoutes
import com.example.novelreaderapp.viewmodel.AuthViewModel

/**
 * Represents a web scraper source that the user can choose from.
 *
 * @param id Unique ID used internally to route or fetch data.
 * @param displayName Display name shown in the UI (may include emojis).
 */
data class ScraperSource(val id: String, val displayName: String)

/**
 * List of available web scrapers displayed on the home screen.
 * TODO: Replace placeholder entries with actual scraper implementations.
 */
val scraperSources = listOf(
    ScraperSource("royalroad", "📚 Royal Road"),
    ScraperSource("empty1", "📘 Empty Source 1"),
    ScraperSource("empty2", "📗 Empty Source 2"),
    ScraperSource("empty3", "📙 Empty Source 3"),
    ScraperSource("empty4", "📒 Empty Source 4"),
    ScraperSource("empty5", "📕 Empty Source 5")
)

/**
 * Main home screen showing welcome message, scraper list, and top app bar.
 *
 * @param onScraperClick Callback triggered when a scraper is selected.
 * @param onNavigateTo Navigation callback (e.g., for login, register, settings).
 * @param modifier Modifier to apply to the screen layout.
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novel Reader App") },
                scrollBehavior = scrollBehavior,
                actions = {
                    // Login/Profile button: navigates to Auth screen if not logged in
                    TextButton(onClick = {
                        if (token.isNullOrEmpty()) {
                            // User not logged in -> go to Auth screen, no autoNavigate
                            onNavigateTo("${AppRoutes.AuthScreen}?autoNavigate=false")
                        } else {
                            // User logged in -> go to profile/dashboard screen (update route if exists)
                            onNavigateTo(AppRoutes.AuthScreen) // Replace with profile route if available
                        }
                    }) {
                        Text(if (token.isNullOrEmpty()) "Login / Register" else "Profile")
                    }

                    // Settings button icon
                    IconButton(onClick = { onNavigateTo(AppRoutes.Settings) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
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
            // 🏠 Welcome section (Hero card)
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

            // 📂 Section title
            Text("Web Scrapers", style = MaterialTheme.typography.titleLarge)

            // 📚 Grid of scraper cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(scraperSources) { source ->
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
 *
 * @param source The scraper data to display.
 * @param onClick Action to perform when card is clicked.
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
            // Emoji or icon prefix
            Text(
                text = source.displayName.take(2),
                fontSize = 32.sp
            )
            Spacer(Modifier.height(8.dp))
            // Text title of the source
            Text(
                text = source.displayName.drop(2),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
