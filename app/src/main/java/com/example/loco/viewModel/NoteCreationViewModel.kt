package com.example.loco.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.loco.model.network.NoteRepository
import com.example.loco.model.notifications.NotificationWorker
import com.example.loco.model.room.NoteEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class ReminderUiState(
    val date: String = "",
    val time: String = "",
    val isReminderSet: Boolean = false,
    val reminderTimestamp: Long = 0L
)

data class FontSettings(
    val titleFont: FontFamily = FontFamily.Default,
    val contentFont: FontFamily = FontFamily.Default
)

class NoteCreationViewModel(
    application: Application,
    private val repository: NoteRepository
) : AndroidViewModel(application) {

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "offline_user" // Return offline_user if not authenticated

    // Initialize noteState with current user ID
    private val _noteState = MutableStateFlow(
        NoteEntity(
            title = "",
            content = "",
            category = "All",
            imageUri = null,
            userId = currentUserId
        )
    )
    val noteState: StateFlow<NoteEntity> = _noteState.asStateFlow()

    private val _fontSettings = MutableStateFlow(FontSettings())
    val fontSettings: StateFlow<FontSettings> = _fontSettings.asStateFlow()

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

    fun updateImage(uri: Uri) {
        viewModelScope.launch {
            try {
                // Get the application context
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
                _noteState.value = _noteState.value.copy(imageUri = internalUri)
            } catch (e: Exception) {
                // Handle error - you might want to show a message to the user
                e.printStackTrace()
            }
        }
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
        viewModelScope.launch {
            val userId = currentUserId
            // Ensure the note has the current user ID when saving
            val noteToSave = _noteState.value.copy(userId = userId)
            repository.insertNote(noteToSave)
        }
    }

    fun updateTitleFont(font: FontFamily) {
        _fontSettings.value = _fontSettings.value.copy(titleFont = font)
    }

    fun updateContentFont(font: FontFamily) {
        _fontSettings.value = _fontSettings.value.copy(contentFont = font)
    }
}