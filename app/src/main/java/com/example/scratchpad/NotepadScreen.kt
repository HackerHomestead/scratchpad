package com.example.scratchpad

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.core.content.edit
import kotlinx.coroutines.delay

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
    val prefs = remember { context.getSharedPreferences("notepad", Context.MODE_PRIVATE) }
    var showBootAnimation by remember { mutableStateOf(true) }

    if (showBootAnimation) {
        BootUpAnimation(onFinished = { showBootAnimation = false })
    } else {
        NotepadContent(prefs)
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
private fun NotepadContent(prefs: SharedPreferences) {
    var text by remember { mutableStateOf(prefs.getString("content", "") ?: "") }
    var originalText by remember { mutableStateOf(text) }
    var saveStatus by remember { mutableStateOf("---") }
    var fontIndex by remember { mutableIntStateOf(0) }
    var sizeIndex by remember { mutableIntStateOf(1) }

    LaunchedEffect(text) {
        if (text != originalText) {
            saveStatus = ">>>"
            delay(500)
            prefs.edit { putString("content", text) }
            originalText = text
            saveStatus = "SAV"
            delay(1500)
            if (text == originalText) {
                saveStatus = "---"
            }
        }
    }

    Scaffold(
        topBar = {
            NotepadTopAppBar(
                saveStatus = saveStatus,
                sizeIndex = sizeIndex,
                fontIndex = fontIndex,
                onSizeClick = { sizeIndex = (sizeIndex + 1) % textSizes.size },
                onFontClick = { fontIndex = (fontIndex + 1) % fonts.size }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotepadTopAppBar(
    saveStatus: String,
    sizeIndex: Int,
    fontIndex: Int,
    onSizeClick: () -> Unit,
    onFontClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Scratchpad  [$saveStatus]") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black,
            titleContentColor = Color.Green
        ),
        actions = {
            IconButton(onClick = onSizeClick) {
                Text(
                    text = textSizes[sizeIndex],
                    color = Color.Green
                )
            }
            IconButton(onClick = onFontClick) {
                Text(
                    text = fonts[fontIndex],
                    color = Color.Green
                )
            }
        }
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
