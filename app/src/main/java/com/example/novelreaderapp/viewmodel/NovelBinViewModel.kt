package com.example.novelreaderapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novelreaderapp.data.models.Novel
import com.example.novelreaderapp.data.scraper.NovelBinScraper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NovelBinViewModel : ViewModel() {

    private val scraper = NovelBinScraper()

    private val _novels = MutableStateFlow<List<Novel>>(emptyList())
    val novels: StateFlow<List<Novel>> = _novels

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted

    private val _author = MutableStateFlow("")
    val author: StateFlow<String> = _author

    val category = MutableStateFlow("daily") // "daily" or "popular"
    fun loadNovelsPage(page: Int = 1) {
        viewModelScope.launch {
            val result = when (category.value) {
                "popular" -> scraper.fetchPopularNovelsPage(page, isCompleted.value)
                else -> scraper.fetchNovelsPage(page, isCompleted.value)
            }

            _novels.value = result
            _currentPage.value = page
        }
    }

    fun loadNextPage() {
        loadNovelsPage(_currentPage.value + 1)
    }

    fun loadPreviousPage() {
        val prevPage = (_currentPage.value - 1).coerceAtLeast(1)
        loadNovelsPage(prevPage)
    }

    fun toggleCompleted(completed: Boolean) {
        _isCompleted.value = completed
        loadNovelsPage(1)
    }

    fun toggleCategory(newCategory: String) {
        if (category.value != newCategory) {
            category.value = newCategory
            loadNovelsPage(1)
        }
    }

    fun searchNovels(query: String) {
        viewModelScope.launch {
            val results = scraper.searchNovels(query)
            _novels.value = results
            // Reset pagination or other states if necessary
        }
    }
}
