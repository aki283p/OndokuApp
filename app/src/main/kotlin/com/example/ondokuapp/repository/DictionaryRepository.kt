package com.example.ondokuapp.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.ondokuapp.model.UserDictionaryEntry
import java.util.UUID

class DictionaryRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_dictionary", Context.MODE_PRIVATE)

    fun loadEntries(): List<UserDictionaryEntry> {
        val ids = prefs.getStringSet("entry_ids", emptySet()) ?: emptySet()
        return ids.mapNotNull { id ->
            val from = prefs.getString("$id.from", null)
            val to = prefs.getString("$id.to", null)
            if (from != null && to != null) {
                UserDictionaryEntry(id, from, to)
            } else {
                null
            }
        }.sortedBy { it.from }
    }

    fun saveEntries(entries: List<UserDictionaryEntry>) {
        val editor = prefs.edit()
        // Clear old entries (simplified)
        val oldIds = prefs.getStringSet("entry_ids", emptySet()) ?: emptySet()
        oldIds.forEach { id ->
            editor.remove("$id.from")
            editor.remove("$id.to")
        }

        val newIds = entries.map { it.id }.toSet()
        editor.putStringSet("entry_ids", newIds)
        entries.forEach { entry ->
            editor.putString("${entry.id}.from", entry.from)
            editor.putString("${entry.id}.to", entry.to)
        }
        editor.apply()
    }

    fun addEntry(from: String, to: String): UserDictionaryEntry {
        val id = UUID.randomUUID().toString()
        val entry = UserDictionaryEntry(id, from, to)
        val current = loadEntries().toMutableList()
        current.add(entry)
        saveEntries(current)
        return entry
    }

    fun updateEntry(entry: UserDictionaryEntry) {
        val current = loadEntries().map {
            if (it.id == entry.id) entry else it
        }
        saveEntries(current)
    }

    fun deleteEntry(entry: UserDictionaryEntry) {
        val current = loadEntries().filter { it.id != entry.id }
        saveEntries(current)
    }

    /**
     * 初期データの提供
     */
    fun getInitialEntries(): List<UserDictionaryEntry> {
        return listOf(
            UserDictionaryEntry(UUID.randomUUID().toString(), "異世界", "いせかい"),
            UserDictionaryEntry(UUID.randomUUID().toString(), "魔王", "まおう"),
            UserDictionaryEntry(UUID.randomUUID().toString(), "勇者", "ゆうしゃ")
        )
    }
}
