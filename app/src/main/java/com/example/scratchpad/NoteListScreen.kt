package com.example.scratchpad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scratchpad.data.Note
import com.example.scratchpad.data.NoteDao
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    noteDao: NoteDao,
    onNoteClick: (Long) -> Unit,
    onCreateNote: () -> Unit
) {
    var showTrash by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val notes by if (showTrash) {
        noteDao.getTrashedNotes()
    } else {
        noteDao.getAllNotes()
    }.collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (showTrash) "Trash" else "Scratchpad", 
                        color = Color.Green,
                        modifier = Modifier.clickable { 
                            if (!showTrash) showAbout = true 
                        }
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.Green
                ),
                actions = {
                    IconButton(onClick = { showTrash = !showTrash }) {
                        Icon(
                            imageVector = if (showTrash) Icons.Default.Shield else Icons.Default.Delete,
                            contentDescription = if (showTrash) "Notes" else "Trash",
                            tint = Color.Green
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!showTrash) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            val count = noteDao.getNoteCount()
                            val newNoteId = noteDao.insertNote(
                                Note(title = "Note ${count + 1}")
                            )
                            onNoteClick(newNoteId)
                        }
                    },
                    containerColor = Color.Green,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Note")
                }
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showTrash) "Trash is empty" else "No notes yet.\nTap + to create one.",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        color = Color.Green
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteItem(
                        note = note,
                        isTrash = showTrash,
                        onClick = { onNoteClick(note.id) },
                        onDelete = {
                            coroutineScope.launch {
                                if (showTrash) {
                                    noteDao.deleteNote(note)
                                } else {
                                    noteDao.softDelete(note.id)
                                }
                            }
                        },
                        onRestore = {
                            coroutineScope.launch {
                                noteDao.restoreNote(note.id)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("-=Hacker Computer Company=-", color = Color.Green) },
            text = {
                Text(
                    text = "Scratchpad v1.1\nA retro-style notepad",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = Color.Green
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text("OK", color = Color.Green)
                }
            },
            containerColor = Color.Black,
            titleContentColor = Color.Green,
            textContentColor = Color.Green
        )
    }
}

@Composable
private fun NoteItem(
    note: Note,
    isTrash: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content.ifEmpty { "(empty)" },
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(if (isTrash) note.deletedAt ?: note.updatedAt else note.updatedAt),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.Green.copy(alpha = 0.6f)
                    )
                )
            }
            if (isTrash) {
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Default.RestoreFromTrash,
                        contentDescription = "Restore",
                        tint = Color.Green
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Forever",
                        tint = Color.Red
                    )
                }
            } else {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Green
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
