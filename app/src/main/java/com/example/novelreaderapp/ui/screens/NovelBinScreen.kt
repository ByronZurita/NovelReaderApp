package com.example.novelreaderapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.novelreaderapp.viewmodel.NovelBinViewModel
import com.example.novelreaderapp.ui.components.NovelCard
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelBinScreen(
    onNovelClick: (novelId: String, novelUrl: String, novelTitle: String, coverUrl: String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val viewModel: NovelBinViewModel = viewModel()

    val currentPageState = viewModel.currentPage.collectAsState()
    val isCompletedState = viewModel.isCompleted.collectAsState()
    val novelsState = viewModel.novels.collectAsState()

    val currentPage = currentPageState.value
    val isCompleted = isCompletedState.value
    val novels = novelsState.value

    // Latests or Popular
    val category = viewModel.category.collectAsState().value

    // Searchbar
    var searchBarVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }


    // Load novels on screen entry
    LaunchedEffect(category, isCompleted) {
        viewModel.loadNovelsPage(1)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text("NovelBin")
                        Text(
                            text = if (isCompleted) "Completed Novels" else "Daily Updates",
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


            // 🔁 Category toggle: Latest vs Popular (side by side) + search button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        viewModel.toggleCategory("daily")
                        searchBarVisible = false
                        searchQuery = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (category == "daily") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (category == "daily") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Latest")
                }

                Button(
                    onClick = {
                        viewModel.toggleCategory("popular")
                        searchBarVisible = false
                        searchQuery = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (category == "popular") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (category == "popular") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Popular")
                }

                IconButton(
                    onClick = {
                        searchBarVisible = !searchBarVisible
                        if (!searchBarVisible) {
                            searchQuery = ""
                            viewModel.loadNovelsPage(1)
                        }
                    }
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "Toggle Search")
                }
            }

            // Search bar (animated visibility)
            AnimatedVisibility(visible = searchBarVisible) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { newQuery ->
                        searchQuery = newQuery
                        if (newQuery.length >= 3) {
                            viewModel.searchNovels(newQuery)
                        } else if (newQuery.isEmpty()) {
                            viewModel.loadNovelsPage(1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    placeholder = { Text("Search novels...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.loadNovelsPage(1)
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Spacer between search bar and completed toggle
            Spacer(modifier = Modifier.height(8.dp))

            // Completed toggle button
            Button(
                onClick = { viewModel.toggleCompleted(!isCompleted) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(50), // pill shape
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                }
                Text(
                    text = if (isCompleted) "Showing Completed Novels Only" else "Show Completed Novels Only"
                )
            }



            // 📚 Novel List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                items(novels, key = { it.id + it.url }) { novel ->
                    NovelCard(
                        title = novel.title,
                        author = novel.author,
                        coverUrl = novel.coverUrl,
                    ) {
                        onNovelClick(novel.id, novel.url, novel.title, novel.coverUrl ?: "")
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
