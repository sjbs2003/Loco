package com.example.noteapp.data.model

import kotlinx.coroutines.flow.Flow

interface NoteRepository{

    fun getAllNotes(): Flow<List<NoteEntity>>

    fun getNote(id:Long): Flow<NoteEntity?>

    suspend fun insertNote(note: NoteEntity)
    suspend fun deleteNote(note: NoteEntity)
    suspend fun updateNote(note: NoteEntity)
    suspend fun updateNoteImage(id: Long, imageUri:String?)
    suspend fun getNoteImageUri(id: Long): String?
}