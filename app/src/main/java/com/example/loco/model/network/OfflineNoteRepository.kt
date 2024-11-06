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
import kotlinx.coroutines.launch

class OfflineNoteRepository(
    private val noteDao: NoteDao,
    private val firestoreManager: FireStoreManager,
    private val networkObserver: NetworkConnectivityObserver
) : NoteRepository {

    private var currentUserId: String? = null
    private val pendingSyncQueue = mutableListOf<NoteEntity>()
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    override fun getNote(id: Long): Flow<NoteEntity?> = noteDao.getNoteById(id)

    fun cleanup() {
        repositoryScope.cancel()
    }

    override suspend fun insertNote(note: NoteEntity) {
        // Insert with PENDING status
        val noteWithStatus = note.copy(syncStatus = SyncStatus.PENDING)
        noteDao.insert(noteWithStatus)

        when (networkObserver.observe().first()) {
            is NetworkStatus.Available -> currentUserId?.let { userId ->
                try {
                    firestoreManager.syncNote(noteWithStatus, userId)
                    noteDao.updateSyncStatus(note.id, SyncStatus.SYNCED)
                } catch (e: Exception) {
                    noteDao.updateSyncStatus(note.id, SyncStatus.FAILED)
                }
            }
            is NetworkStatus.Unavailable -> pendingSyncQueue.add(noteWithStatus)
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
                // First sync local changes to Firestore
                syncPendingNotes()

                // Then get all notes from Firestore
                val firestoreNotes = firestoreManager.getAllUserNotes(uid)

                // Update local database with Firestore data
                firestoreNotes.forEach { note ->
                    val existingNote = noteDao.getNoteById(note.id).first()
                    if (existingNote == null || existingNote.syncStatus != SyncStatus.PENDING) {
                        noteDao.insert(note.copy(syncStatus = SyncStatus.SYNCED))
                    }
                }
            } catch (e: Exception) {
                // Handle sync errors
            }
        }
    }

    override fun setCurrentUser(userId: String?) {
        currentUserId = userId
        // Trigger sync when user changes
        repositoryScope.launch {
            syncWithFireStore(userId)
        }
    }
}