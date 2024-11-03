package com.example.loco.ui


import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.loco.LocoApplication

import com.example.loco.viewModel.NoteCreationViewModel
import com.example.loco.viewModel.NoteDetailViewModel
import com.example.loco.viewModel.NoteListViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for NoteListViewModel
        initializer {
            NoteListViewModel(noteApplication().container.noteRepository)
        }

        // Initializer for NoteCreationViewModel
        initializer {
            NoteCreationViewModel(noteApplication().container.noteRepository)
        }

        // Initializer for NoteDetailViewModel
        initializer {
            NoteDetailViewModel(noteApplication().container.noteRepository)
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [NoteApplication].
 */
fun CreationExtras.noteApplication(): LocoApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LocoApplication)
