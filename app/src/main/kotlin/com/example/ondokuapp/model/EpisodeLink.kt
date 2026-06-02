package com.example.ondokuapp.model

/**
 * 作品ページから抽出された各話のリンク情報（一時モデル）
 */
data class EpisodeLink(
    val title: String,
    val url: String,
    val index: Int
)
