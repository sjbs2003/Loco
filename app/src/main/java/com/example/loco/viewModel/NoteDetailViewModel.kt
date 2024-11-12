package com.example.loco.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.loco.model.network.NoteRepository
import com.example.loco.model.room.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class NoteDetailViewModel(
    application: Application,
    private val repository: NoteRepository
) : AndroidViewModel(application) {

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

    fun updateImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext

                // Create a file in the app's internal storage
                val fileName = "note_image_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)

                // Copy the image to internal storage
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                // Update the note with the internal file path
                val internalUri = file.absolutePath
                _noteState.value = _noteState.value?.copy(imageUri = internalUri)

                // Save to repository
                _noteState.value?.let { note ->
                    repository.updateNoteImage(note.id, internalUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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