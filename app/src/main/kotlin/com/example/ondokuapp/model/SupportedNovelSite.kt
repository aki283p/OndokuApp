package com.example.ondokuapp.model

/**
 * 対応小説サイトの情報
 */
data class SupportedNovelSite(
    val name: String,
    val url: String,
    val description: String? = null
)
