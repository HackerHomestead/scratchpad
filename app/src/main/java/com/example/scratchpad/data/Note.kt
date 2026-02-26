package com.example.scratchpad.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "Note",
    val content: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
