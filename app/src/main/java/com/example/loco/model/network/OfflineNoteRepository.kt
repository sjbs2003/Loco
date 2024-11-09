package com.example.loco.model.network

import com.example.loco.model.firebase.FireStoreManager
import com.example.loco.model.room.NoteDao
import com.example.loco.model.room.NoteEntity
import com.example.loco.model.room.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class OfflineNoteRepository(
    private val noteDao: NoteDao,
    private val firestoreManager: FireStoreManager,
    private val networkObserver: NetworkConnectivityObserver
) : NoteRepository {
    private var isNewDevice = true
    private var currentUserId: String? = null
    private val pendingSyncQueue = mutableListOf<NoteEntity>()
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getAllNotes(): Flow<List<NoteEntity>> {
        return if (isNewDevice) {
            // For new device, get notes directly from Firestore
            currentUserId?.let { uid ->
                firestoreManager.getUserNotes(uid)
            } ?: flow { emit(emptyList()) }
        } else {
            // For existing device, get notes from local storage
            noteDao.getAllNotes()
        }
    }

    override fun getNote(id: Long): Flow<NoteEntity?> = noteDao.getNoteById(id)

    fun cleanup() {
        repositoryScope.cancel()
    }

    override suspend fun insertNote(note: NoteEntity) {
        val noteWithStatus = note.copy(syncStatus = SyncStatus.PENDING)

        if (!isNewDevice) {
            // Store in local database only if this is not a new device
            noteDao.insert(noteWithStatus)
        }

        // Always sync new notes to Firebase
        currentUserId?.let { userId ->
            try {
                firestoreManager.syncNote(noteWithStatus, userId)
                if (!isNewDevice) {
                    noteDao.updateSyncStatus(note.id, SyncStatus.SYNCED)
                } else{
                    TODO("handle new device case")
                }
            } catch (e: Exception) {
                if (!isNewDevice) {
                    noteDao.updateSyncStatus(note.id, SyncStatus.FAILED)
                }
                pendingSyncQueue.add(noteWithStatus)
            }
        }
    }


    override suspend fun updateNote(note: NoteEntity) {
        val noteWithStatus = note.copy(syncStatus = SyncStatus.PENDING)
        noteDao.update(noteWithStatus)

        currentUserId?.let { userId ->
            try {
                firestoreManager.syncNote(noteWithStatus, userId)
                noteDao.updateSyncStatus(note.id, SyncStatus.SYNCED)
            } catch (e: Exception) {
                noteDao.updateSyncStatus(note.id, SyncStatus.FAILED)
            }
        }
    }

    override suspend fun deleteNote(note: NoteEntity) {
        noteDao.delete(note)
        currentUserId?.let { userId ->
            firestoreManager.deleteNote(note.id, userId)
        }
    }

    override suspend fun getNoteImageUri(id: Long): String? = noteDao.getNoteImageUri(id)


    override suspend fun updateNoteImage(id: Long, imageUri: String?) {
        noteDao.updateNoteImage(id, imageUri)
        // Sync the updated note with Firestore
        getNote(id).collect { note ->
            note?.let { currentUserId?.let { userId -> firestoreManager.syncNote(it, userId) } }
        }
    }

    init {
        observeNetworkStatus()
    }

    private fun observeNetworkStatus() {
        repositoryScope.launch {
            networkObserver.observe().collect { status ->
                when (status) {
                    is NetworkStatus.Available -> syncPendingNotes()
                    is NetworkStatus.Unavailable -> { /* Do nothing */ }
                }
            }
        }
    }

    private suspend fun syncPendingNotes() {
        currentUserId?.let { userId ->
            try {
                // Get all pending notes from Room
                val pendingNotes = noteDao.getNotesBySyncStatus(SyncStatus.PENDING)
                pendingNotes.forEach { note ->
                    try {
                        firestoreManager.syncNote(note, userId)
                        noteDao.updateSyncStatus(note.id, SyncStatus.SYNCED)
                        pendingSyncQueue.remove(note)
                    } catch (e: Exception) {
                        noteDao.updateSyncStatus(note.id, SyncStatus.FAILED)
                    }
                }

                // Also sync any failed notes
                val failedNotes = noteDao.getNotesBySyncStatus(SyncStatus.FAILED)
                failedNotes.forEach { note ->
                    try {
                        firestoreManager.syncNote(note, userId)
                        noteDao.updateSyncStatus(note.id, SyncStatus.SYNCED)
                    } catch (e: Exception) {
                        // Keep failed status
                    }
                }
            } catch (e: Exception) {
                // Handle general sync errors
            }
        }
    }

    override suspend fun syncWithFireStore(userId: String?) {
        userId?.let { uid ->
            try {
                if (isNewDevice) {
                    // For new device, just get notes from Firestore
                    val firestoreNotes = firestoreManager.getAllUserNotes(uid)
                    // Don't store in local database, just keep in memory
                    // The notes will be shown directly from Firestore
                } else {
                    // For existing device, sync pending notes to Firestore
                    syncPendingNotes()
                }
            } catch (e: Exception) {
                // Handle sync errors
                println("Sync failed: ${e.message}")
            }
        }
    }


    override fun setCurrentUser(userId: String?) {
        currentUserId = userId

        // Check if this is a new device by looking for any existing notes
        repositoryScope.launch {
            val existingNotes = noteDao.getAllNotesOneShot()
            isNewDevice = existingNotes.isEmpty()

            // Trigger initial sync
            syncWithFireStore(userId)
        }
    }
}