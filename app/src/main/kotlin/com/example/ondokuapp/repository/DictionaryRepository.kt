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
        // Clear all metadata based on current saved IDs
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

    fun addEntry(from: String, to: String): UserDictionaryEntry? {
        val trimmedFrom = from.trim()
        val trimmedTo = to.trim()
        
        if (trimmedFrom.isEmpty() || trimmedTo.isEmpty()) return null
        
        val current = loadEntries().toMutableList()
        val existingIndex = current.indexOfFirst { it.from == trimmedFrom }
        
        return if (existingIndex != -1) {
            val updated = current[existingIndex].copy(to = trimmedTo)
            current[existingIndex] = updated
            saveEntries(current)
            updated
        } else {
            val id = UUID.randomUUID().toString()
            val entry = UserDictionaryEntry(id, trimmedFrom, trimmedTo)
            current.add(entry)
            saveEntries(current)
            entry
        }
    }

    fun updateEntry(entry: UserDictionaryEntry) {
        val trimmedFrom = entry.from.trim()
        val trimmedTo = entry.to.trim()
        
        if (trimmedFrom.isEmpty() || trimmedTo.isEmpty()) return

        val current = loadEntries().toMutableList()
        val index = current.indexOfFirst { it.id == entry.id }
        
        if (index != -1) {
            // Check for duplicate 'from' in other entries
            val duplicateIndex = current.indexOfFirst { it.from == trimmedFrom && it.id != entry.id }
            if (duplicateIndex != -1) {
                // If the new 'from' matches another entry, delete this one and update that one?
                // Or simpler: just don't allow update if it creates a duplicate.
                // But instructions say "already same from -> overwrite".
                // In update case, if user changes A->B to C->B, and C already exists, 
                // we should probably merge them or just update the existing C and remove the old A.
                current.removeAt(index)
                val existingC = current[current.indexOfFirst { it.from == trimmedFrom }]
                current[current.indexOfFirst { it.from == trimmedFrom }] = existingC.copy(to = trimmedTo)
            } else {
                current[index] = entry.copy(from = trimmedFrom, to = trimmedTo)
            }
            saveEntries(current)
        }
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
