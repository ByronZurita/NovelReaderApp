package com.example.novelreaderapp.ui.screens

import android.text.method.LinkMovementMethod
import android.widget.ScrollView
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.novelreaderapp.data.models.Chapter
import com.example.novelreaderapp.viewmodel.ChapterViewModel
import com.example.novelreaderapp.viewmodel.SettingsViewModel

/**
 * A composable screen that hosts the chapter reader functionality.
 *
 * It supports HTML content rendering, font scaling, TTS (Text-to-Speech), and chapter navigation.
 *
 * @param chapters A list of all chapters for navigation.
 * @param viewModel The ViewModel that handles chapter content fetching.
 * @param startIndex The index of the chapter to display initially.
 * @param onNavigateToSettings Callback when the settings icon is clicked.
 * @param settingsViewModel ViewModel for handling user preferences like font size and TTS.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterHostScreen(
    chapters: List<Chapter>,
    viewModel: ChapterViewModel,
    startIndex: Int = 0,
    onNavigateToSettings: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    // UI state and cache
    var htmlContent by remember { mutableStateOf("Loading...") }
    var content by remember { mutableStateOf("Loading...") }
    var currentIndex by rememberSaveable { mutableStateOf(startIndex) }
    val chapterCache = remember { mutableStateMapOf<String, String>() }

    val context = LocalContext.current
    val fontSizeSp by settingsViewModel.fontSize.collectAsState(initial = 18f)
    val fontSize = fontSizeSp.sp
    val chapterContent by viewModel.chapterContent.collectAsState()
    val scrollViewRef = remember { mutableStateOf<ScrollView?>(null) }
    val isSpeaking by settingsViewModel.isSpeaking.collectAsState()

    // Load chapter content when the chapter changes
    LaunchedEffect(chapters, currentIndex) {
        chapters.getOrNull(currentIndex)?.let { chapter ->
            val cachedContent = chapterCache[chapter.url]
            if (cachedContent != null) {
                content = cachedContent
                htmlContent = cachedContent
                settingsViewModel.setHtmlContent(cachedContent)
            } else {
                content = "Loading..."
                htmlContent = "Loading..."
                settingsViewModel.setHtmlContent("Loading...")
                viewModel.loadChapter(chapter.url, chapter.url)
            }
        }
    }

    // Update content when loaded by ViewModel
    LaunchedEffect(chapterContent) {
        if (chapterContent.isNotEmpty()) {
            content = chapterContent
            htmlContent = chapterContent
            chapterCache[chapters[currentIndex].url] = chapterContent
        }
    }

    // Control TTS based on state
    LaunchedEffect(isSpeaking) {
        if (isSpeaking) {
            settingsViewModel.startTTSAsync(htmlContent)
        } else {
            settingsViewModel.stopTTS()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = chapters.getOrNull(currentIndex)?.title ?: "Reading",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                actions = {
                    // Previous Chapter
                    IconButton(onClick = {
                        if (currentIndex > 0) {
                            settingsViewModel.stopTTS()
                            scrollViewRef.value?.scrollTo(0, 0)
                            currentIndex--
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Chapter")
                    }

                    // Next Chapter
                    IconButton(onClick = {
                        if (currentIndex < chapters.size - 1) {
                            settingsViewModel.stopTTS()
                            scrollViewRef.value?.scrollTo(0, 0)
                            currentIndex++
                        }
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Chapter")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            val chapter = chapters.getOrNull(currentIndex)
            if (chapter == null) {
                Text("No chapters available")
                return@Column
            }

            val textColorInt = MaterialTheme.colorScheme.onBackground.toArgb()

            // Render HTML content using a native TextView
            AndroidView(
                factory = { context ->
                    ScrollView(context).apply {
                        scrollViewRef.value = this
                        val textView = TextView(context).apply {
                            movementMethod = LinkMovementMethod.getInstance()
                            setPadding(24, 16, 24, 16)
                            setTextColor(textColorInt)
                            textSize = fontSize.value
                        }
                        addView(textView)
                    }
                },
                update = { view ->
                    val textView = view.getChildAt(0) as TextView
                    textView.text = HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    textView.setTextColor(textColorInt)
                    textView.textSize = fontSize.value
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
