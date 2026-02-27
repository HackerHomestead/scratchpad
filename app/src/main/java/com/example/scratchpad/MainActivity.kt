package com.example.scratchpad

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.scratchpad.data.Note
import com.example.scratchpad.data.NoteDatabase
import com.example.scratchpad.ui.theme.ScratchpadTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NoteDatabase.getDatabase(applicationContext)
        val noteDao = database.noteDao()

        handleIntent(intent, noteDao)

        setContent {
            ScratchpadTheme {
                ScratchpadApp(noteDao = noteDao)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val database = NoteDatabase.getDatabase(applicationContext)
        val noteDao = database.noteDao()
        handleIntent(intent, noteDao)
    }

    private fun handleIntent(intent: Intent?, noteDao: com.example.scratchpad.data.NoteDao) {
        // Check for base64 data (ADB import)
        val base64Data = intent?.getStringExtra("base64_data")
        if (base64Data != null) {
            handleBase64Import(base64Data, noteDao)
            return
        }
        
        when (intent?.action) {
            "com.example.scratchpad.IMPORT" -> {
                // For file URI imports (requires proper permissions)
                intent.data?.let { uri ->
                    coroutineScope.launch {
                        try {
                            val json = withContext(Dispatchers.IO) {
                                contentResolver.openInputStream(uri)?.use { stream ->
                                    BufferedReader(stream.reader()).readText()
                                }
                            }
                            if (json != null) {
                                val notes = parseJsonNotes(json)
                                noteDao.insertAll(notes)
                                Toast.makeText(this@MainActivity, "Imported ${notes.size} notes", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            "com.example.scratchpad.EXPORT" -> {
                coroutineScope.launch {
                    try {
                        val notes = noteDao.getAllNotesOnce()
                        val json = notesToJson(notes)
                        Toast.makeText(this@MainActivity, "Export: ${notes.size} notes ready (output to log)", Toast.LENGTH_SHORT).show()
                        android.util.Log.d("SCRATCHPAD_EXPORT", json)
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            "com.example.scratchpad.CLEAR" -> {
                coroutineScope.launch {
                    try {
                        noteDao.deleteAllNotes()
                        Toast.makeText(this@MainActivity, "All notes cleared", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Clear failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            "com.example.scratchpad.LIST" -> {
                coroutineScope.launch {
                    try {
                        val notes = noteDao.getAllNotesOnce()
                        val list = notes.joinToString("\n") { "${it.id}: ${it.title}" }
                        Toast.makeText(this@MainActivity, "${notes.size} notes (see log)", Toast.LENGTH_SHORT).show()
                        android.util.Log.d("SCRATCHPAD_LIST", list)
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "List failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            "com.example.scratchpad.ABOUT" -> {
                Toast.makeText(this, "Scratchpad v1.1 - Hacker Computer Company", Toast.LENGTH_LONG).show()
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

    private fun notesToJson(notes: List<Note>): String {
        val jsonArray = JSONArray()
        notes.forEach { note ->
            val obj = org.json.JSONObject()
            obj.put("title", note.title)
            obj.put("content", note.content)
            obj.put("updatedAt", note.updatedAt)
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    private fun handleBase64Import(base64Data: String, noteDao: com.example.scratchpad.data.NoteDao) {
        coroutineScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT).toString(Charsets.UTF_8)
                }
                android.util.Log.d("SCRATCHPAD", "Decoded JSON, length: ${json.length}")
                
                val notes = parseJsonNotes(json)
                noteDao.insertAll(notes)
                Toast.makeText(this@MainActivity, "Imported ${notes.size} notes", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val coroutineScope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main)
}

@Composable
fun ScratchpadApp(noteDao: com.example.scratchpad.data.NoteDao) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "noteList"
    ) {
        composable("noteList") {
            NoteListScreen(
                noteDao = noteDao,
                onNoteClick = { noteId ->
                    navController.navigate("noteEditor/$noteId")
                },
                onCreateNote = { /* FAB in NoteListScreen creates note and navigates */ }
            )
        }

        composable(
            route = "noteEditor/{noteId}",
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: return@composable
            NotepadScreen(
                noteId = noteId,
                noteDao = noteDao,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
