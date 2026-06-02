package com.example.ondokuapp.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE novelId = :novelId ORDER BY episodeIndex ASC")
    fun getEpisodesByNovelId(novelId: Long): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE id = :id")
    suspend fun getEpisodeById(id: Long): Episode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: Episode): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<Episode>)

    @Update
    suspend fun updateEpisode(episode: Episode)

    @Delete
    suspend fun deleteEpisode(episode: Episode)

    @Query("DELETE FROM episodes WHERE novelId = :novelId")
    suspend fun deleteEpisodesByNovelId(novelId: Long)

    @Query("SELECT COUNT(*) FROM episodes WHERE novelId = :novelId")
    suspend fun getEpisodeCount(novelId: Long): Int
}
