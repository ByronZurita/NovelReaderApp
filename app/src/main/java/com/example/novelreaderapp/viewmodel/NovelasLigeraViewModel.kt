package com.example.novelreaderapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novelreaderapp.data.models.Novel
import com.example.novelreaderapp.data.scraper.NovelasLigeraScraper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NovelasLigeraViewModel : ViewModel() {

    private val scraper = NovelasLigeraScraper()

    private val _novels = MutableStateFlow<List<Novel>>(emptyList())
    val novels: StateFlow<List<Novel>> = _novels.asStateFlow()

    private val _selectedCategory = MutableStateFlow("main")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private var currentPage = 1
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
        currentPage++
        fetchNovelsForCategory(_selectedCategory.value, reset = false)
    }

    private fun fetchNovelsForCategory(category: String, reset: Boolean) {
        viewModelScope.launch {
            isLoading = true
            if (reset) {
                currentPage = 1
                endReached = false
            }
            val fetched = when (category) {
                "chinese" -> scraper.fetchChineseNovels(currentPage)
                "korean" -> scraper.fetchKoreanNovels(currentPage)
                "japanese" -> scraper.fetchJapaneseNovels(currentPage)
                else -> scraper.fetchNovels() // Assuming main page no pagination
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
