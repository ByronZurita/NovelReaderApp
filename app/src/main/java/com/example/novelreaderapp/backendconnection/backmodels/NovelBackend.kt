package com.example.novelreaderapp.backendconnection.backmodels

import com.google.gson.annotations.SerializedName

data class NovelBackend(
    @SerializedName("_id") val id: String? = null,
    val title: String,
    val author: String,
    val status: String,
    val chapters: Int,
    val totalChapters: Int,
    val notes: String,
    val userId: String = ""
)
