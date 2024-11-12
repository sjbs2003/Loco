package com.example.loco.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val content: String,
    val category: String = "All",
    val creationDate: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val isMarkedForDeletion: Boolean = false  // Add this field
)

enum class SyncStatus {
    SYNCED,
    PENDING,
    FAILED,
    IDLE
}