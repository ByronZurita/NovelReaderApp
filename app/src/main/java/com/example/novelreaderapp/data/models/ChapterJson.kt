package com.example.novelreaderapp.data.models

// Data class matching the JSON structure in window.chapters JS array
data class ChapterJson(
    val id: Int,
    val volumeId: Int?,
    val title: String,
    val slug: String,
    val date: String,
    val order: Int,
    val visible: Int,
    val subscriptionTiers: Any?,
    val doesNotRollOver: Boolean,
    val isUnlocked: Boolean,
    val url: String
)