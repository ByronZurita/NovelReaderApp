package com.byron.novelreaderapp.data.scraper

import android.util.Log
import com.byron.novelreaderapp.data.models.Chapter
import com.byron.novelreaderapp.data.models.Novel
import com.byron.novelreaderapp.data.scraper.base.NovelScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NovelasLigeraScraper : NovelScraper {

    private val baseUrl = "https://novelasligera.com"

    suspend fun fetchChineseNovels(page: Int = 1): List<Novel> =
        fetchNovelsFromCategory("https://novelasligera.com/novelas-chinas/?_page=$page")

    suspend fun fetchKoreanNovels(page: Int = 1): List<Novel> =
        fetchNovelsFromCategory("https://novelasligera.com/novelas-coreanas/?_page=$page")

    suspend fun fetchJapaneseNovels(page: Int = 1): List<Novel> =
        fetchNovelsFromCategory("https://novelasligera.com/novelas-japonesas/?_page=$page")

    private suspend fun fetchNovelsFromCategory(categoryUrl: String): List<Novel> =
        withContext(Dispatchers.IO) {
            val novels = mutableListOf<Novel>()
            try {
                val doc = Jsoup.connect(categoryUrl)
                    .userAgent("Mozilla/5.0")
                    .get()

                val elements = doc.select(".pt-cv-content-item")
                for (element in elements) {
                    val novelUrl = element.selectFirst("a.pt-cv-href-thumbnail")?.attr("href")
                    val cover = element.selectFirst("img")?.let {
                        it.attr("data-lazy-src").takeIf { it.isNotBlank() }
                            ?: it.attr("src")
                                .takeIf { it.isNotBlank() && !it.startsWith("data:image") }
                    }

                    if (novelUrl == null) continue

                    val id = novelUrl.trimEnd('/').split("/").lastOrNull() ?: novelUrl
                    // Title fallback from slug (replace dashes, capitalize)
                    val title = id.replace('-', ' ').replaceFirstChar { it.uppercaseChar() }

                    novels.add(
                        Novel(
                            id = id,
                            title = title,
                            author = "Unknown",
                            description = "",
                            tags = emptyList(),
                            url = novelUrl,
                            coverUrl = cover,
                            sourceId = "novelasligera"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("NovelasLigera", "Error fetching category novels: ${e.message}")
            }
            novels
        }

    override suspend fun fetchNovels(): List<Novel> = withContext(Dispatchers.IO) {
        val novels = mutableListOf<Novel>()
        try {
            val doc = Jsoup.connect(baseUrl)
                .userAgent("Mozilla/5.0")
                .get()

            val elements = doc.select(".elementor-column")
            Log.d("NovelasLigera", "Found ${elements.size} novels on homepage")

            for (element in elements) {
                val title = element.selectFirst(".widget-image-caption.wp-caption-text")?.text()
                val novelUrl = element.selectFirst("a")?.attr("href")
                // Notice data-lazy-src, not src
                val cover = element.selectFirst("a > img")?.attr("data-lazy-src")

                Log.d(
                    "NovelasLigera",
                    "Novel element - title: $title, url: $novelUrl, cover: $cover"
                )

                if (title == null || novelUrl == null) continue

                val id = novelUrl.trimEnd('/').split("/").lastOrNull() ?: novelUrl

                novels.add(
                    Novel(
                        id = id,
                        title = title,
                        author = "Unknown",
                        description = "",
                        tags = emptyList(),
                        url = novelUrl,
                        coverUrl = cover,
                        sourceId = "novelasligera"
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("NovelasLigera", "Error in fetchNovels: ${e.message}")
        }
        novels
    }

    override suspend fun fetchNovelDetails(novelUrl: String): Novel = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(novelUrl)
                .userAgent("Mozilla/5.0")
                .get()

            val title = doc.selectFirst("h1")?.text() ?: "Unknown Title"
            val cover = doc.selectFirst(".elementor-widget-container img")?.attr("data-lazy-src")

            var author = "Unknown"
            var status = "Unknown"
            var type = ""
            var translator = ""
            var original = ""
            var genres = emptyList<String>()
            var description = ""

            // Description block (rich HTML, pick first large description paragraph)
            doc.select(".elementor-widget-container p").forEach { p ->
                val text = p.text().trim()

                when {
                    text.contains("Estado", ignoreCase = true) -> {
                        // Matches formats like: Estado: 24 Capítulos (Emisión)
                        status = text.substringAfter(":").trim()
                    }

                    text.contains("Tipo", ignoreCase = true) -> {
                        type = text.substringAfter(":").trim()
                    }

                    text.contains("Género", ignoreCase = true) -> {
                        genres = text.substringAfter(":").split(",").map { it.trim() }
                    }

                    text.contains("Autor", ignoreCase = true) -> {
                        author = text.substringAfter(":").trim()
                    }

                    text.contains("Traductor", ignoreCase = true) -> {
                        translator = text.substringAfter(":").trim()
                    }

                    text.contains("Original", ignoreCase = true) -> {
                        original = text.substringAfter(":").trim()
                    }
                    // Long description content
                    description.isEmpty() && text.length > 300 -> {
                        description = text
                    }
                }
            }

            Novel(
                id = novelUrl.trimEnd('/').split("/").lastOrNull() ?: novelUrl,
                title = title,
                author = author,
                description = description,
                tags = genres,
                url = novelUrl,
                coverUrl = cover,
                sourceId = "novelasligera",
                status = status
            )

        } catch (e: Exception) {
            Log.e("NovelasLigera", "Error in fetchNovelDetails: ${e.message}")
            Novel(
                id = novelUrl,
                title = "Unknown",
                author = "Unknown",
                description = "",
                tags = emptyList(),
                url = novelUrl,
                coverUrl = null,
                sourceId = "novelasligera"
            )
        }
    }


    override suspend fun fetchNovelChapters(novelUrl: String): List<Chapter> =
        withContext(Dispatchers.IO) {
            val chapters = mutableListOf<Chapter>()
            try {
                val doc = Jsoup.connect(novelUrl)
                    .userAgent("Mozilla/5.0")
                    .get()

                val seenUrls = mutableSetOf<String>()

                // Only select links under the actual chapter list (exclude 'Últimos Capítulos')
                val chapterElements = doc.select(".elementor-tabs .elementor-tab-content a")

                for (element in chapterElements) {
                    val title = element.text()
                    val chapterUrl = element.attr("href")

                    if (chapterUrl.isNotBlank() && chapterUrl !in seenUrls) {
                        seenUrls.add(chapterUrl)
                        chapters.add(Chapter(title = title, url = chapterUrl, novelUrl = novelUrl))
                    }
                }

            } catch (e: Exception) {
                Log.e("NovelasLigera", "Error in fetchNovelChapters: ${e.message}")
            }

            chapters
        }


    override suspend fun fetchChapterContent(url: String): String = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get()

            val contentElement = doc.selectFirst(".entry-content")

            // Clean unwanted elements **inside** the chapter content
            contentElement?.select(
                ".osny-nightmode--left, .code-block, .adsb30, .saboxplugin-wrap, .wp-post-navigation, style, script, ins.adsbygoogle"
            )?.remove()

            contentElement?.html() ?: ""
        } catch (e: Exception) {
            Log.e("NovelasLigera", "Error in fetchChapterContent: ${e.message}")
            ""
        }
    }
}
