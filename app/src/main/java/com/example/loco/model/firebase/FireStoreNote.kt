package com.example.loco.model.firebase

import com.example.loco.model.room.NoteEntity
import com.google.firebase.Timestamp

data class FireStoreNote(
    val id: Long = 0,
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "All",
    val creationDate: Timestamp = Timestamp.now(),
    val imageUri: String? = null,
    val lastModified: Timestamp = Timestamp.now()
) {
    fun toNoteEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            title = title,
            content = content,
            category = category,
            creationDate = creationDate.seconds * 1000, // Convert to milliseconds
            imageUri = imageUri
        )
    }

    companion object {
        fun fromNoteEntity(note: NoteEntity, userId: String): FireStoreNote {
            return FireStoreNote(
                id = note.id,
                userId = userId,
                title = note.title,
                content = note.content,
                category = note.category,
                creationDate = Timestamp(note.creationDate / 1000, 0), // Convert from milliseconds
                imageUri = note.imageUri,
                lastModified = Timestamp.now()
            )
        }
    }
}