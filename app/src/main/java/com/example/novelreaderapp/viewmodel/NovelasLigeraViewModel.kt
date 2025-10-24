package com.example.novelreaderapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novelreaderapp.data.models.Novel
import com.example.novelreaderapp.data.scraper.NovelasLigeraScraper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NovelasLigeraViewModel : ViewModel() {

    private val scraper = NovelasLigeraScraper()

    private val _novels = MutableStateFlow<List<Novel>>(emptyList())
    val novels: StateFlow<List<Novel>> = _novels.asStateFlow()

    private val _selectedCategory = MutableStateFlow("main")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private var isLoading = false
    private var endReached = false

    init {
        fetchNovelsForCategory("main", reset = true)
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        fetchNovelsForCategory(category, reset = true)
    }

    fun fetchNextPage() {
        if (isLoading || endReached) return
        _currentPage.value++
        fetchNovelsForCategory(_selectedCategory.value, reset = false)
    }

    fun loadNextPage() {
        fetchNextPage()
    }

    fun loadPreviousPage() {
        if (_currentPage.value > 1 && !isLoading) {
            _currentPage.value--
            fetchNovelsForCategory(_selectedCategory.value, reset = true)
        }
    }

    private fun fetchNovelsForCategory(category: String, reset: Boolean) {
        viewModelScope.launch {
            isLoading = true
            if (reset) {
                endReached = false
            }

            val fetched = when (category) {
                "chinese" -> scraper.fetchChineseNovels(_currentPage.value)
                "korean" -> scraper.fetchKoreanNovels(_currentPage.value)
                "japanese" -> scraper.fetchJapaneseNovels(_currentPage.value)
                else -> scraper.fetchNovels() // main page, no pagination
            }

            if (reset) {
                _novels.value = fetched
            } else {
                if (fetched.isEmpty()) {
                    endReached = true
                } else {
                    _novels.value = _novels.value + fetched
                }
            }

            isLoading = false
        }
    }
}
