package com.example.ondokuapp.repository

import com.example.ondokuapp.model.Novel
import com.example.ondokuapp.util.TextCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NovelImportRepository {
    
    suspend fun fetchFromUrl(url: String): Result<Novel> = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .timeout(10000)
                .get()
            
            // タイトル抽出
            val title = doc.select("h1, .novel_title, title").firstOrNull()?.text() ?: "無題"
            
            // 本文抽出（汎用的なタグを優先）
            val contentElement = doc.select(".novel_view, #novel_honbun, article, main, body").firstOrNull()
            val rawContent = contentElement?.text() ?: ""
            val cleanedContent = TextCleaner.clean(rawContent)
            
            Result.success(Novel(
                title = title,
                content = cleanedContent,
                sourceUrl = url
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
