package com.example.loco


import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.loco.viewModel.AuthViewModel
import com.example.loco.viewModel.NoteCreationViewModel
import com.example.loco.viewModel.NoteDetailViewModel
import com.example.loco.viewModel.NoteListViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {

        // Initializer for AuthViewModel
        initializer {
            val application = (this [APPLICATION_KEY] as LocoApplication)
            AuthViewModel(application, noteApplication().container.noteRepository).apply {
                initializeGoogleSignIn(
                    context = application,
                    webClientId = application.getString(R.string.web_client_id)
                )
            }
        }

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

fun CreationExtras.noteApplication(): LocoApplication =
    (this[APPLICATION_KEY] as LocoApplication)