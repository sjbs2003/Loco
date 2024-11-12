package com.example.loco.model.network

import android.content.Context
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.loco.model.firebase.FireStoreManager
import com.example.loco.model.room.NoteDao
import com.example.loco.model.room.NoteEntity
import com.example.loco.model.room.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class OfflineNoteRepository(
    private val noteDao: NoteDao,
    private val firestoreManager: FireStoreManager,
    private val networkObserver: NetworkConnectivityObserver,
    private val context: Context
) : NoteRepository
{
    private var currentUserId: String? = null
    private val pendingSyncQueue = mutableListOf<NoteEntity>()
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    override fun getAllNotes(): Flow<List<NoteEntity>> =
        currentUserId?.let { noteDao.getAllNotesForUser(it) }
            ?: throw IllegalStateException("No user logged in")

    override fun getNote(id: Long): Flow<NoteEntity?> =
        currentUserId?.let { noteDao.getNoteByIdForUser(id, it) }
            ?: throw IllegalStateException("No user logged in")

    override suspend fun insertNote(note: NoteEntity) {
        currentUserId?.let { userId ->
            // Special handling for offline user
            val noteWithUserAndStatus = if (userId == "offline_user") {
                note.copy(
                    userId = userId,
                    syncStatus = SyncStatus.SYNCED  // Always SYNCED for offline user
                )
            } else {
                note.copy(
                    userId = userId,
                    syncStatus = SyncStatus.PENDING
                )
            }

            noteDao.insert(noteWithUserAndStatus)

            // Only attempt Firebase sync for non-offline users
            if (userId != "offline_user") {
                when (networkObserver.observe().first()) {
                    is NetworkStatus.Available -> {
                        try {
                            firestoreManager.syncNote(noteWithUserAndStatus, userId)
                            noteDao.updateSyncStatus(noteWithUserAndStatus.id, SyncStatus.SYNCED, userId)
                        } catch (e: Exception) {
                            pendingSyncQueue.add(noteWithUserAndStatus)
                        }
                    }
                    is NetworkStatus.Unavailable -> {
                        pendingSyncQueue.add(noteWithUserAndStatus)
                    }
                }
            }
        } ?: throw IllegalStateException("No user logged in")
    }

    override suspend fun updateNote(note: NoteEntity) {
        currentUserId?.let { userId ->
            // Special handling for offline user
            val noteWithStatus = if (userId == "offline_user") {
                note.copy(syncStatus = SyncStatus.SYNCED)  // Always SYNCED for offline user
            } else {
                note.copy(syncStatus = SyncStatus.PENDING)
            }

            noteDao.update(noteWithStatus)

            // Only attempt Firebase sync for non-offline users
            if (userId != "offline_user") {
                when (networkObserver.observe().first()) {
                    is NetworkStatus.Available -> {
                        try {
                            firestoreManager.syncNote(noteWithStatus, userId)
                            noteDao.updateSyncStatus(note.id, SyncStatus.SYNCED, userId)
                        } catch (e: Exception) {
                            pendingSyncQueue.add(noteWithStatus)
                        }
                    }
                    is NetworkStatus.Unavailable -> {
                        pendingSyncQueue.add(noteWithStatus)
                    }
                }
            }
        } ?: throw IllegalStateException("No user logged in")
    }

    override suspend fun deleteNote(note: NoteEntity) {
        currentUserId?.let { userId ->
            try {
                // First try to delete from Firestore if online
                if (userId != "offline_user") {
                    when (networkObserver.observe().first()) {
                        is NetworkStatus.Available -> {
                            try {
                                // Delete from Firestore first
                                firestoreManager.deleteNote(note.id, userId)
                                // Only delete locally if Firestore deletion succeeds
                                noteDao.delete(note)
                            } catch (e: Exception) {
                                // Log error and potentially retry later
                                throw e // Propagate error to UI
                            }
                        }
                        is NetworkStatus.Unavailable -> {
                            // When offline, mark for deletion instead of deleting
                            noteDao.markForDeletion(note.id) // You'll need to add this method
                            noteDao.delete(note)
                        }
                    }
                } else {
                    // Offline user - just delete locally
                    noteDao.delete(note)
                }
            } catch (e: Exception) {
                throw e
            }
        } ?: throw IllegalStateException("No user logged in")
    }

    override suspend fun syncDeletedNotes() {
        currentUserId?.let { userId ->
            try {
                when (networkObserver.observe().first()) {
                    is NetworkStatus.Available -> {
                        val deletedNotes = noteDao.getNotesMarkedForDeletion()
                        deletedNotes.forEach { note ->
                            try {
                                // Delete from Firestore
                                firestoreManager.deleteNote(note.id, userId)
                                // If successful, permanently delete locally
                                noteDao.permanentlyDelete(note.id)
                            } catch (e: Exception) {
                                // If deletion fails, keep it marked for deletion
                                // Will be retried next sync
                            }
                        }
                    }
                    is NetworkStatus.Unavailable -> {
                        // Do nothing when offline
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "note_image_${System.currentTimeMillis()}.jpg"
            val outputFile = File(context.filesDir, fileName)

            inputStream?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            outputFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun syncWithFireStore(userId: String?) {
        // Skip sync entirely for offline user
        if (userId == "offline_user") return

        // Existing sync logic remains unchanged for regular users
        userId?.let { uid ->
            try {
                // Get local notes that need syncing
                val pendingNotes = noteDao.getNotesBySyncStatusForUser(SyncStatus.PENDING, uid)
                val failedNotes = noteDao.getNotesBySyncStatusForUser(SyncStatus.FAILED, uid)
                val notesToSync = pendingNotes + failedNotes

                notesToSync.forEach { note ->
                    try {
                        firestoreManager.syncNote(note, uid)
                        noteDao.updateSyncStatus(note.id, SyncStatus.SYNCED, uid)
                    } catch (e: Exception) {
                        noteDao.updateSyncStatus(note.id, SyncStatus.FAILED, uid)
                    }
                }

                if (noteDao.getAllNotesForUserOneShot(uid).isEmpty()) {
                    val firestoreNotes = firestoreManager.getAllUserNotes(uid)
                    firestoreNotes.forEach { firestoreNote ->
                        val localNote = noteDao.getNoteByIdForUser(firestoreNote.id, uid).first()
                        if (localNote == null) {
                            noteDao.insert(
                                firestoreNote.copy(
                                    userId = uid,
                                    syncStatus = SyncStatus.SYNCED
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                println("Sync failed: ${e.message}")
            }
        }
    }

    override suspend fun refreshNotes() {
        currentUserId?.let { userId ->
            try {
                when (networkObserver.observe().first()) {
                    is NetworkStatus.Available -> {
                        // Only sync pending local changes to Firestore
                        val pendingNotes = noteDao.getNotesBySyncStatusForUser(SyncStatus.PENDING, userId)
                        pendingNotes.forEach { note ->
                            try {
                                firestoreManager.syncNote(note, userId)
                                noteDao.updateSyncStatus(note.id, SyncStatus.SYNCED, userId)
                            } catch (e: Exception) {
                                // Keep as pending if sync fails
                            }
                        }
                    }
                    is NetworkStatus.Unavailable -> {
                        throw IllegalStateException("Cannot sync while offline")
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        } ?: throw IllegalStateException("No user logged in")
    }

    override fun setCurrentUser(userId: String?) {
        currentUserId = userId
        // Only initiate sync for non-offline users
        if (userId != null && userId != "offline_user") {
            repositoryScope.launch {
                syncWithFireStore(userId)
            }
        }
    }

    override suspend fun updateNoteImage(id: Long, imageUri: String?) {
        currentUserId?.let { userId ->
            // Update image locally first
            noteDao.updateNoteImage(id, imageUri, userId)

            // Skip Firebase sync for offline user
            if (userId != "offline_user") {
                val note = noteDao.getNoteByIdForUser(id, userId).first()

                note?.let { updatedNote ->
                    when (networkObserver.observe().first()) {
                        is NetworkStatus.Available -> {
                            try {
                                noteDao.updateSyncStatus(id, SyncStatus.PENDING, userId)
                                firestoreManager.syncNote(updatedNote, userId)
                                noteDao.updateSyncStatus(id, SyncStatus.SYNCED, userId)
                            } catch (e: Exception) {
                                noteDao.updateSyncStatus(id, SyncStatus.FAILED, userId)
                            }
                        }
                        is NetworkStatus.Unavailable -> {
                            noteDao.updateSyncStatus(id, SyncStatus.PENDING, userId)
                        }
                    }
                }
            }
        } ?: throw IllegalStateException("No user logged in")
    }

    override suspend fun getNoteImageUri(id: Long): String =
        currentUserId?.let { userId ->
            // Simply return the stored image URI from local database
            noteDao.getNoteImageUri(id, userId)
        } ?: throw IllegalStateException("No user logged in")

    private fun observeNetworkStatus() {
        repositoryScope.launch {
            networkObserver.observe().collect { status ->
                when (status) {
                    is NetworkStatus.Available -> {
                        currentUserId?.let { userId ->
                            if (userId != "offline_user") {
                                // First sync deleted notes
                                syncDeletedNotes()

                                // Then sync pending notes
                                val pendingNotes = noteDao.getNotesBySyncStatusForUser(SyncStatus.PENDING, userId)
                                pendingNotes.forEach { note ->
                                    try {
                                        firestoreManager.syncNote(note, userId)
                                        noteDao.updateSyncStatus(note.id, SyncStatus.SYNCED, userId)
                                    } catch (e: Exception) {
                                        // Keep as pending if sync fails
                                    }
                                }
                            }
                        }
                    }
                    is NetworkStatus.Unavailable -> { /* Do nothing */ }
                }
            }
        }
    }

    fun cleanup() {
        repositoryScope.cancel()
    }

    init {
        observeNetworkStatus()
    }
}