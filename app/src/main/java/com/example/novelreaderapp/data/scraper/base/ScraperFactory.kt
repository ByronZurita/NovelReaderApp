package com.example.novelreaderapp.data.scraper.base

import com.example.novelreaderapp.data.scraper.NovelBinScraper
import com.example.novelreaderapp.data.scraper.RoyalRoadScraper

object ScraperFactory {
    fun getScraper(identifier: String): NovelScraper {
        return when {
            identifier.contains("royalroad.com", ignoreCase = true) ||
                    identifier.equals("royalroad", ignoreCase = true) -> RoyalRoadScraper()

            identifier.contains("novelbin.me", ignoreCase = true) ||
                    identifier.equals("novelbin", ignoreCase = true) -> NovelBinScraper()

            // Add more site checks here
            else -> throw IllegalArgumentException("No scraper available for this URL or source ID: $identifier")
        } as NovelScraper
    }
}
