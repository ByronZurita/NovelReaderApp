import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novelreaderapp.data.models.Novel
import com.example.novelreaderapp.data.scraper.RoyalRoadScraper
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
            _currentPage.value = page
            val novelsPage = royalRoadScraper.fetchNovelsPage(page, genre)
            _novels.value = novelsPage
            _allNovels.value = novelsPage
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
            loadNovelsPage(1)
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
            loadBestRatedNovels(newGenre)
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
     * Loads the latest updated novels from the repository and caches them for search.
     */
    fun loadNovelsLive() {
        viewModelScope.launch {
            try {
                // Directly load first page for live latest novels
                loadNovelsPage(1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
