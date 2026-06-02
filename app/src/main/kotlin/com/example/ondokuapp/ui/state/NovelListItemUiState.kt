package com.example.ondokuapp.ui.state

import com.example.ondokuapp.model.Novel

/**
 * 本棚画面の各アイテムの表示用状態
 */
data class NovelListItemUiState(
    val novel: Novel,
    val episodeCount: Int,
    val lastReadEpisodeTitle: String?,
    val hasProgress: Boolean
)
