package com.example.scratchpad

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scratchpad.ui.theme.BbsGreenDark
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("notepad", Context.MODE_PRIVATE)

    var text by remember { mutableStateOf(prefs.getString("content", "") ?: "") }
    var originalText by remember { mutableStateOf(text) }
    var saveStatus by remember { mutableStateOf("---") }

    val lines = remember(text) { text.split("\n") }
    val lineData = remember(lines) {
        lines.mapIndexed { index, line ->
            if (line.isEmpty()) {
                listOf("")
            } else {
                val charWidth = 8.5f
                val maxChars = 40
                val wrappedLines = (line.length / maxChars) + 1
                (1..wrappedLines).map { wrapIndex ->
                    if (wrapIndex == 1) (index + 1).toString() else "."
                }
            }
        }.flatten()
    }

    val verticalScrollState = rememberScrollState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scratchpad  [$saveStatus]") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.Green
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(32.dp)
                    .fillMaxHeight()
                    .verticalScroll(verticalScrollState)
                    .background(Color.Black)
            ) {
                lineData.forEach { label ->
                    Text(
                        text = label.padStart(3),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            color = BbsGreenDark,
                            textAlign = TextAlign.End
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp)
                    )
                }
            }

            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 8.dp)
                    .verticalScroll(verticalScrollState),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = Color.Green
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Green)
            )
        }
    }
}
