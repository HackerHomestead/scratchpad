package com.example.scratchpad

import android.util.Base64
import com.example.scratchpad.data.Note
import org.json.JSONArray
import org.json.JSONObject

/**
 * Utility class for JSON serialization/deserialization of notes.
 * 
 * Used for:
 * - Exporting notes to JSON backup files
 * - Importing notes from JSON
 * - ADB backup/restore operations
 * 
 * @see <a href="https://github.com/HackerHomestead/scratchpad">Project Documentation</a>
 * @see Constants
 */
object JsonNoteUtils {

    private const val KEY_TITLE = "title"
    private const val KEY_CONTENT = "content"
    private const val KEY_UPDATED_AT = "updatedAt"

    /**
     * Parse a JSON string into a list of Note objects.
     * 
     * @param json The JSON string to parse
     * @return List of parsed Note objects
     * @throws IllegalArgumentException if JSON is malformed
     */
    fun parseJsonNotes(json: String): List<Note> {
        if (json.isBlank()) {
            return emptyList()
        }
        
        val notes = mutableListOf<Note>()
        val jsonArray = JSONArray(json)
        
        for (i in 0 until jsonArray.length()) {
            try {
                val obj = jsonArray.getJSONObject(i)
                notes.add(
                    Note(
                        title = obj.optString(KEY_TITLE, "Imported").take(Constants.Limits.MAX_TITLE_LENGTH),
                        content = obj.optString(KEY_CONTENT, "").take(Constants.Limits.MAX_CONTENT_LENGTH),
                        updatedAt = obj.optLong(KEY_UPDATED_AT, System.currentTimeMillis())
                    )
                )
            } catch (e: Exception) {
                // Skip malformed entries but log the error
                android.util.Log.w("JsonNoteUtils", "Failed to parse note at index $i: ${e.message}")
            }
        }
        
        return notes
    }

    /**
     * Convert a list of Notes to a JSON string.
     * 
     * @param notes The list of notes to serialize
     * @return JSON string representation
     */
    fun notesToJson(notes: List<Note>): String {
        val jsonArray = JSONArray()
        
        notes.forEach { note ->
            val obj = JSONObject().apply {
                put(KEY_TITLE, note.title)
                put(KEY_CONTENT, note.content)
                put(KEY_UPDATED_AT, note.updatedAt)
            }
            jsonArray.put(obj)
        }
        
        return jsonArray.toString(2)
    }

    /**
     * Decode base64-encoded JSON data.
     * 
     * @param base64Data The base64-encoded string
     * @return The decoded JSON string
     * @throws IllegalArgumentException if decoding fails
     */
    fun decodeBase64(base64Data: String): String {
        return try {
            Base64.decode(base64Data, Base64.DEFAULT).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to decode base64 data: ${e.message}", e)
        }
    }

    /**
     * Encode a string to base64.
     * 
     * @param data The string to encode
     * @return Base64-encoded string
     */
    fun encodeBase64(data: String): String {
        return Base64.encodeToString(data.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
    }
}
