package com.example.scratchpad

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.scratchpad.data.NoteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * BroadcastReceiver for handling import intents from ADB.
 * 
 * Receives:
 * - IMPORT_ACTION: Import notes from base64-encoded JSON
 * 
 * @see JsonNoteUtils for JSON parsing
 * @see Constants for intent action names
 */
class ImportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteDao = NoteDatabase.getDatabase(context).noteDao()
        
        when (intent.action) {
            Constants.Actions.IMPORT_ACTION -> {
                val filePath = intent.getStringExtra(Constants.Extras.FILE_PATH)
                val base64Data = intent.getStringExtra(Constants.Extras.BASE64_DATA)
                android.util.Log.d("ImportReceiver", "Import action received, file: $filePath, hasBase64: ${base64Data != null}")
                
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val json = withContext(Dispatchers.IO) {
                            when {
                                base64Data != null -> JsonNoteUtils.decodeBase64(base64Data)
                                filePath != null -> {
                                    try {
                                        java.io.FileInputStream(filePath).use { stream ->
                                            stream.bufferedReader().readText()
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("ImportReceiver", "File read error: ${e.message}")
                                        null
                                    }
                                }
                                else -> null
                            }
                        }
                        
                        if (!json.isNullOrBlank()) {
                            val notes = JsonNoteUtils.parseJsonNotes(json)
                            noteDao.insertAll(notes)
                            Toast.makeText(context, "Imported ${notes.size} notes", Toast.LENGTH_SHORT).show()
                            android.util.Log.d("ImportReceiver", "Imported ${notes.size} notes")
                        } else {
                            Toast.makeText(context, "Failed to read data", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ImportReceiver", "Import error: ${e.message}", e)
                        Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
