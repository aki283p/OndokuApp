package com.example.ondokuapp.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

object TextCleaner {
    /**
     * 既存呼び出し互換のための関数。
     * HTMLタグらしきものがあればHTMLとして、そうでなければプレーンテキストとして整形する。
     */
    fun clean(text: String): String {
        if (text.isBlank()) return ""
        return if (text.contains("<") && text.contains(">")) {
            cleanHtml(text)
        } else {
            cleanPlainText(text)
        }
    }

    /**
     * HTML文字列を整形する。
     * 改行(br)や段落(p, div)を適切に処理してからタグを除去する。
     */
    fun cleanHtml(html: String): String {
        val doc = Jsoup.parse(html)
        return cleanHtml(doc.body())
    }

    /**
     * JsoupのElementを解析して、改行構造を保持したテキストを抽出する。
     */
    fun cleanHtml(element: Element): String {
        val sb = StringBuilder()
        
        fun traverse(node: org.jsoup.nodes.Node) {
            when (node) {
                is TextNode -> {
                    sb.append(node.text())
                }
                is Element -> {
                    val tagName = node.tagName()
                    val isBlock = node.isBlock || tagName == "br" || tagName == "p"
                    
                    if (isBlock && sb.isNotEmpty() && !sb.endsWith("\n")) {
                        sb.append("\n")
                    }
                    
                    for (child in node.childNodes()) {
                        traverse(child)
                    }
                    
                    if (isBlock && !sb.endsWith("\n")) {
                        sb.append("\n")
                    }
                }
            }
        }
        
        traverse(element)
        return cleanPlainText(sb.toString())
    }

    /**
     * プレーンテキストを整形する。
     * ルビの除去と、余分な空白・改行の整理を行う。
     */
    fun cleanPlainText(text: String): String {
        if (text.isBlank()) return ""

        // ルビ記法の除去
        // ｜漢字《かんじ》 -> 漢字
        // 漢字《かんじ》 -> 漢字
        var cleaned = text.replace(Regex("[｜|]([^《\\n]+)《[^》\\n]+》"), "$1")
        cleaned = cleaned.replace(Regex("([^《\\n\\s　]+)《[^》\\n]+》"), "$1")

        return cleaned
            .lines()
            .map { it.trim() }
            .joinToString("\n")
            .replace(Regex("\\n{3,}"), "\n\n") // 3回以上の改行を2回に制限
            .replace(Regex("[ 　]+"), " ")      // 連続する空白を1つに
            .trim()
    }

    /**
     * 文または段落単位に分割する。
     * 1チャンクが長すぎる場合は適度に分割する（TTSの制約や表示のため）。
     */
    fun splitIntoChunks(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        
        // 改行または句点、感嘆符、疑問符で分割
        val regex = Regex("(?<=[。！？\\n])")
        val initialChunks = text.split(regex)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            
        val result = mutableListOf<String>()
        val maxChunkLength = 500 // 1チャンクの最大文字数目安
        
        for (chunk in initialChunks) {
            if (chunk.length > maxChunkLength) {
                // 長すぎる場合は文字数で分割（本来は読点などで切りたいが簡略化）
                chunk.chunked(maxChunkLength).forEach { result.add(it) }
            } else {
                result.add(chunk)
            }
        }
        
        return result
    }
}
