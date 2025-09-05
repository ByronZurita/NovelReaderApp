package com.example.novelreaderapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.novelreaderapp.data.models.Chapter
import com.example.novelreaderapp.ui.components.ScreenTopBar
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.novelreaderapp.viewmodel.ChapterViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember



/**
 * A composable screen that displays a list of chapters for a selected novel.
 * @param onChapterClick Called when a chapter is clicked.
 * @param onNavigateToSettings Callback for navigating to the Settings screen.
 * @param modifier Modifier for customizing the layout.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChapterListScreen(
    onChapterClick: (Chapter) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChapterViewModel
) {
    // 🌟 Collect the state from the ViewModel
    val title by viewModel.novelTitle.collectAsState()
    val author by viewModel.author.collectAsState()
    val description by viewModel.description.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val coverUrl by viewModel.coverUrl.collectAsState()
    val chapters by viewModel.chapters.collectAsState()

    // Scaffold provides the base structure including the top app bar
    Scaffold(
        topBar = {
            ScreenTopBar(
                title = title,
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .padding(innerPadding)
                .padding(vertical = 8.dp)
        ) {
            // 📌 Novel Details Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    coverUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "$title cover image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4f)
                                .clip(RoundedCornerShape(8.dp))
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .padding(bottom = 16.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = "Author: $author",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (tags.isNotEmpty()) {
                        Text(
                            text = "Tags:",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            tags.forEach { tag ->
                                Surface(
                                    tonalElevation = 2.dp,
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Limited description
                    val showFullDescription = remember { mutableStateOf(false) }
                    val maxLines = if (showFullDescription.value) Int.MAX_VALUE else 5 // Limit lines when collapsed

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = maxLines,
                        modifier = Modifier
                            .clickable { showFullDescription.value = !showFullDescription.value }
                            .padding(bottom = 4.dp)
                    )

                    Text(
                        text = if (showFullDescription.value) "Show less" else "Show more",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { showFullDescription.value = !showFullDescription.value }
                            .padding(bottom = 16.dp)
                    )
                }
            }

            // 📚 Chapters List
            items(chapters) { chapter ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable { onChapterClick(chapter) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
