package com.example.loco.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loco.model.NoteEntity
import com.example.loco.model.NoteRepository
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