package com.example.noteapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.noteapp.data.model.NoteEntity
import com.example.noteapp.data.model.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteDetailViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _noteState = MutableStateFlow<NoteEntity?>(null)
    val noteState: StateFlow<NoteEntity?> = _noteState.asStateFlow()

    fun loadNote(noteId: Long) {
        viewModelScope.launch {
            repository.getNote(noteId).collect { note ->
                _noteState.value = note
            }
        }
    }

    fun updateTitle(title: String) {
        _noteState.value = _noteState.value?.copy(title = title)
    }

    fun updateContent(content: String) {
        _noteState.value = _noteState.value?.copy(content = content)
    }

    fun updateImage(imageUri: String?) {
        _noteState.value = _noteState.value?.copy(imageUri = imageUri)
    }

    fun saveNote() {
        viewModelScope.launch {
            _noteState.value?.let { repository.updateNote(it) }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            _noteState.value?.let { repository.deleteNote(it) }
        }
    }
}