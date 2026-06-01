package com.example.ondokuapp.repository

import android.util.Patterns
import com.example.ondokuapp.model.Novel
import com.example.ondokuapp.util.TextCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.UnknownHostException

class NovelImportRepository {
    
    suspend fun fetchFromUrl(url: String): Result<Novel> = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext Result.failure(Exception("URLを入力してください。"))
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return@withContext Result.failure(Exception("正しいURLを入力してください（http:// または https://）"))
        }
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            return@withContext Result.failure(Exception("URLの形式が正しくありません。"))
        }

        try {
            val doc = Jsoup.connect(url)
                .timeout(15000)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .get()
            
            // 不要な要素を削除
            doc.select("script, style, nav, header, footer, aside, iframe, noscript, form, button, .ads, .comment, .social-share").remove()
            
            // タイトル抽出
            val ogTitle = doc.select("meta[property=og:title]").attr("content")
            val docTitle = doc.title()
            val h1 = doc.select("h1").firstOrNull()?.text()
            val h2 = doc.select("h2").firstOrNull()?.text()
            
            var title = (if (ogTitle.isNotBlank()) ogTitle else if (docTitle.isNotBlank()) docTitle else h1 ?: h2 ?: "無題").trim()
            title = title.substringBefore(" - ").substringBefore(" | ").trim()
            if (title.isBlank()) title = "無題"

            // 本文抽出の優先度
            val contentSelectors = arrayOf(
                "article", "main", 
                "div[class*=novel]", "div[class*=content]", "div[class*=text]",
                "div[id*=novel]", "div[id*=content]", "div[id*=text]",
                "body"
            )
            
            var bestElement = doc.body()
            for (selector in contentSelectors) {
                val found = doc.select(selector).firstOrNull()
                if (found != null && found.text().length > 200) {
                    bestElement = found
                    break
                }
            }

            // 改行構造を保持して抽出
            var cleanedContent = TextCleaner.cleanHtml(bestElement)
            
            // 抽出結果が極端に短い場合は body 全体から text() でフォールバックを試みる
            if (cleanedContent.length < 50 && bestElement.tagName() != "body") {
                val fallback = TextCleaner.cleanHtml(doc.body())
                if (fallback.length > cleanedContent.length) {
                    cleanedContent = fallback
                }
            }
            
            if (cleanedContent.length < 10) {
                return@withContext Result.failure(Exception("本文の抽出に失敗したか、内容が短すぎます。"))
            }

            // 品質チェック：同じ行が極端に連続している場合の簡易的な整理
            val lines = cleanedContent.lines()
            val dedupedLines = mutableListOf<String>()
            var lastLine = ""
            var repeatCount = 0
            for (line in lines) {
                if (line == lastLine && line.isNotBlank()) {
                    repeatCount++
                    if (repeatCount < 3) { // 3回以上の連続は無視する
                        dedupedLines.add(line)
                    }
                } else {
                    dedupedLines.add(line)
                    lastLine = line
                    repeatCount = 0
                }
            }
            cleanedContent = dedupedLines.joinToString("\n")

            Result.success(Novel(
                title = title,
                content = cleanedContent,
                sourceUrl = url
            ))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("サイトにアクセスできませんでした。通信状態やURLを確認してください。"))
        } catch (e: Exception) {
            Result.failure(Exception("エラーが発生しました: ${e.message}"))
        }
    }
}
