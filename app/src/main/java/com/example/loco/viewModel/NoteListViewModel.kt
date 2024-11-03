package com.example.loco.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loco.model.NoteEntity
import com.example.loco.model.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NoteListViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _allNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    private val _filteredNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _filteredNotes.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            combine(
                repository.getAllNotes(),
                _selectedCategory,
                _searchQuery
            ) { allNotes, category, query ->
                filterNotes(allNotes, category, query)
            }.collect { filteredNotes ->
                _allNotes.value = filteredNotes
                _filteredNotes.value = filteredNotes
            }
        }
    }

    fun updateSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun filterNotes(
        notes: List<NoteEntity>,
        category: String,
        query: String
    ): List<NoteEntity> {
        return notes.filter { note ->
            (category == "All" || note.category == category) &&
                    (note.title.contains(query, ignoreCase = true) ||
                            note.content.contains(query, ignoreCase = true))
        }
    }
}