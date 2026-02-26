package com.example.scratchpad

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("notepad", Context.MODE_PRIVATE)

    var text by remember { mutableStateOf(prefs.getString("content", "") ?: "") }
    var originalText by remember { mutableStateOf(text) }
    var saveStatus by remember { mutableStateOf("---") }

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
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                color = Color.Green
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Green)
        )
    }
}
