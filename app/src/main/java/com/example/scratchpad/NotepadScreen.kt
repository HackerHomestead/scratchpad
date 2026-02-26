package com.example.scratchpad

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scratchpad.data.Note
import com.example.scratchpad.data.NoteDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val textSizes = listOf("S", "M", "L", "XL")
private val textSizeValues = listOf(12, 16, 20, 24)

private fun formatTitleTo83(input: String): String {
    val sanitized = input.replace(Regex("[^a-zA-Z0-9\\-_#<>]"), "")
    return when {
        sanitized.length <= 8 -> sanitized
        sanitized.contains(".") -> {
            val parts = sanitized.split(".", limit = 2)
            val name = parts[0].take(8)
            val ext = parts.getOrElse(1) { "" }.take(3)
            "$name.$ext"
        }
        else -> sanitized.take(8)
    }
}

private fun isValid83Format(input: String): Boolean {
    if (input.isEmpty()) return true
    val pattern = Regex("^[a-zA-Z0-9\\-_#<>]{1,8}(\\.[a-zA-Z0-9\\-_#<>]{1,3})?$")
    return pattern.matches(input)
}

@Composable
fun NotepadScreen(
    noteId: Long,
    noteDao: NoteDao,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showBootAnimation by remember { mutableStateOf(false) }

    if (showBootAnimation) {
        BootUpAnimation(onFinished = { showBootAnimation = false })
    } else {
        NotepadContent(
            noteId = noteId,
            noteDao = noteDao,
            onNavigateBack = onNavigateBack,
            context = context
        )
    }
}

@Composable
private fun BootUpAnimation(onFinished: () -> Unit) {
    var loadingText by remember { mutableStateOf("") }

    val bootMessages = remember {
        listOf(
            "ATZ",
            "ATDT 618-781-8424",
            "CONNECT 2400",
            "-= HACKER COMPUTER COMPANY =-"
        )
    }

    val loadMessages = remember {
        listOf(
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
            delay(200L)
        }
        delay(300)

        loadingText = ""

        for (message in loadMessages) {
            message.forEach { char ->
                loadingText += char
                delay(3L)
            }
            loadingText += "\n"
            delay(150L)
        }
        delay(300)
        onFinished()
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
    noteId: Long,
    noteDao: NoteDao,
    onNavigateBack: () -> Unit,
    context: Context
) {
    val coroutineScope = rememberCoroutineScope()
    var note by remember { mutableStateOf<Note?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var originalContent by remember { mutableStateOf("") }
    var originalTitle by remember { mutableStateOf("") }
    var saveStatus by remember { mutableStateOf("---") }
    var sizeIndex by remember { mutableIntStateOf(1) }
    var isTitleEditing by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFileName by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        note = noteDao.getNoteById(noteId)
        if (note == null) {
            onNavigateBack()
            return@LaunchedEffect
        }
        note?.let {
            title = it.title
            content = it.content
            originalTitle = it.title
            originalContent = it.content
        }
    }

    fun saveNote() {
        coroutineScope.launch {
            note?.let { currentNote ->
                val updatedNote = currentNote.copy(
                    title = title,
                    content = content,
                    updatedAt = System.currentTimeMillis()
                )
                noteDao.updateNote(updatedNote)
                note = updatedNote
            }
        }
    }

    LaunchedEffect(content, title) {
        if (content != originalContent || title != originalTitle) {
            saveStatus = ">>>"
            delay(500)
            saveNote()
            originalContent = content
            originalTitle = title
            saveStatus = "SAV"
            delay(1500)
            if (content == originalContent && title == originalTitle) {
                saveStatus = "---"
            }
        }
    }

    fun exportNote(fileName: String) {
        coroutineScope.launch {
            saveStatus = "EXP"

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val uri = context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    uri?.let {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(content.toByteArray())
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = java.io.File(downloadsDir, fileName)
                    file.writeText(content)
                }
                saveStatus = "EXP OK"
            } catch (e: Exception) {
                saveStatus = "EXP ER"
            }

            delay(1500)
            if (content == originalContent && title == originalTitle) {
                saveStatus = "---"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isTitleEditing) {
                        BasicTextField(
                            value = title,
                            onValueChange = { newValue ->
                                val formatted = formatTitleTo83(newValue.uppercase())
                                title = formatted
                                titleError = !isValid83Format(formatted)
                            },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                color = if (titleError) Color.Red else Color.Green
                            ),
                            cursorBrush = SolidColor(if (titleError) Color.Red else Color.Green),
                            modifier = Modifier
                                .clickable { }
                        )
                    } else {
                        Text(
                            text = "[$title]",
                            modifier = Modifier.clickable { 
                                titleError = false
                                isTitleEditing = true 
                            },
                            color = Color.Green
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        saveNote()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Green
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.Green
                ),
                actions = {
                    Text(
                        text = saveStatus,
                        color = Color.Green.copy(alpha = 0.7f),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(onClick = { sizeIndex = (sizeIndex + 1) % textSizes.size }) {
                        Text(
                            text = textSizes[sizeIndex],
                            color = Color.Green
                        )
                    }
                    IconButton(onClick = { 
                        exportFileName = if (title.isNotEmpty()) title else "NOTE"
                        showExportDialog = true 
                    }) {
                        Icon(
                            imageVector = Icons.Filled.FileDownload,
                            contentDescription = "Export",
                            tint = Color.Green
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        NotepadEditor(
            text = content,
            onTextChange = { content = it },
            sizeIndex = sizeIndex,
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )
    }

    if (showExportDialog) {
        val fullPath = "Downloads/${exportFileName.ifEmpty { "NOTE" }}"
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export File") },
            text = {
                Column {
                    OutlinedTextField(
                        value = exportFileName,
                        onValueChange = { exportFileName = formatTitleTo83(it.uppercase()) },
                        label = { Text("File name (8.3 format)") },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = Color.Green
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = fullPath,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.Green.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val fileName = if (exportFileName.contains(".")) exportFileName else "$exportFileName.txt"
                    exportNote(fileName)
                    showExportDialog = false
                }) {
                    Text("Export", color = Color.Green)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel", color = Color.Green)
                }
            },
            containerColor = Color.Black,
            titleContentColor = Color.Green,
            textContentColor = Color.Green
        )
    }
}

@Composable
private fun NotepadEditor(
    text: String,
    onTextChange: (String) -> Unit,
    sizeIndex: Int,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier,
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = textSizeValues[sizeIndex].sp,
            color = Color.White
        ),
        placeholder = {
            Text(
                text = "> Start typing...",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
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
