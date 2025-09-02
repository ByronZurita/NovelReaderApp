package com.example.novelreaderapp.data.scraper.base

import com.example.novelreaderapp.data.models.Chapter
import com.example.novelreaderapp.data.models.Novel

interface   NovelScraper {

    suspend fun fetchNovels(): List<Novel>
    suspend fun fetchNovelChapters(novelUrl: String): List<Chapter>
    suspend fun fetchChapterContent(chapterUrl: String): String
    suspend fun fetchNovelDetails(novelUrl: String): Novel
}
