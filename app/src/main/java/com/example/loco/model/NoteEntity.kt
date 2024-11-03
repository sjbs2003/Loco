package com.example.loco.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "All",
    val creationDate: Long = System.currentTimeMillis(),
    val imageUri: String? = null
)