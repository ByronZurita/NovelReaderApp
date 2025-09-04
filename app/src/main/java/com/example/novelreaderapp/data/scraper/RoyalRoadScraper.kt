package com.example.novelreaderapp.data.scraper

import android.util.Log
import com.example.novelreaderapp.data.models.Chapter
import com.example.novelreaderapp.data.models.ChapterJson
import com.example.novelreaderapp.data.models.Novel
import com.example.novelreaderapp.data.scraper.base.NovelScraper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import java.net.URLEncoder

/**
 * RoyalRoadScraper handles all scraping operations related to the RoyalRoad source.
 * It implements the NovelScraper interface and provides search, chapter, and novel data.
 */
class RoyalRoadScraper : NovelScraper {

    /**
     * Fetch a specific page of novels from RoyalRoad's "Latest Updates" section.
     * @param page The page number to fetch.
     * @return A list of [com.example.novelreaderapp.data.models.Novel]s found on the specified page.
     */
    suspend fun fetchNovelsPage(page: Int, genre: String? = null): List<Novel> =
        withContext(Dispatchers.IO) {
            val novels = mutableListOf<Novel>()
            val baseUrl = "https://www.royalroad.com/fictions/latest-updates"
            val url =
                if (!genre.isNullOrBlank()) "$baseUrl?genre=$genre&page=$page" else "$baseUrl?page=$page"


            try {
                val doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get()

                val novelElements = doc.select(".fiction-list-item")
                for (element in novelElements) {
                    val titleElement = element.selectFirst(".fiction-title > a") ?: continue
                    val title = titleElement.text()
                    val url = "https://www.royalroad.com" + titleElement.attr("href")
                    val id = url.split("/").last()
                    val author = element.selectFirst(".author")?.text() ?: "Unknown Author"
                    val description =
                        element.selectFirst(".fiction-description")?.text()?.trim() ?: ""
                    val tagElements = element.select("span.tags a.fiction-tag")
                    val tags = tagElements.map { it.text().trim() }
                    val coverElement = element.selectFirst("figure.col-sm-2 img[data-type=cover]")
                    val coverUrl = coverElement?.attr("src")



                    novels.add(
                        Novel(
                            id = id,
                            title = title,
                            author = author,
                            description = description,
                            url = url,
                            tags = tags,
                            sourceId = "royalroad",
                            coverUrl = coverUrl
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("RoyalRoadScraper", "Failed to fetch page $page: ${e.message}")
            }

            novels
        }

    /**
     * Searches novels by title keyword.
     * @param query The search keyword.
     * @return A list of novels matching the query.
     */
    suspend fun searchNovels(query: String): List<Novel> = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val searchUrl =
            "https://www.royalroad.com/fictions/search?title=$encodedQuery&globalFilters=true"
        val novels = mutableListOf<Novel>()

        try {
            val doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0")
                .get()

            val novelElements = doc.select(".fiction-list-item")
            for (element in novelElements) {
                val titleElement = element.selectFirst(".fiction-title > a") ?: continue
                val title = titleElement.text()
                val url = "https://www.royalroad.com" + titleElement.attr("href")
                val id = url.split("/").last()
                val author = element.selectFirst(".author")?.text() ?: "Unknown Author"
                val description = element.selectFirst(".fiction-description")?.text()?.trim() ?: ""
                val tagElements = element.select("span.tags a.fiction-tag")
                val tags = tagElements.map { it.text().trim() }
                val coverElement = element.selectFirst("figure.col-sm-2 img[data-type=cover]")
                val coverUrl = coverElement?.attr("src")


                novels.add(
                    Novel(
                        id = id,
                        title = title,
                        author = author,
                        description = description,
                        url = url,
                        tags = tags,
                        sourceId = "royalroad",
                        coverUrl = coverUrl
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("RoyalRoadScraper", "Search failed: ${e.message}")
        }

        novels
    }

    /**
     * Default implementation from NovelScraper interface.
     * Fetches the latest updated novels (page 1 only).
     */
    override suspend fun fetchNovels(): List<Novel> = withContext(Dispatchers.IO) {
        val novels = mutableListOf<Novel>()
        val doc = Jsoup.connect("https://www.royalroad.com/fictions/latest-updates").get()
        val novelElements = doc.select(".fiction-list-item")

        for (element in novelElements) {
            val titleElement = element.selectFirst(".fiction-title > a") ?: continue
            val title = titleElement.text()
            val url = "https://www.royalroad.com" + titleElement.attr("href")
            val id = url.split("/").last()
            val author = element.selectFirst(".author")?.text() ?: "Unknown Author"
            val description = element.selectFirst(".fiction-description")?.text()?.trim() ?: ""
            val tagElements = element.select("span.tags a.fiction-tag")
            val tags = tagElements.map { it.text().trim() }
            val coverElement = element.selectFirst("figure.col-sm-2 img[data-type=cover]")
            val coverUrl = coverElement?.attr("src")



            novels.add(
                Novel(
                    id = id,
                    title = title,
                    author = author,
                    description = description,
                    url = url,
                    tags = tags,
                    sourceId = "royalroad",
                    coverUrl = coverUrl
                )
            )
        }

        novels
    }

    /**
     * Scrapes chapters for a given novel page.
     * Uses embedded JSON (window.chapters) from RoyalRoad’s HTML.
     * @param novelUrl Full URL of the novel page.
     */
    override suspend fun fetchNovelChapters(novelUrl: String): List<Chapter> =
        withContext(Dispatchers.IO) {
            val chapters = mutableListOf<Chapter>()
            val doc = Jsoup.connect(novelUrl).get()
            val html = doc.html()

            val regex = Regex("""window\.chapters\s*=\s*(\[[^\]]*\])""")
            val matchResult = regex.find(html)

            if (matchResult != null) {
                val chaptersJson = matchResult.groupValues[1]
                val gson = Gson()
                val chapterList =
                    gson.fromJson(chaptersJson, Array<ChapterJson>::class.java).toList()

                chapterList.forEach { c ->
                    val fullUrl = "https://www.royalroad.com" + c.url
                    chapters.add(Chapter(title = c.title, url = fullUrl, novelUrl = novelUrl))
                }

                println("Scraper fetched ${chapters.size} chapters for $novelUrl")
            } else {
                println("No chapters JSON found on the page: $novelUrl")
            }

            chapters
        }

    /**
     * Loads the HTML content of a specific chapter.
     * @param url Chapter URL.
     * @return HTML string of the chapter content.
     */
    override suspend fun fetchChapterContent(url: String): String = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .get()

            // Try various common selectors
            val chapterContentElement = document.selectFirst("#chapter-content")
                ?: document.selectFirst(".chapter-content")
                ?: document.selectFirst(".chapter-inner")

            val content = chapterContentElement?.html()
            Log.d("RoyalRoadScraper", "Fetched chapter content length: ${content?.length ?: 0}")
            content ?: ""
        } catch (e: HttpStatusException) {
            if (e.statusCode == 429) {
                Log.e("RoyalRoadScraper", "Rate limit hit for url $url. Slow down!")
                ""
            } else {
                throw e
            }
        }
    }

    /**
     * Fetch top-rated novels from RoyalRoad. Can be filtered by genre.
     * @param genre Optional genre string (e.g., "fantasy", "sci_fi").
     * @return List of top-rated novels.
     */
    suspend fun getBestRatedNovels(genre: String? = null): List<Novel> =
        withContext(Dispatchers.IO) {
            val novels = mutableListOf<Novel>()
            val baseUrl = "https://www.royalroad.com/fictions/best-rated"
            val url = if (!genre.isNullOrBlank()) "$baseUrl?genre=$genre" else baseUrl

            try {
                val doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get()

                val novelElements = doc.select(".fiction-list-item")
                for (element in novelElements) {
                    val titleElement = element.selectFirst(".fiction-title > a") ?: continue
                    val title = titleElement.text()
                    val novelUrl = "https://www.royalroad.com" + titleElement.attr("href")
                    val id = novelUrl.split("/").last()
                    val author = element.selectFirst(".author")?.text() ?: "Unknown"
                    val description =
                        element.selectFirst(".fiction-description")?.text()?.trim() ?: ""
                    val tagElements = element.select("span.tags a.fiction-tag")
                    val tags = tagElements.map { it.text().trim() }
                    val coverElement = element.selectFirst("figure.col-sm-2 img[data-type=cover]")
                    val coverUrl = coverElement?.attr("src")


                    novels.add(
                        Novel(
                            id = id,
                            title = title,
                            author = author,
                            description = description,
                            url = novelUrl,
                            tags = tags,
                            sourceId = "royalroad",
                            coverUrl = coverUrl
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("RoyalRoadScraper", "Failed to fetch best-rated novels: ${e.message}")
            }

            novels
        }

    override suspend fun fetchNovelDetails(novelUrl: String): Novel = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(novelUrl)
            .userAgent("Mozilla/5.0")
            .get()

        val title = doc.selectFirst("h1.profile-title")?.text()
            ?: doc.selectFirst("meta[name=twitter:title]")?.attr("content")
            ?: "Unknown Title"

        val author = doc.selectFirst("meta[property=books:author]")?.attr("content")
            ?: doc.selectFirst("meta[name=twitter:creator]")?.attr("content")
            ?: "Unknown Author"

        val coverUrl = doc.selectFirst("meta[property=og:image]")?.attr("content")

        val descriptionParagraphs = doc.select(".description .hidden-content p")
        val description = if (descriptionParagraphs.isNotEmpty()) {
            descriptionParagraphs.joinToString("\n\n") { it.text().trim() }
        } else {
            doc.selectFirst("meta[name=description]")?.attr("content")
                ?: doc.selectFirst("meta[property=og:description]")?.attr("content")
                ?: ""
        }

        val tagElements = doc.select(".tags a.fiction-tag")
        val tags = tagElements.map { it.text().trim() }

        Novel(
            id = novelUrl.split("/").last(),
            title = title,
            author = author,
            description = description,
            tags = tags,
            url = novelUrl,
            coverUrl = coverUrl,
            sourceId = "royalroad"
        )
    }
}