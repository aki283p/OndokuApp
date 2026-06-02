package com.example.ondokuapp.repository

import com.example.ondokuapp.model.Episode
import com.example.ondokuapp.model.EpisodeDao
import kotlinx.coroutines.flow.Flow

class EpisodeRepository(private val episodeDao: EpisodeDao) {
    fun getEpisodesByNovelId(novelId: Long): Flow<List<Episode>> {
        return episodeDao.getEpisodesByNovelId(novelId)
    }

    suspend fun getEpisodeById(id: Long): Episode? {
        return episodeDao.getEpisodeById(id)
    }

    suspend fun insertEpisode(episode: Episode): Long {
        return episodeDao.insertEpisode(episode)
    }

    suspend fun insertEpisodes(episodes: List<Episode>) {
        episodeDao.insertEpisodes(episodes)
    }

    suspend fun updateEpisode(episode: Episode) {
        episodeDao.updateEpisode(episode)
    }

    suspend fun deleteEpisode(episode: Episode) {
        episodeDao.deleteEpisode(episode)
    }

    suspend fun deleteEpisodesByNovelId(novelId: Long) {
        episodeDao.deleteEpisodesByNovelId(novelId)
    }

    suspend fun getEpisodeCount(novelId: Long): Int {
        return episodeDao.getEpisodeCount(novelId)
    }

    suspend fun hasEpisodesWithProgress(novelId: Long): Boolean {
        return episodeDao.hasEpisodesWithProgress(novelId)
    }
}
