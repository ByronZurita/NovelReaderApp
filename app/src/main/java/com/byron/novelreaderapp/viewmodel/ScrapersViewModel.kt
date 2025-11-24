package com.byron.novelreaderapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byron.novelreaderapp.data.models.Novel
import com.byron.novelreaderapp.data.scraper.NovelBinScraper
import com.byron.novelreaderapp.data.scraper.NovelasLigeraScraper
import com.byron.novelreaderapp.data.scraper.RoyalRoadScraper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the state and business logic for RoyalRoad content.
 * Supports paginated loading, best-rated browsing, genre filtering, and remote search.
 *
 * @property repository The data repository used to fetch novels.
 */
class RoyalRoadViewModel : ViewModel() {

    private val royalRoadScraper = RoyalRoadScraper()

    /** Whether the "Best Rated" mode is currently enabled. */
    val isBestRated = MutableStateFlow(false)

    /** Current genre filter used in "Best Rated" mode. */
    val genre = MutableStateFlow("")

    /** Current page number (for paginated loading in live/latest mode). */
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    /** Loads a specific page of latest updates from RoyalRoad. */
    fun loadNovelsPage(page: Int, genre: String = this.genre.value) {
        viewModelScope.launch {
            val novelsPage = royalRoadScraper.fetchNovelsPage(page, genre)

            if (page == 1) {
                _novels.value = novelsPage
                _allNovels.value = novelsPage
            } else {
                _novels.value = _novels.value + novelsPage
                _allNovels.value = _allNovels.value + novelsPage
            }

            _currentPage.value = page
        }
    }

    /** Loads the next page of novels. */
    fun loadNextPage() {
        loadNovelsPage(_currentPage.value + 1)
    }

    /** Loads the previous page of novels, ensuring it stays >= 1. */
    fun loadPreviousPage() {
        val prev = (_currentPage.value - 1).coerceAtLeast(1)
        loadNovelsPage(prev)
    }

    /**
     * Loads the best-rated novels from RoyalRoad, optionally filtered by genre.
     *
     * @param genre Optional genre filter (e.g., "fantasy", "romance").
     */
    fun loadBestRatedNovels(genre: String?) {
        viewModelScope.launch {
            try {
                val results = royalRoadScraper.getBestRatedNovels(genre)
                _novels.value = results
                _allNovels.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Toggles between "Latest Updates" and "Best Rated" modes.
     * Always resets the current page to 1 when switching modes.
     *
     * @param enabled Whether to enable "Best Rated" mode.
     */
    fun toggleBestRatedMode(enabled: Boolean) {
        isBestRated.value = enabled
        _currentPage.value = 1

        if (enabled) {
            loadBestRatedNovels(genre.value)
        } else {
            // pass current genre explicitly so latest updates are filtered correctly
            loadNovelsPage(1, genre.value)
        }
    }


    /**
     * Updates the genre filter for best-rated mode.
     * Automatically reloads the best-rated novels if the mode is active.
     *
     * @param newGenre The genre to filter by.
     */
    fun updateGenre(newGenre: String) {
        genre.value = newGenre
        _currentPage.value = 1

        if (isBestRated.value) {
            loadBestRatedNovels(if (newGenre.isBlank()) null else newGenre)
        } else {
            loadNovelsPage(1, newGenre)
        }
    }

    /** Holds the complete list of loaded novels (used to restore after search clears). */
    private val _allNovels = MutableStateFlow<List<Novel>>(emptyList())

    /** Holds the filtered/search result list of novels for display in UI. */
    private val _novels = MutableStateFlow<List<Novel>>(emptyList())
    val novels: StateFlow<List<Novel>> = _novels.asStateFlow()

    /** Current search query for debounced remote search. */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Performs a remote search using the RoyalRoad scraper.
     *
     * @param query The query string to search for.
     */
    fun searchNovels(query: String) {
        viewModelScope.launch {
            try {
                val results = royalRoadScraper.searchNovels(query)
                _novels.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Holds the current job for debouncing search inputs. */
    private var searchJob: Job? = null

    /**
     * Updates the current search query and performs a debounced remote search after 300ms.
     * Restores the full novel list if the query is blank.
     *
     * @param query The user-entered query string.
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce delay
            if (query.isBlank()) {
                _novels.value = _allNovels.value
            } else {
                searchNovels(query)
            }
        }
    }
}


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

    // 🔍 --- Search Support ---
    private val _searchQuery = MutableStateFlow("")
    private val _allNovels = MutableStateFlow<List<Novel>>(emptyList())

    fun searchNovels(query: String) {
        viewModelScope.launch {
            try {
                val results = scraper.searchNovels(query)
                _novels.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300) // debounce delay
            if (query.isBlank()) {
                _novels.value = _allNovels.value
            } else {
                searchNovels(query)
            }
        }
    }

    fun loadNovelsPage(page: Int = 1) {
        viewModelScope.launch {
            val result = when (category.value) {
                "popular" -> scraper.fetchPopularNovelsPage(page, isCompleted.value)
                else -> scraper.fetchNovelsPage(page, isCompleted.value)
            }

            if (page == 1) {
                _novels.value = result
            } else {
                _novels.value = _novels.value + result
            }

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

    fun applyFilter(filter: String) {
        when (filter) {
            "daily" -> { toggleCategory("daily"); toggleCompleted(false) }
            "popular" -> { toggleCategory("popular"); toggleCompleted(false) }
            "daily_completed" -> { toggleCategory("daily"); toggleCompleted(true) }
            "popular_completed" -> { toggleCategory("popular"); toggleCompleted(true) }
        }
    }
}

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

    // never used
    fun loadNextPage() {
        fetchNextPage()
    }

    // never used
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
