package com.example.ondokuapp.repository

import com.example.ondokuapp.model.Novel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NovelRepository {
    
    suspend fun fetchNovelFromUrl(url: String): Novel = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(url).get()
        
        // Simple heuristic for generic web pages or specific support for Narou
        val title = doc.select(".novel_title, title").text()
        val content = doc.select(".novel_view, #novel_honbun, article, main").text()
        
        return@withContext Novel(
            title = title,
            content = content,
            sourceUrl = url
        )
    }
}
