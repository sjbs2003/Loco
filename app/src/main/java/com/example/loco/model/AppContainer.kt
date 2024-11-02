package com.example.noteapp.data.model

import android.content.Context

interface AppContainer{
    val noteRepository: NoteRepository
}

class AppDataContainer(private val context: Context): AppContainer{
    override val noteRepository: NoteRepository by lazy {
        OfflineNoteRepository(NoteDatabase.getDatabase(context).noteDao())
    }
}