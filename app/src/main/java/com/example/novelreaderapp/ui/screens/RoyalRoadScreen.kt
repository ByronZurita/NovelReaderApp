package com.example.novelreaderapp.ui.screens

import RoyalRoadViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.novelreaderapp.ui.components.NovelCard
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.novelreaderapp.ui.components.ScreenTopBar
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

/**
 * Composable screen that displays novels from RoyalRoad, with support for live updates,
 * search, pagination, and filtering by genre and rating mode.
 *
 * @param onToggleMode Callback when switching between modes (currently unused in this screen).
 * @param onNovelClick Called when a novel is clicked, passing the novel ID and URL.
 * @param onNavigateToSettings Callback to navigate to the settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoyalRoadScreen(
    onNovelClick: (novelId: String, novelUrl: String, novelTitle: String, coverUrl: String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val viewModel: RoyalRoadViewModel = viewModel()

    val currentPage by viewModel.currentPage.collectAsState()
    val isBestRated by viewModel.isBestRated.collectAsState()
    val genre by viewModel.genre.collectAsState()
    val novels by viewModel.novels.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var searchBarVisible by remember { mutableStateOf(false) }


    // Load novels once when the screen first composes
    // Automatically load page 1 if it's not Best Rated mode
    LaunchedEffect(Unit) {
        if (!viewModel.isBestRated.value) {
            viewModel.loadNovelsPage(1)
        }
    }


    // Debug logging for duplicated IDs (can be removed in production)
    LaunchedEffect(novels) {
        val duplicateIds = novels.groupBy { it.id }
            .filter { it.value.size > 1 }
            .keys
        if (duplicateIds.isNotEmpty()) {
            println("Duplicate IDs found: $duplicateIds")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text("Royal Road")
                        Text(
                            text = if (isBestRated) "Best Rated" else "Latest Updates",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // 🔎 FILTER CONTROLS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Row 1: Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.toggleBestRatedMode(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isBestRated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!isBestRated) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Latest")
                    }

                    Button(
                        onClick = { viewModel.toggleBestRatedMode(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBestRated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isBestRated) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Best Rated")
                    }

                    // 🔍 Search toggle icon
                    IconButton(onClick = { searchBarVisible = !searchBarVisible }) {
                        Icon(Icons.Filled.Search, contentDescription = "Toggle Search")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Genre dropdown full width
                GenreDropdown(
                    selectedGenre = genre,
                    onGenreSelected = { viewModel.updateGenre(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Row 3: Search bar (optional, toggled)
                AnimatedVisibility(visible = searchBarVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        placeholder = { Text("Search novels") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true
                    )
                }
            }

            // 📚 Novel list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                items(novels, key = { it.id + it.url + it.tags.joinToString() }) { novel ->
                    NovelCard(
                        title = novel.title,
                        tags = novel.tags,
                        coverUrl = novel.coverUrl,
                    ) {
                        viewModel.updateSearchQuery("")
                        onNovelClick(novel.id, novel.url, novel.title, novel.coverUrl?: "")
                    }
                }
            }

            // ⬅️ Pagination
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.loadPreviousPage() },
                    enabled = currentPage > 1
                ) {
                    Text("Previous")
                }

                Text("Page $currentPage")

                Button(
                    onClick = { viewModel.loadNextPage() },
                    enabled = novels.isNotEmpty()
                ) {
                    Text("Next")
                }
            }
        }
    }
}

/**
 * Composable dropdown menu used to select genres for best-rated novel filtering.
 *
 * @param selectedGenre The currently selected genre value.
 * @param onGenreSelected Callback when a new genre is selected.
 */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GenreDropdown(
        selectedGenre: String,
        onGenreSelected: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val genres = listOf(
            "" to "All Genres",
            "action" to "Action",
            "adventure" to "Adventure",
            "comedy" to "Comedy",
            "contemporary" to "Contemporary",
            "drama" to "Drama",
            "fantasy" to "Fantasy",
            "historical" to "Historical",
            "horror" to "Horror",
            "mystery" to "Mystery",
            "psychological" to "Psychological",
            "romance" to "Romance",
            "satire" to "Satire",
            "sci_fi" to "Sci-fi",
            "one_shot" to "Short Story",
            "tragedy" to "Tragedy"
        )

        var expanded by remember { mutableStateOf(false) }
        val label = genres.firstOrNull { it.first == selectedGenre }?.second ?: "All Genres"

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier
        ) {
            OutlinedTextField(
                value = label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Genre") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genres.forEach { (value, displayName) ->
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            onGenreSelected(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
