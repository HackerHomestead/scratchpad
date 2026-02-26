package com.example.scratchpad.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)

class NoteRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
    
    fun getAllNotes(): List<Note> {
        val json = prefs.getString("notes_json", "[]") ?: "[]"
        return parseNotes(json)
    }
    
    fun getNoteById(id: Long): Note? {
        return getAllNotes().find { it.id == id }
    }
    
    fun getNoteByTitle(title: String): Note? {
        return getAllNotes().find { it.title == title }
    }
    
    fun saveNote(note: Note): Long {
        val notes = getAllNotes().toMutableList()
        val existingIndex = notes.indexOfFirst { it.id == note.id }
        
        if (existingIndex >= 0) {
            notes[existingIndex] = note.copy(updatedAt = System.currentTimeMillis())
        } else {
            notes.add(note)
        }
        
        saveNotes(notes)
        return note.id
    }
    
    fun generateUniqueTitle(baseTitle: String): String {
        var title = baseTitle
        var counter = 0
        while (getNoteByTitle(title) != null) {
            counter++
            title = "$baseTitle ($counter)"
        }
        return title
    }
    
    private fun saveNotes(notes: List<Note>) {
        val jsonArray = JSONArray()
        notes.forEach { note ->
            val jsonObject = JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("content", note.content)
                put("createdAt", note.createdAt)
                put("updatedAt", note.updatedAt)
            }
            jsonArray.put(jsonObject)
        }
        prefs.edit().putString("notes_json", jsonArray.toString()).apply()
    }
    
    private fun parseNotes(json: String): List<Note> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                Note(
                    id = obj.getLong("id"),
                    title = obj.getString("title"),
                    content = obj.getString("content"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt")
                )
            }.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
