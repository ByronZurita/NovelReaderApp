package com.example.novelreaderapp.ui.screens.common

import RoyalRoadViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.novelreaderapp.ui.components.NovelCard
import com.example.novelreaderapp.ui.viewmodel.NovelasLigeraViewModel
import com.example.novelreaderapp.viewmodel.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelsScreen(
    source: String = "",
    onNovelClick: (novelId: String, novelUrl: String, novelTitle: String, coverUrl: String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // --- ViewModel selection based on source ---
    val rrViewModel: RoyalRoadViewModel? =
        if (source == "royalroad") viewModel<RoyalRoadViewModel>() else null
    val nbViewModel: NovelBinViewModel? =
        if (source == "novelbin") viewModel<NovelBinViewModel>() else null
    val nlViewModel: NovelasLigeraViewModel? =
        if (source == "novelasligera") viewModel<NovelasLigeraViewModel>() else null

    // --- Auto-load initial page for RoyalRoad and NovelBin ---
    LaunchedEffect(source) {
        when (source) {
            "royalroad" -> {
                rrViewModel?.let { vm ->
                    if (vm.isBestRated.value) {
                        vm.loadBestRatedNovels(vm.genre.value)
                    } else {
                        vm.loadNovelsPage(1)
                    }
                }
            }
            "novelbin" -> {
                nbViewModel?.loadNovelsPage(1) // default "daily"
            }
        }
    }

    // --- Common UI states ---
    val novels = when (source) {
        "royalroad" -> rrViewModel?.novels?.collectAsState()?.value ?: emptyList()
        "novelbin" -> nbViewModel?.novels?.collectAsState()?.value ?: emptyList()
        "novelasligera" -> nlViewModel?.novels?.collectAsState()?.value ?: emptyList()
        else -> emptyList()
    }

    val currentPage = when (source) {
        "royalroad" -> rrViewModel?.currentPage?.collectAsState()?.value ?: 1
        "novelbin" -> nbViewModel?.currentPage?.collectAsState()?.value ?: 1
        "novelasligera" -> nlViewModel?.currentPage?.collectAsState()?.value ?: 1
        else -> 1
    }

    // 🔍 Search state
    var showSearch by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // --- Filter handling ---
    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember {
        mutableStateOf(
            when (source) {
                "royalroad" -> if (rrViewModel?.isBestRated?.value == true) "best" else rrViewModel?.genre?.value
                    ?: "latest"

                "novelbin" -> "daily" // default for novelbin, for example
                "novelasligera" -> "main" // default for novelasligera
                else -> ""
            }
        )
    }

    // --- Dynamic filter list based on source ---
    val filters: List<Pair<String, String>> = when (source) {
        "royalroad" -> listOf(
            "Latest Updates" to "latest",
            "Best Rated" to "best",
            "All Genres" to "",
            "Action" to "action",
            "Adventure" to "adventure",
            "Romance" to "romance",
            "Fantasy" to "fantasy",
            "Comedy" to "comedy",
            "Contemporary" to "contemporary",
            "Drama" to "drama",
            "Historical" to "historical",
            "Horror" to "horror",
            "Mystery" to "mystery",
            "Psychological" to "psychological",
            "Satire" to "satire",
            "Sci_fi" to "sci-fi",
            "Short Story" to "one_shot",
            "Tragedy" to "tragedy"
        )

        "novelbin" -> listOf(
            "Latest" to "daily",
            "Popular" to "popular",
            "Latest Completed" to "daily_completed",
            "Popular Completed" to "popular_completed",
        )

        "novelasligera" -> listOf(
            "Pagina Principal" to "main",
            "Novelas Chinas" to "chinese",
            "Novelas Coreanas" to "korean",
            "Novelas Japonesas" to "japanese",
        )

        else -> emptyList()
    }

    // --- Actions for filters ---
    fun onFilterSelect(value: String) {
        val wasBestRated = rrViewModel?.isBestRated?.value == true
        selectedFilter = value

        when (source) {
            "royalroad" -> when (value) {
                "best" -> rrViewModel?.toggleBestRatedMode(true)
                "latest" -> rrViewModel?.toggleBestRatedMode(false)
                "" -> { // All Genres
                    rrViewModel?.updateGenre("")
                    rrViewModel?.toggleBestRatedMode(wasBestRated)
                }

                else -> {
                    rrViewModel?.updateGenre(value)
                    rrViewModel?.toggleBestRatedMode(false)
                }
            }

            "novelbin" -> nbViewModel?.applyFilter(value)
            "novelasligera" -> nlViewModel?.selectCategory(value)
        }
    }


    // --- UI Layout ---
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(source.replaceFirstChar { it.uppercase() }) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // 🔽 Filter / Search Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showSearch && (source == "royalroad" || source == "novelbin")) {
                        // --- Search Bar ---
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
                        IconButton(
                            onClick = {
                                when (source) {
                                    "royalroad" -> rrViewModel?.searchNovels(searchText)
                                    "novelbin" -> nbViewModel?.searchNovels(searchText)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }

                        // Cancel / Close button
                        IconButton(onClick = { showSearch = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Search")
                        }

                    } else {
                        // --- Filter Dropdown ---
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = filters.firstOrNull { it.second == selectedFilter }?.first
                                    ?: filters.firstOrNull()?.first.orEmpty(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filter") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                filters.forEach { (label, key) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            onFilterSelect(key)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Search toggle button next to filter
                        if (source == "royalroad" || source == "novelbin") {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { showSearch = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Show Search")
                            }
                        }
                    }

                    // Optional: You could also keep notes inside the row if preferred
                }

                // --- Info Note Below Row ---
                val noteText = when (source) {
                    "royalroad" -> "⚠️ RoyalRoad filters may work unexpectedly"
                    "novelbin" -> "⚠️ Some NovelBin novels may take a while to load"
                    else -> null
                }

                noteText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = 4.dp, start = 4.dp)
                    )
                }
            }



            // 📚 Novel List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                items(novels, key = { it.id }) { novel ->
                    NovelCard(
                        title = novel.title,
                        tags = novel.tags,
                        coverUrl = novel.coverUrl,
                        onClick = {
                            onNovelClick(
                                novel.id,
                                novel.url,
                                novel.title,
                                novel.coverUrl ?: ""
                            )
                        }
                    )
                }
            }

            // ⬅️➡️ Pagination (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        when (source) {
                            "royalroad" -> rrViewModel?.loadPreviousPage()
                            "novelbin" -> nbViewModel?.loadPreviousPage()
                            "novelasligera" -> nlViewModel?.loadPreviousPage()
                        }
                    },
                    enabled = currentPage > 1
                ) { Text("Previous") }

                Text("Page $currentPage")

                Button(
                    onClick = {
                        when (source) {
                            "royalroad" -> rrViewModel?.loadNextPage()
                            "novelbin" -> nbViewModel?.loadNextPage()
                            "novelasligera" -> nlViewModel?.loadNextPage()
                        }
                    },
                    enabled = novels.isNotEmpty()
                ) { Text("Next") }
            }
        }
    }
}