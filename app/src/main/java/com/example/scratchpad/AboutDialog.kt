package com.example.scratchpad

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable About dialog with Hacker Computer Company branding.
 * 
 * Used in:
 * - NoteListScreen: Tap on "Scratchpad" title
 * - NotepadScreen: Menu > About
 * 
 * @param version The app version to display
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun AboutDialog(
    version: String = Constants.VERSION_NAME,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "-=Hacker Computer Company=-",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    color = Color.Green
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Scratchpad",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        color = Color.Green
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Version $version",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color.Green.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A retro-style notepad with 1980s BBS aesthetics.",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.Green.copy(alpha = 0.7f)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "OK",
                    color = Color.Green
                )
            }
        },
        containerColor = Color.Black,
        titleContentColor = Color.Green,
        textContentColor = Color.Green
    )
}
