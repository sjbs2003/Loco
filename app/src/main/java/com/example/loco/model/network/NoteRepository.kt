package com.example.loco.model.network

import com.example.loco.model.room.NoteEntity
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<NoteEntity>>
    fun getNote(id: Long): Flow<NoteEntity?>
    suspend fun insertNote(note: NoteEntity)
    suspend fun deleteNote(note: NoteEntity)
    suspend fun updateNote(note: NoteEntity)
    suspend fun updateNoteImage(id: Long, imageUri: String?)
    suspend fun getNoteImageUri(id: Long): String?
    suspend fun syncWithFireStore(userId: String?)
    fun setCurrentUser(userId: String?)
    suspend fun refreshNotes()  // Add new method to force refresh
}