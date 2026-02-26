package com.example.scratchpad

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
        AndroidView(
            factory = { ctx ->
                EditText(ctx).apply {
                    setText(text)
                    setTextColor(Color.Green.toArgb())
                    setBackgroundColor(Color.Black.toArgb())
                    textSize = 16f
                    typeface = Typeface.MONOSPACE
                    setHintTextColor(Color.Green.toArgb())
                    hint = "> Start typing..."
                    setPadding(0, 16, 16, 16)
                    setOnScrollChangeListener { _, _, scrollY, _, _ -> }
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
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        )
    }
}
