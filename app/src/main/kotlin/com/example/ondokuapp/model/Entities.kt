package com.example.ondokuapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "novels")
data class Novel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String, // 互換性維持のため保持
    val author: String? = null,
    val sourceUrl: String? = null,
    val sourceSite: String? = null,
    /**
     * 将来的に読み上げ位置（文字数など）を保存するために使用する。
     * 現時点のMVPでは、チャンク（段落）のインデックスとして使用。
     */
    val currentPosition: Int = 0,
    val isFavorite: Boolean = false,
    val lastReadEpisodeId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastReadAt: Long? = null
)

@Entity(tableName = "episodes")
data class Episode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val novelId: Long,
    val title: String,
    val content: String,
    val episodeUrl: String? = null,
    val episodeIndex: Int,
    val currentPosition: Int = 0,
    val isDownloaded: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
