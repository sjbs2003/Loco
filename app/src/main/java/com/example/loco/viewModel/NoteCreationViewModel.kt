package com.example.loco.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.loco.model.room.NoteEntity
import com.example.loco.model.network.NoteRepository
import com.example.loco.model.notifications.NotificationWorker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class ReminderUiState(
    val date: String = "",
    val time: String = "",
    val isReminderSet: Boolean = false,
    val reminderTimestamp: Long = 0L
)

class NoteCreationViewModel(private val repository: NoteRepository) : ViewModel() {
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    // Initialize noteState with current user ID
    private val _noteState = MutableStateFlow(
        NoteEntity(
            title = "",
            content = "",
            category = "All",
            imageUri = null,
            userId = requireNotNull(currentUserId) { "User must be logged in to create notes" }
        )
    )
    val noteState: StateFlow<NoteEntity> = _noteState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _reminderState = MutableStateFlow(ReminderUiState())
    val reminderState: StateFlow<ReminderUiState> = _reminderState.asStateFlow()

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

    fun setReminder(context: Context, timestamp: Long, formattedDate: String, formattedTime: String) {
        val delay = timestamp - System.currentTimeMillis()
        if (delay <= 0) return

        val noteData = Data.Builder()
            .putLong(NotificationWorker.KEY_NOTE_ID, noteState.value.id)
            .putString(NotificationWorker.KEY_NOTE_TITLE, noteState.value.title)
            .putString(NotificationWorker.KEY_NOTE_CONTENT, noteState.value.content)
            .build()

        val reminderRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(noteData)
            .build()

        WorkManager.getInstance(context).enqueue(reminderRequest)

        _reminderState.value = ReminderUiState(
            date = formattedDate,
            time = formattedTime,
            isReminderSet = true,
            reminderTimestamp = timestamp
        )
    }

    fun saveNote() {
        val userId = currentUserId ?: return  // Don't save if no user is logged in

        viewModelScope.launch {
            // Ensure the note has the current user ID when saving
            val noteToSave = _noteState.value.copy(userId = userId)
            repository.insertNote(noteToSave)
        }
    }

    init {
        // Verify user is logged in when ViewModel is created
        requireNotNull(currentUserId) { "User must be logged in to create notes" }
    }
}