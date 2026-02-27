package com.example.scratchpad

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Base64
import android.widget.Toast
import com.example.scratchpad.data.Note
import com.example.scratchpad.data.NoteDao
import com.example.scratchpad.data.NoteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader

class ImportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteDao = NoteDatabase.getDatabase(context).noteDao()
        
        when (intent.action) {
            "com.example.scratchpad.IMPORT_ACTION" -> {
                val filePath = intent.getStringExtra("file_path")
                val base64Data = intent.getStringExtra("base64_data")
                android.util.Log.d("SCRATCHPAD_RECEIVER", "Import action received, file: $filePath, hasBase64: ${base64Data != null}")
                
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val json = withContext(Dispatchers.IO) {
                            when {
                                base64Data != null -> {
                                    String(Base64.decode(base64Data, Base64.DEFAULT))
                                }
                                filePath != null -> {
                                    try {
                                        java.io.FileInputStream(filePath).use { stream ->
                                            BufferedReader(stream.reader()).readText()
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("SCRATCHPAD_RECEIVER", "File read error: ${e.message}")
                                        null
                                    }
                                }
                                else -> null
                            }
                        }
                        
                        if (json != null && json.isNotEmpty()) {
                            val notes = parseJsonNotes(json)
                            noteDao.insertAll(notes)
                            Toast.makeText(context, "Imported ${notes.size} notes", Toast.LENGTH_SHORT).show()
                            android.util.Log.d("SCRATCHPAD_RECEIVER", "Imported ${notes.size} notes")
                        } else {
                            Toast.makeText(context, "Failed to read data", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SCRATCHPAD_RECEIVER", "Import error: ${e.message}", e)
                        Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun parseJsonNotes(json: String): List<Note> {
        val notes = mutableListOf<Note>()
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            notes.add(
                Note(
                    title = obj.optString("title", "Imported"),
                    content = obj.optString("content", ""),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }
        return notes
    }
}
