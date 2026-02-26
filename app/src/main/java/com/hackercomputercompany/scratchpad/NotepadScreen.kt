package com.hackercomputercompany.scratchpad

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackercomputercompany.scratchpad.data.Note
import com.hackercomputercompany.scratchpad.data.NoteRepository
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val fonts = listOf("Mono", "Default", "Serif", "Sans")
private val fontFamilies = listOf(
    FontFamily.Monospace,
    FontFamily.Default,
    FontFamily.Serif,
    FontFamily.SansSerif
)

private val textSizes = listOf("S", "M", "L", "XL")
private val textSizeValues = listOf(12, 16, 20, 24)

@Composable
fun NotepadScreen() {
    val context = LocalContext.current
    val repo = remember { NoteRepository.getInstance(context) }

    var showBootAnimation by remember { mutableStateOf(true) }
    var currentNoteId by remember { mutableLongStateOf(-1L) }
    var fontIndex by remember { mutableIntStateOf(0) }
    var sizeIndex by remember { mutableIntStateOf(1) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var titleText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (showBootAnimation) {
            delay(2000)
            showBootAnimation = false
        }
        
        val notes = repo.getAllNotes()
        if (notes.isEmpty()) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val uniqueTitle = repo.generateUniqueTitle(dateStr)
            val newId = repo.saveNote(Note(
                id = 0,
                title = uniqueTitle,
                content = "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ))
            currentNoteId = newId
            titleText = uniqueTitle
        } else {
            currentNoteId = notes.first().id
            titleText = notes.first().title
        }
    }

    if (showBootAnimation) {
        BootUpAnimation()
    } else if (currentNoteId > 0) {
        val note = repo.getNoteById(currentNoteId)
        note?.let { n ->
            NotepadContent(
                note = n,
                repo = repo,
                fontIndex = fontIndex,
                sizeIndex = sizeIndex,
                isEditingTitle = isEditingTitle,
                titleText = titleText,
                onTitleClick = { isEditingTitle = true },
                onTitleChange = { newTitle -> titleText = newTitle },
                onTitleConfirm = { newTitle ->
                    repo.saveNote(n.copy(title = newTitle))
                    titleText = newTitle
                    isEditingTitle = false
                },
                onTitleCancel = {
                    titleText = n.title
                    isEditingTitle = false
                },
                onNewNote = {
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val uniqueTitle = repo.generateUniqueTitle(dateStr)
                    val newId = repo.saveNote(Note(
                        id = 0,
                        title = uniqueTitle,
                        content = "",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    ))
                    currentNoteId = newId
                    titleText = uniqueTitle
                },
                onSizeClick = { sizeIndex = (sizeIndex + 1) % textSizes.size },
                onFontClick = { fontIndex = (fontIndex + 1) % fonts.size }
            )
        }
    }
}

@Composable
private fun BootUpAnimation() {
    var loadingText by remember { mutableStateOf("") }

    val bootMessages = remember {
        listOf(
            "ATZ",
            "ATDT 618-781-8424",
            "CONNECT 2400",
            "-= HACKER COMPUTER COMPANY =-",
            "LOADING FILE...",
            "READING SECTOR",
            "LOAD COMPLETE"
        )
    }

    LaunchedEffect(Unit) {
        for (message in bootMessages) {
            message.forEach { char ->
                loadingText += char
                delay(4L)
            }
            loadingText += "\n"
            delay(150L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = loadingText,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                color = Color.Green
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotepadContent(
    note: Note,
    repo: NoteRepository,
    fontIndex: Int,
    sizeIndex: Int,
    isEditingTitle: Boolean,
    titleText: String,
    onTitleClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onTitleConfirm: (String) -> Unit,
    onTitleCancel: () -> Unit,
    onNewNote: () -> Unit,
    onSizeClick: () -> Unit,
    onFontClick: () -> Unit
) {
    var text by remember(note.id) { mutableStateOf(note.content) }
    var originalText by remember(note.id) { mutableStateOf(note.content) }
    var saveStatus by remember { mutableStateOf("---") }

    LaunchedEffect(text) {
        if (text != originalText) {
            saveStatus = ">>>"
            delay(500)
            repo.saveNote(note.copy(content = text))
            originalText = text
            saveStatus = "SAV"
            delay(1500)
            if (text == originalText) {
                saveStatus = "---"
            }
        }
    }

    if (isEditingTitle) {
        TitleEditDialog(
            initialTitle = titleText,
            onConfirm = onTitleConfirm,
            onDismiss = onTitleCancel
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onTitleClick() }
                    ) {
                        Text(
                            text = "[ ${note.title} ]",
                            color = Color.Green
                        )
                        Text(
                            text = " $saveStatus",
                            color = Color.Green.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.Green
                ),
                actions = {
                    IconButton(onClick = onNewNote) {
                        Text("[ + ]", color = Color.Green)
                    }
                    IconButton(onClick = onSizeClick) {
                        Text(textSizes[sizeIndex], color = Color.Green)
                    }
                    IconButton(onClick = onFontClick) {
                        Text(fonts[fontIndex], color = Color.Green)
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        NotepadEditor(
            text = text,
            onTextChange = { text = it },
            fontIndex = fontIndex,
            sizeIndex = sizeIndex,
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )
    }
}

@Composable
private fun TitleEditDialog(
    initialTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Title", color = Color.Green) },
        text = {
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(8.dp)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title) }) {
                Text("OK", color = Color.Green)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Green)
            }
        },
        containerColor = Color.Black
    )
}

@Composable
private fun NotepadEditor(
    text: String,
    onTextChange: (String) -> Unit,
    fontIndex: Int,
    sizeIndex: Int,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier,
        textStyle = TextStyle(
            fontFamily = fontFamilies[fontIndex],
            fontSize = textSizeValues[sizeIndex].sp,
            color = Color.White
        ),
        placeholder = {
            Text(
                text = "> Start typing...",
                style = TextStyle(
                    fontFamily = fontFamilies[fontIndex],
                    fontSize = textSizeValues[sizeIndex].sp,
                    color = Color.Green
                )
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Green,
            unfocusedBorderColor = Color.Green,
            cursorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}
