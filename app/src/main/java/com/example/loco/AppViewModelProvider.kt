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
            val context = (this [APPLICATION_KEY] as Application)
            AuthViewModel().apply {
                initializeGoogleSignIn(
                    context = context,
                    webClientId = context.getString(R.string.web_client_id)
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

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [NoteApplication].
 */
fun CreationExtras.noteApplication(): LocoApplication =
    (this[APPLICATION_KEY] as LocoApplication)