package com.example.novelreaderapp.data.models

data class Novel(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val url: String,
    val tags: List<String> = emptyList(),
    val sourceId: String,
    val coverUrl: String? = null
)