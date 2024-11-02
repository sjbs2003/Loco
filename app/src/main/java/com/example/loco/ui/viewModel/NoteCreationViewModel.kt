package com.example.noteapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.noteapp.data.model.NoteEntity
import com.example.noteapp.data.model.NoteRepository
//import com.example.loco.data.NoteEntity
//import com.example.loco.data.NoteRepository
//import com.example.noteapp.NoteApplication
//import com.example.noteapp.data.model.NoteEntity
//import com.example.noteapp.data.model.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class NoteCreationViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _noteState = MutableStateFlow(NoteEntity(title = "", content = "", category = "All", imageUri = null))
    val noteState: StateFlow<NoteEntity> = _noteState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun updateTitle(title: String) {
        _noteState.value = _noteState.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _noteState.value = _noteState.value.copy(content = content)
    }

    fun updateCategory(category: String) {
        _selectedCategory.value = category
        _noteState.value = _noteState.value.copy(category = category)
    }

    fun updateImage(imageUri: String?) {
        _noteState.value = _noteState.value.copy(imageUri = imageUri)
    }

    fun saveNote() {
        viewModelScope.launch {
            repository.insertNote(_noteState.value)
        }
    }
}