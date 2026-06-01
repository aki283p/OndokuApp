package com.example.ondokuapp.repository

import com.example.ondokuapp.model.Novel
import com.example.ondokuapp.model.NovelDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

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

    /**
     * 将来的に外部サイト（小説家になろう等）から小説を取り込む際に使用する
     * 現在はMVPのため、簡易的なJoupによるパースのみ実装
     */
    suspend fun fetchNovelFromUrl(url: String): Novel = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(url).get()
        
        // 汎用的なメタデータまたは「なろう」等の特定クラスをターゲットに取得
        val title = doc.select(".novel_title, title").text()
        val content = doc.select(".novel_view, #novel_honbun, article, main").text()
        
        return@withContext Novel(
            title = title,
            content = content,
            sourceUrl = url
        )
    }
}
