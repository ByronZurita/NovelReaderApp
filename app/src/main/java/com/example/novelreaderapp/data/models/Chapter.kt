package com.example.novelreaderapp.data.models

data class Chapter(
    val url: String,
    val title: String,
    val content: String? = null,
    val novelUrl: String
)