package com.example.noteapp.data.model

import kotlinx.coroutines.flow.Flow

class OfflineNoteRepository(private val noteDao: NoteDao): NoteRepository {
    override fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    override fun getNote(id: Long): Flow<NoteEntity?> = noteDao.getNoteById(id)

    override suspend fun insertNote(note: NoteEntity) = noteDao.insert(note)

    override suspend fun updateNote(note: NoteEntity) = noteDao.update(note)

    override suspend fun deleteNote(note: NoteEntity) = noteDao.delete(note)

    override suspend fun getNoteImageUri(id: Long): String? = noteDao.getNoteImageUri(id)

    override suspend fun updateNoteImage(id: Long, imageUri: String?) = noteDao.updateNoteImage(id, imageUri)
}