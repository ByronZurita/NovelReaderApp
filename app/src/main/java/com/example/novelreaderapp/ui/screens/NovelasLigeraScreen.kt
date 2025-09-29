package com.example.novelreaderapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.novelreaderapp.ui.components.NovelCard
import com.example.novelreaderapp.ui.viewmodel.NovelasLigeraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelasLigeraScreen(
    onNovelClick: (novelId: String, novelUrl: String, novelTitle: String, coverUrl: String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val viewModel: NovelasLigeraViewModel = viewModel()
    val novels by viewModel.novels.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val listState = rememberLazyListState()
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Pagina Principal" to "main",
        "Novelas Chinas" to "chinese",
        "Novelas Coreanas" to "korean",
        "Novelas Japonesas" to "japanese"
    )

    // Trigger load more when near bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.layoutInfo.totalItemsCount }
            .collect { (firstVisible, totalItems) ->
                if (firstVisible > totalItems - 10) {  // when user scrolls near last 10 items
                    viewModel.fetchNextPage()
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Novelas Ligera") },
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
                .fillMaxSize()
                .padding(padding)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = categories.first { it.second == selectedCategory }.first,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { (label, key) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.selectCategory(key)
                                expanded = false
                            }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                state = listState
            ) {
                items(novels, key = { it.id }) { novel ->
                    NovelCard(
                        title = novel.title,
                        tags = novel.tags,
                        coverUrl = novel.coverUrl,
                        onClick = {
                            onNovelClick(novel.id, novel.url, novel.title, novel.coverUrl ?: "")
                        }
                    )
                }
            }
        }
    }
}
