package com.example.novelreaderapp.data.scraper

import android.util.Log
import com.example.novelreaderapp.data.models.Chapter
import com.example.novelreaderapp.data.models.Novel
import com.example.novelreaderapp.data.scraper.base.NovelScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NovelBinScraper : NovelScraper {

    // Used to load one page of novels (pagination support)
    suspend fun fetchNovelsPage(page: Int, completed: Boolean = false): List<Novel> = withContext(Dispatchers.IO) {
        val novels = mutableListOf<Novel>()
        val baseUrl = if (completed)
            "https://novelbin.me/sort/novelbin-daily-update/completed"
        else
            "https://novelbin.me/sort/novelbin-daily-update"

        val url = "$baseUrl?page=$page"

        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get()

            val novelElements = doc.select(".list-novel .row")
            for (element in novelElements) {
                val titleElement = element.selectFirst(".novel-title a") ?: continue
                val title = titleElement.text()
                val novelUrl = titleElement.attr("href")
                val id = novelUrl.split("/").lastOrNull() ?: title

                val author = element.selectFirst(".author")?.text() ?: "Unknown Author"
                val latestChapter = element.selectFirst(".col-xs-2 .chapter-title")?.text() ?: ""
                val coverUrl = element.selectFirst("img.cover")?.attr("data-src")

                val description = "Latest: $latestChapter"

                novels.add(
                    Novel(
                        id = id,
                        title = title,
                        author = author,
                        description = description,
                        url = novelUrl,
                        tags = emptyList(),
                        sourceId = "novelbin",
                        coverUrl = coverUrl
                    )
                )
            }

        } catch (e: Exception) {
            Log.e("NovelBinScraper", "Failed to fetch novels page: ${e.message}")
        }

        novels
    }

    suspend fun fetchPopularNovelsPage(page: Int, completed: Boolean = false): List<Novel> = withContext(Dispatchers.IO) {
        val novels = mutableListOf<Novel>()
        val baseUrl = if (completed)
            "https://novelbin.me/sort/novelbin-popular/completed"
        else
            "https://novelbin.me/sort/novelbin-popular"

        val url = "$baseUrl?page=$page"

        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get()

            val novelElements = doc.select(".list-novel .row")
            for (element in novelElements) {
                val titleElement = element.selectFirst(".novel-title a") ?: continue
                val title = titleElement.text()
                val novelUrl = titleElement.attr("href")
                val id = novelUrl.split("/").lastOrNull() ?: title

                val author = element.selectFirst(".author")?.text() ?: "Unknown Author"
                val latestChapter = element.selectFirst(".col-xs-2 .chapter-title")?.text() ?: ""
                val coverUrl = element.selectFirst("img.cover")?.attr("data-src")

                val description = "Latest: $latestChapter"

                novels.add(
                    Novel(
                        id = id,
                        title = title,
                        author = author,
                        description = description,
                        url = novelUrl,
                        tags = emptyList(),
                        sourceId = "novelbin",
                        coverUrl = coverUrl
                    )
                )
            }

        } catch (e: Exception) {
            Log.e("NovelBinScraper", "Failed to fetch popular novels page: ${e.message}")
        }

        novels
    }

    override suspend fun fetchNovels(): List<Novel> {
        return fetchNovelsPage(1)
    }

    // Searchbar function
    suspend fun searchNovels(query: String, page: Int = 1): List<Novel> = withContext(Dispatchers.IO) {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        val searchUrl = "https://novelbin.me/search?keyword=$encodedQuery&page=$page"
        val novels = mutableListOf<Novel>()

        try {
            val doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0")
                .get()

            val novelElements = doc.select(".list-novel .row")
            for (element in novelElements) {
                val titleElement = element.selectFirst(".novel-title a") ?: continue
                val title = titleElement.text()
                val novelUrl = titleElement.attr("href")
                val id = novelUrl.split("/").lastOrNull() ?: title

                val author = element.selectFirst(".author")?.text() ?: "Unknown Author"
                val latestChapter = element.selectFirst(".col-xs-2 .chapter-title")?.text() ?: ""
                val coverUrl = element.selectFirst("img.cover")?.attr("src")
                val description = "Latest: $latestChapter"

                novels.add(
                    Novel(
                        id = id,
                        title = title,
                        author = author,
                        description = description,
                        url = novelUrl,
                        tags = emptyList(),
                        sourceId = "novelbin",
                        coverUrl = coverUrl
                    )
                )
            }

        } catch (e: Exception) {
            Log.e("NovelBinScraper", "Search failed: ${e.message}")
        }

        novels
    }


    override suspend fun fetchNovelDetails(novelUrl: String): Novel = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(novelUrl)
            .userAgent("Mozilla/5.0")
            .get()

        val title = doc.selectFirst("h3.title[itemprop=name]")?.text() ?: "Unknown Title"
        val author = doc.selectFirst("span[itemprop=author] meta[itemprop=name]")?.attr("content")
            ?: doc.selectFirst("ul.info.info-meta li:contains(Author) a")?.text()
            ?: "Unknown Author"
        val coverUrl = doc.selectFirst("meta[itemprop=image]")?.attr("content")
            ?: doc.selectFirst(".book img.lazy")?.attr("data-src") ?: ""
        val description = doc.selectFirst("div.desc-text[itemprop=description]")?.text()?.trim() ?: ""
        val tags = doc.select("ul.info.info-meta li:has(h3:contains(Genre)) a")
            .map { it.text() }
        val status = doc.selectFirst("ul.info.info-meta li:has(h3:contains(Status)) a")?.text() ?: "Unknown"

        Novel(
            id = novelUrl.substringAfterLast("/"),
            title = title,
            author = author,
            coverUrl = coverUrl,
            description = description,
            tags = tags,
            status = status,
            url = novelUrl,
            sourceId = "novelbin"
        )
    }

    override suspend fun fetchNovelChapters(novelUrl: String): List<Chapter> = withContext(Dispatchers.IO) {
        // Step 1: Fetch main novel page
        val mainDoc = Jsoup.connect(novelUrl)
            .userAgent("Mozilla/5.0")
            .get()

        // Step 2: Extract novel ID from og:url meta tag
        val ogUrl = mainDoc.selectFirst("meta[property=og:url]")?.attr("content")
            ?: return@withContext emptyList()
        val keyId = ogUrl.trimEnd('/').substringAfterLast("/")

        // Step 3: Build AJAX URL
        val ajaxUrl = "https://novelbin.me/ajax/chapter-archive?novelId=$keyId"

        // Step 4: Fetch chapters from AJAX endpoint
        val ajaxDoc = Jsoup.connect(ajaxUrl)
            .userAgent("Mozilla/5.0")
            .ignoreContentType(true)
            .get()

        // Step 5: Parse chapters from AJAX response
        val chapters = mutableListOf<Chapter>()
        val chapterElements = ajaxDoc.select("li a") // adjust selector if needed

        for (element in chapterElements) {
            val chapterTitle = element.text()
            val chapterUrl = element.absUrl("href")
            chapters.add(Chapter(title = chapterTitle, url = chapterUrl, novelUrl = novelUrl))
        }

        chapters
    }


    override suspend fun fetchChapterContent(chapterUrl: String): String = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(chapterUrl)
            .userAgent("Mozilla/5.0")
            .get()

        val chapterContentDiv = doc.selectFirst("div#chr-content")
        if (chapterContentDiv == null) {
            Log.d("NovelBinScraper", "chapter-content div not found")
            return@withContext ""
        }

        // Remove unwanted tags like scripts or ads inside the content div
        chapterContentDiv.select("script, style, div[id^=pf-]").remove()

        chapterContentDiv.html()
    }
}
