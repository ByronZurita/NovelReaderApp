package com.example.novelreaderapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novelreaderapp.data.scraper.base.ScraperFactory
import com.example.novelreaderapp.data.models.Chapter
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

    fun loadChapters(novelId: String, novelUrl: String) {
        viewModelScope.launch {
            try {
                val scraper = ScraperFactory.getScraper(novelUrl)
                val novel = scraper.fetchNovelDetails(novelUrl)
                val fetchedChapters = scraper.fetchNovelChapters(novelUrl)

                _novelTitle.value = novel.title
                _author.value = novel.author
                _description.value = novel.description
                _tags.value = novel.tags
                _chapters.value = fetchedChapters
            } catch (e: Exception) {
                e.printStackTrace()
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
