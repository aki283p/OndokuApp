package com.example.ondokuapp.util

import org.jsoup.Jsoup

object TextCleaner {
    /**
     * HTMLタグを除去し、余分な空白や改行を整理する。
     * ルビ記法も簡易的に除去する。
     */
    fun clean(text: String): String {
        if (text.isBlank()) return ""
        
        // HTMLタグ除去
        val noHtml = Jsoup.parse(text).text()
        
        // ルビ記法の除去
        // ｜漢字《かんじ》 -> 漢字
        // 漢字《かんじ》 -> 漢字
        var cleaned = noHtml.replace(Regex("[｜|]([^《\\n]+)《[^》\\n]+》"), "$1")
        cleaned = cleaned.replace(Regex("([^《\\n\\s]+)《[^》\\n]+》"), "$1")

        return cleaned
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n\n")
            .replace(Regex("\\n{3,}"), "\n\n") // 3回以上の改行を2回に制限
            .replace(Regex("[ 　]+"), " ")      // 連続する空白を1つに
    }

    /**
     * 文または段落単位に分割する
     */
    fun splitIntoChunks(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        // 改行または句点、感嘆符、疑問符で分割
        val regex = Regex("(?<=[。！？\\n])")
        return text.split(regex)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
