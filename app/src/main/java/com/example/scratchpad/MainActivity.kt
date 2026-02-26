package com.example.scratchpad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.scratchpad.data.NoteDatabase
import com.example.scratchpad.ui.theme.ScratchpadTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NoteDatabase.getDatabase(applicationContext)
        val noteDao = database.noteDao()

        setContent {
            ScratchpadTheme {
                ScratchpadApp(noteDao = noteDao)
            }
        }
    }
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
