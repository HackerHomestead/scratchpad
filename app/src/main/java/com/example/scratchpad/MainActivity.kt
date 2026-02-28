package com.example.scratchpad

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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

/**
 * Main entry point for the Scratchpad application.
 * 
 * Handles:
 * - Navigation between note list and editor
 * - Intent processing for ADB commands
 * - Share intents from other apps
 * 
 * @see <a href="https://github.com/HackerHomestead/scratchpad">Project Documentation</a>
 */
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

    /**
     * Process incoming intents for ADB commands and share actions.
     * 
     * Supported intents:
     * - ACTION_SEND: Share text from other apps
     * - IMPORT: Import notes from JSON (via base64)
     * - EXPORT: Export notes to Downloads
     * - CLEAR: Delete all notes
     * - LIST: List notes to logcat
     * - ABOUT: Show about dialog
     * 
     * @param intent The incoming intent to process
     * @param noteDao Database access object
     */
    private fun handleIntent(intent: Intent?, noteDao: com.example.scratchpad.data.NoteDao) {
        // Check for share intent (from other apps)
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                handleShareIntent(sharedText.take(Constants.Limits.MAX_CONTENT_LENGTH), noteDao)
                return
            }
        }
        
        // Check for base64 data (ADB import)
        val base64Data = intent?.getStringExtra(Constants.Extras.BASE64_DATA)
        if (base64Data != null) {
            handleBase64Import(base64Data, noteDao)
            return
        }
        
        when (intent?.action) {
            Constants.Actions.IMPORT -> {
                // For file URI imports (requires proper permissions)
                intent.data?.let { uri ->
                    coroutineScope.launch {
                        try {
                            val json = withContext(Dispatchers.IO) {
                                contentResolver.openInputStream(uri)?.use { stream ->
                                    stream.bufferedReader().readText()
                                }
                            }
                            if (json != null) {
                                val notes = JsonNoteUtils.parseJsonNotes(json)
                                noteDao.insertAll(notes)
                                showToast("Imported ${notes.size} notes")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Import failed", e)
                            showToast("Import failed: ${e.message}")
                        }
                    }
                }
            }
            Constants.Actions.EXPORT -> {
                coroutineScope.launch {
                    try {
                        val notes = noteDao.getAllNotesOnce()
                        val json = JsonNoteUtils.notesToJson(notes)
                        
                        val fileName = "${Constants.Files.BACKUP_PREFIX}${System.currentTimeMillis()}${Constants.Files.BACKUP_EXTENSION}"
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val contentValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                            }
                            
                            val uri = contentResolver.insert(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                contentValues
                            )
                            
                            uri?.let {
                                contentResolver.openOutputStream(it)?.use { outputStream ->
                                    outputStream.write(json.toByteArray())
                                }
                                showToast("Backup saved: Downloads/$fileName")
                            } ?: run {
                                showToast("Backup failed")
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val file = java.io.File(downloadsDir, fileName)
                            file.writeText(json)
                            showToast("Backup saved: Downloads/$fileName")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Export failed", e)
                        showToast("Export failed: ${e.message}")
                    }
                }
            }
            Constants.Actions.CLEAR -> {
                coroutineScope.launch {
                    try {
                        noteDao.deleteAllNotes()
                        showToast("All notes cleared")
                    } catch (e: Exception) {
                        showToast("Clear failed: ${e.message}")
                    }
                }
            }
            Constants.Actions.LIST -> {
                coroutineScope.launch {
                    try {
                        val notes = noteDao.getAllNotesOnce()
                        val list = notes.joinToString("\n") { "${it.id}: ${it.title}" }
                        showToast("${notes.size} notes")
                        android.util.Log.d("SCRATCHPAD_LIST", list)
                    } catch (e: Exception) {
                        showToast("List failed: ${e.message}")
                    }
                }
            }
            Constants.Actions.ABOUT -> {
                showAboutDialog()
            }
        }
    }

    /**
     * Show toast message safely, handling case where toast system may be unavailable.
     */
    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "Toast failed: ${e.message}")
        }
    }

    /**
     * Handle import from base64-encoded JSON data.
     */
    private fun handleBase64Import(base64Data: String, noteDao: com.example.scratchpad.data.NoteDao) {
        coroutineScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    JsonNoteUtils.decodeBase64(base64Data)
                }
                
                val notes = JsonNoteUtils.parseJsonNotes(json)
                noteDao.insertAll(notes)
                showToast("Imported ${notes.size} notes")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Base64 import failed", e)
                showToast("Import failed: ${e.message}")
            }
        }
    }

    /**
     * Handle share intent from another app.
     * Shows dialog to set note title before saving.
     */
    private fun handleShareIntent(sharedContent: String, noteDao: com.example.scratchpad.data.NoteDao) {
        coroutineScope.launch {
            try {
                val noteCount = noteDao.getNoteCount()
                val defaultTitle = "Note ${noteCount + 1}"
                
                runOnUiThread {
                    showTitleDialog(defaultTitle) { title ->
                        coroutineScope.launch {
                            val newNote = Note(
                                title = title.ifBlank { defaultTitle }.take(Constants.Limits.MAX_TITLE_LENGTH),
                                content = sharedContent,
                                updatedAt = System.currentTimeMillis()
                            )
                            noteDao.insertNote(newNote)
                            showToast("Note saved: ${newNote.title}")
                        }
                    }
                }
            } catch (e: Exception) {
                showToast("Failed to save: ${e.message}")
            }
        }
    }

    /**
     * Show dialog for entering note title during share operation.
     */
    private fun showTitleDialog(defaultTitle: String, onTitleSelected: (String) -> Unit) {
        var title by mutableStateOf(defaultTitle)
        
        setContent {
            ScratchpadTheme {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Save Shared Text", color = Color.Green) },
                    text = {
                        Column {
                            Text(
                                text = "Enter a title for the new note:",
                                style = TextStyle(color = Color.Green)
                            )
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it.uppercase().take(Constants.Limits.MAX_TITLE_LENGTH) },
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Green
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { onTitleSelected(title.ifBlank { defaultTitle }) }) {
                            Text("Save", color = Color.Green)
                        }
                    },
                    containerColor = Color.Black,
                    titleContentColor = Color.Green,
                    textContentColor = Color.Green
                )
            }
        }
    }

    /**
     * Show the about dialog.
     */
    private fun showAboutDialog() {
        setContent {
            ScratchpadTheme {
                AboutDialog(
                    version = Constants.VERSION_NAME,
                    onDismiss = { }
                )
            }
        }
    }

    private val coroutineScope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main)
}

/**
 * Main navigation composable for the app.
 * 
 * Routes:
 * - noteList: List of all notes
 * - noteEditor/{noteId}: Edit a specific note
 * 
 * @param noteDao Database access object
 */
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
