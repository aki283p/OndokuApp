package com.example.ondokuapp.repository

import com.example.ondokuapp.model.Novel
import com.example.ondokuapp.model.NovelDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NovelRepository(private val novelDao: NovelDao) {
    
    val allNovels: Flow<List<Novel>> = novelDao.getAllNovels()

    suspend fun getNovelById(id: Long): Novel? = withContext(Dispatchers.IO) {
        novelDao.getNovelById(id)
    }

    suspend fun insertNovel(novel: Novel): Long = withContext(Dispatchers.IO) {
        novelDao.insertNovel(novel)
    }

    suspend fun updateNovel(novel: Novel) = withContext(Dispatchers.IO) {
        novelDao.updateNovel(novel)
    }

    suspend fun deleteNovel(novel: Novel) = withContext(Dispatchers.IO) {
        novelDao.deleteNovel(novel)
    }
}
