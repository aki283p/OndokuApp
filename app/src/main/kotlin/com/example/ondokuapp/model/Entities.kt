package com.example.ondokuapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "novels")
data class Novel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    /**
     * 将来的に読み上げ位置（文字数など）を保存するために使用する。
     * 現時点のMVPでは、チャンク（段落）のインデックスとして使用。
     */
    val currentPosition: Int = 0,
    val sourceUrl: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastReadAt: Long? = null
)
