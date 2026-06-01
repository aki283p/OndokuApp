package com.example.ondokuapp.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NovelDao {
    @Query("SELECT * FROM novels ORDER BY updatedAt DESC")
    fun getAllNovels(): Flow<List<Novel>>

    @Query("SELECT * FROM novels WHERE id = :id")
    suspend fun getNovelById(id: Long): Novel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNovel(novel: Novel): Long

    @Update
    suspend fun updateNovel(novel: Novel)

    @Delete
    suspend fun deleteNovel(novel: Novel)

    @Query("SELECT * FROM novels WHERE title LIKE :query OR content LIKE :query ORDER BY updatedAt DESC")
    fun searchNovels(query: String): Flow<List<Novel>>
}
