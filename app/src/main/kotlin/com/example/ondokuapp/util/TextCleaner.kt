package com.example.ondokuapp.util

import org.jsoup.Jsoup

object TextCleaner {
    /**
     * HTMLタグを除去し、余分な空白や改行を整理する
     */
    fun clean(text: String): String {
        if (text.isBlank()) return ""
        
        // HTMLタグ除去 (Jsoupを使用)
        val doc = Jsoup.parse(text)
        val noHtml = doc.text()
        
        return noHtml
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n\n")
    }

    /**
     * 文または段落単位に分割する
     */
    fun splitIntoChunks(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        // 改行または句点、感嘆符、疑問符で分割（簡易版）
        val regex = Regex("(?<=[。！？\\n])")
        return text.split(regex)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
