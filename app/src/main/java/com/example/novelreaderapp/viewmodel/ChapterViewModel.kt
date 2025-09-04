package com.example.novelreaderapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novelreaderapp.data.scraper.base.ScraperFactory
import com.example.novelreaderapp.data.models.Chapter
import com.example.novelreaderapp.data.scraper.NovelBinScraper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChapterViewModel : ViewModel() {

    private val _author = MutableStateFlow("")
    private val _description = MutableStateFlow("")
    private val _tags = MutableStateFlow<List<String>>(emptyList())

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters

    private val _chapterContent = MutableStateFlow("Loading...")
    val chapterContent: StateFlow<String> = _chapterContent

    private val _novelTitle = MutableStateFlow("")
    val novelTitle: StateFlow<String> = _novelTitle
    val author: StateFlow<String> = _author
    val description: StateFlow<String> = _description
    val tags: StateFlow<List<String>> = _tags

    private val _coverUrl = MutableStateFlow("")
    val coverUrl: StateFlow<String> = _coverUrl

    fun loadChapters(novelId: String, novelUrl: String) {
        viewModelScope.launch {
            try {
                val scraper = ScraperFactory.getScraper(novelUrl)
                val novel = scraper.fetchNovelDetails(novelUrl)
                _novelTitle.value = novel.title
                _author.value = novel.author
                _description.value = novel.description
                _tags.value = novel.tags
                _coverUrl.value = novel.coverUrl ?: ""

                val chapters = scraper.fetchNovelChapters(novelUrl)
                _chapters.value = chapters
            } catch (e: Exception) {
                Log.e("ChapterViewModel", "Error loading chapters/details", e)
                _description.value = "Failed to load novel details."
                _chapters.value = emptyList()
            }
        }
    }


    fun loadChapter(chapterId: String?, chapterUrl: String) {
        viewModelScope.launch {
            try {
                val scraper = ScraperFactory.getScraper(chapterUrl)
                val content = scraper.fetchChapterContent(chapterUrl)
                _chapterContent.value = content.ifBlank { "No content available" }
            } catch (e: Exception) {
                _chapterContent.value = "Failed to load chapter"
            }
        }
    }
}
