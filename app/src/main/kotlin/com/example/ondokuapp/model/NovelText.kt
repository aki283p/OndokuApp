package com.example.ondokuapp.model

data class NovelText(
    val title: String = "",
    val content: String = "",
    val currentPosition: Int = 0,
    val sourceUrl: String? = null
)
