package com.byron.novelreaderapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.byron.novelreaderapp.ui.components.NovelCard
import com.byron.novelreaderapp.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelListScreen(
    source: String = "",
    onNovelClick: (novelId: String, novelUrl: String, novelTitle: String, coverUrl: String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val gridState = rememberLazyGridState()

    // ViewModels
    val rrViewModel: RoyalRoadViewModel? = if (source == "royalroad") viewModel() else null
    val nbViewModel: NovelBinViewModel? = if (source == "novelbin") viewModel() else null
    val nlViewModel: NovelasLigeraViewModel? = if (source == "novelasligera") viewModel() else null

    LaunchedEffect(gridState) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = layoutInfo.totalItemsCount
            lastVisible to total
        }.collect { (lastVisible, total) ->
            if (lastVisible >= total - 6) {   // triggers before fully reaching bottom
                when (source) {
                    "royalroad" -> rrViewModel?.loadNextPage()
                    "novelbin" -> nbViewModel?.loadNextPage()
                    "novelasligera" -> nlViewModel?.loadNextPage()
                }
            }
        }
    }

    // Autoload
    LaunchedEffect(source) {
        when (source) {
            "royalroad" -> {
                rrViewModel?.let { vm ->
                    if (vm.isBestRated.value) vm.loadBestRatedNovels(vm.genre.value)
                    else vm.loadNovelsPage(1)
                }
            }

            "novelbin" -> nbViewModel?.loadNovelsPage(1)
        }
    }

    // State
    val novels = when (source) {
        "royalroad" -> rrViewModel?.novels?.collectAsState()?.value ?: emptyList()
        "novelbin" -> nbViewModel?.novels?.collectAsState()?.value ?: emptyList()
        "novelasligera" -> nlViewModel?.novels?.collectAsState()?.value ?: emptyList()
        else -> emptyList()
    }

    var showSearch by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    var selectedFilter by remember {
        mutableStateOf(
            when (source) {
                "royalroad" -> if (rrViewModel?.isBestRated?.value == true)
                    "best" else rrViewModel?.genre?.value ?: "latest"

                "novelbin" -> "daily"
                "novelasligera" -> "main"
                else -> ""
            }
        )
    }

    // Filters
    val filters = when (source) {
        "royalroad" -> listOf(
            "Latest Updates" to "latest",
            "Best Rated" to "best",
            "Action" to "action",
            "Adventure" to "adventure",
            "Romance" to "romance",
            "Fantasy" to "fantasy",
            "Comedy" to "comedy",
            "Drama" to "drama",
            "Sci-Fi" to "sci-fi"
        )

        "novelbin" -> listOf(
            "Latest" to "daily",
            "Popular" to "popular",
            "Completed" to "daily_completed"
        )

        "novelasligera" -> listOf(
            "Principal" to "main",
            "Chinese" to "chinese",
            "Korean" to "korean",
            "Japanese" to "japanese"
        )

        else -> emptyList()
    }

    fun onFilterSelect(value: String) {
        selectedFilter = value
        when (source) {
            "royalroad" -> when (value) {
                "best" -> rrViewModel?.toggleBestRatedMode(true)
                "latest" -> rrViewModel?.toggleBestRatedMode(false)
                else -> {
                    rrViewModel?.updateGenre(value)
                    rrViewModel?.toggleBestRatedMode(false)
                }
            }

            "novelbin" -> nbViewModel?.applyFilter(value)
            "novelasligera" -> nlViewModel?.selectCategory(value)
        }
    }

    // UI
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        source.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colorScheme.background)
        ) {

            // SEARCH + FILTER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (showSearch && (source == "royalroad" || source == "novelbin")) {

                        OutlinedTextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                                when (source) {
                                    "royalroad" -> rrViewModel?.updateSearchQuery(it)
                                    "novelbin" -> nbViewModel?.updateSearchQuery(it)
                                }
                            },
                            label = { Text("Search novels...") },
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = {
                            when (source) {
                                "royalroad" -> rrViewModel?.searchNovels(searchText)
                                "novelbin" -> nbViewModel?.searchNovels(searchText)
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }

                        IconButton(onClick = { showSearch = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }

                    } else {

                        // FILTER DROPDOWN
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = filters.firstOrNull { it.second == selectedFilter }?.first
                                    ?: "",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                filters.forEach { (label, value) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            expanded = false
                                            onFilterSelect(value)
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                }
            }

            // ---- GRID OF NOVELS (3 per row) ----
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(novels) { novel ->
                    NovelCard(
                        title = novel.title,
                        coverUrl = novel.coverUrl ?: "",
                        onClick = {
                            onNovelClick(
                                novel.id ?: "",
                                novel.url ?: "",
                                novel.title,
                                novel.coverUrl ?: ""
                            )
                        }
                    )
                }
            }
        }
    }
}