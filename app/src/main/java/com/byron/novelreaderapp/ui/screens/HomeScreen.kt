package com.byron.novelreaderapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.byron.novelreaderapp.ui.screens.common.AppRoutes
import com.byron.novelreaderapp.viewmodel.SettingsViewModel
import com.byron.novelreaderapp.ui.theme.*
import com.byron.novelreaderapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel
) {
    var filter by remember { mutableStateOf("all") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // HERO HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background) // custom palette
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Welcome to NovelReaderApp",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground // custom palette
                    )

                    Text(
                        "Continue reading or explore new novels.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )

                    ProjectRepoMiniCard()
                }
            }

            Spacer(Modifier.height(20.dp))

            // FEATURE ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FeatureButton(
                    label = "Profile",
                    icon = Icons.Default.Person
                ) { navController.navigate(AppRoutes.AuthScreen) }

                FeatureButton(
                    label = "Library",
                    icon = Icons.Default.MenuBook
                ) { navController.navigate(AppRoutes.Settings) }

                FeatureButton(
                    label = "History",
                    icon = Icons.Default.History
                ) { navController.navigate(AppRoutes.Settings) }

                FeatureButton(
                    label = "Settings",
                    icon = Icons.Default.Settings
                ) { navController.navigate(AppRoutes.Settings) }
            }

            Spacer(Modifier.height(24.dp))

            // FILTER ROW
            Text(
                "Sources",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip("All", filter == "all") { filter = "all" }
                FilterChip("English", filter == "en") { filter = "en" }
                FilterChip("Spanish", filter == "es") { filter = "es" }
            }

            Spacer(Modifier.height(12.dp))

            // FILTERED LIST
            val filteredList = when (filter) {
                "en" -> scraperSources.filter { it.language == "en" }
                "es" -> scraperSources.filter { it.language == "es" }
                else -> scraperSources
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { source ->
                    ScraperCard(source) {
                        when (source.id) {
                            "royalroad" -> navController.navigate(AppRoutes.RoyalRoad)
                            "novelbin" -> navController.navigate(AppRoutes.NovelBin)
                            "novelasligera" -> navController.navigate(AppRoutes.NovelasLigera)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectRepoMiniCard() {
    val context = LocalContext.current
    Surface(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ByronZurita/NovelReaderApp"))
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondary, // custom palette
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.github_mark),
                contentDescription = "Project Repository",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(45.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Project Repository",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "View the source code on GitHub",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun FeatureButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(65.dp),
            color = MaterialTheme.colorScheme.secondary,
            shape = CircleShape,
            tonalElevation = 6.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.tertiary,
        tonalElevation = if (selected) 6.dp else 2.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontWeight = FontWeight.Medium,
            color = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ScraperCard(source: ScraperSource, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondary,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(source.flagEmoji, fontSize = 28.sp)
            Text(
                text = source.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (source.iconRes != null) {
                Icon(
                    painter = painterResource(id = source.iconRes),
                    contentDescription = source.title,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(45.dp)
                )
            } else {
                Text(
                    text = source.flagEmoji,
                    fontSize = 32.sp
                )
            }
        }
    }
}

data class ScraperSource(
    val id: String,
    val flagEmoji: String,
    val title: String,
    val language: String,
    val iconRes: Int?=null
)

val scraperSources = listOf(
    ScraperSource("royalroad", "🇺🇸", "Royal Road", "en",R.drawable.royalroad),
    ScraperSource("novelbin", "🇺🇸", "NovelBin", "en",R.drawable.novelbin),
    ScraperSource("novelasligera", "🇪🇸", "Novelas Ligera", "es",R.drawable.novelasligera),
    ScraperSource("empty3", "📙", "Empty Source", "es",null),
    ScraperSource("empty4", "📒", "Empty Source", "es",null),
    ScraperSource("empty5", "📕", "Empty Source", "en",null)
)

