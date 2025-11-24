package com.byron.novelreaderapp.data.scraper.base

import com.byron.novelreaderapp.data.models.Chapter
import com.byron.novelreaderapp.data.models.Novel

interface   NovelScraper {

    suspend fun fetchNovels(): List<Novel>
    suspend fun fetchNovelChapters(novelUrl: String): List<Chapter>
    suspend fun fetchChapterContent(chapterUrl: String): String
    suspend fun fetchNovelDetails(novelUrl: String): Novel
}
