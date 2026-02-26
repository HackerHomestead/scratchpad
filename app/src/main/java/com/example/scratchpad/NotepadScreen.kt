package com.example.scratchpad

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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

private val bootMessages = listOf(
    "ATZ",
    "ATDT 618-781-8424",
    "CONNECT",
    "CARRIER 2400",
    "PROTOCOL: FUNK",
    "TERM> "
)

private val loadMessages = listOf(
    "LOADING FILE...",
    "READING SECTOR",
    "LOAD COMPLETE"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("notepad", Context.MODE_PRIVATE)

    var showLoading by remember { mutableStateOf(true) }
    var showContentLoading by remember { mutableStateOf(true) }
    var loadingText by remember { mutableStateOf("") }
    var lineIndex by remember { mutableStateOf(0) }
    var charIndex by remember { mutableStateOf(0) }
    var text by remember { mutableStateOf("") }
    var originalText by remember { mutableStateOf("") }
    var saveStatus by remember { mutableStateOf("---") }
    var fontIndex by remember { mutableStateOf(0) }
    var sizeIndex by remember { mutableStateOf(1) }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        if (showLoading) {
            while (lineIndex < bootMessages.size) {
                val line = bootMessages[lineIndex]
                while (charIndex < line.length) {
                    loadingText += line[charIndex]
                    charIndex++
                    delay(4L)
                }
                if (lineIndex < bootMessages.size - 1) {
                    loadingText += "\n"
                }
                lineIndex++
                charIndex = 0
                delay(200L)
            }
            delay(300)
            showLoading = false
            
            loadingText = ""
            lineIndex = 0
            charIndex = 0
            
            while (lineIndex < loadMessages.size) {
                val line = loadMessages[lineIndex]
                while (charIndex < line.length) {
                    loadingText += line[charIndex]
                    charIndex++
                    delay(3L)
                }
                if (lineIndex < loadMessages.size - 1) {
                    loadingText += "\n"
                }
                lineIndex++
                charIndex = 0
                delay(150L)
            }
            delay(300)
            showContentLoading = false
            text = prefs.getString("content", "") ?: ""
            originalText = text
        }
    }

    LaunchedEffect(text) {
        if (text != originalText) {
            saveStatus = ">>>"
            delay(500)
            prefs.edit().putString("content", text).apply()
            originalText = text
            saveStatus = "SAV"
            delay(1500)
            if (text == originalText) {
                saveStatus = "---"
            }
        }
    }

    if (showLoading || showContentLoading) {
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
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Scratchpad  [$saveStatus]") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.Green
                    ),
                    actions = {
                        IconButton(onClick = { sizeIndex = (sizeIndex + 1) % textSizes.size }) {
                            Text(
                                text = textSizes[sizeIndex],
                                color = Color.Green
                            )
                        }
                        IconButton(onClick = { fontIndex = (fontIndex + 1) % fonts.size }) {
                            Text(
                                text = fonts[fontIndex],
                                color = Color.Green
                            )
                        }
                    }
                )
            },
            containerColor = Color.Black
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(innerPadding)
                    .padding(16.dp)
                    .drawBehind {
                        drawRect(
                            color = Color.Green,
                            size = this.size
                        )
                    }
                    .padding(2.dp)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        EditText(ctx).apply {
                            setText(text)
                            setTextColor(Color.White.toArgb())
                            setBackgroundColor(Color.Black.toArgb())
                            textSize = textSizeValues[sizeIndex].toFloat()
                            typeface = fontFamilies[fontIndex]
                            setHintTextColor(Color.Green.toArgb())
                            hint = "> Start typing..."
                            setPadding(24, 16, 24, 16)
                            isSingleLine = false
                            setHorizontallyScrolling(false)
                            transformationMethod = null
                            gravity = android.view.Gravity.TOP or android.view.Gravity.START
                            addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                override fun afterTextChanged(s: Editable?) {
                                    text = s?.toString() ?: ""
                                }
                            })
                        }
                    },
                    update = { editText ->
                        if (editText.text.toString() != text) {
                            val selection = editText.selectionStart
                            editText.setText(text)
                            editText.setSelection(selection.coerceIn(0, text.length))
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                )
            }
        }
    }
}
