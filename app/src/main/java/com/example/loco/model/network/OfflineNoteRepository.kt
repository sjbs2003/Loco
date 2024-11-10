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

    override fun getAllNotes(): Flow<List<NoteEntity>> =
        currentUserId?.let { noteDao.getAllNotesForUser(it) }
            ?: throw IllegalStateException("No user logged in")

    override fun getNote(id: Long): Flow<NoteEntity?> =
        currentUserId?.let { noteDao.getNoteByIdForUser(id, it) }
            ?: throw IllegalStateException("No user logged in")

    override suspend fun insertNote(note: NoteEntity) {
        currentUserId?.let { userId ->
            // Insert locally with PENDING status
            val noteWithUserAndStatus = note.copy(
                userId = userId,
                syncStatus = SyncStatus.PENDING
            )
            noteDao.insert(noteWithUserAndStatus)

            // Try to sync with Firebase if network is available
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
        } ?: throw IllegalStateException("No user logged in")
    }

    override suspend fun updateNote(note: NoteEntity) {
        currentUserId?.let { userId ->
            val noteWithStatus = note.copy(syncStatus = SyncStatus.PENDING)
            noteDao.update(noteWithStatus)

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
        } ?: throw IllegalStateException("No user logged in")
    }

    override suspend fun deleteNote(note: NoteEntity) {
        currentUserId?.let { userId ->
            noteDao.delete(note)
            when (networkObserver.observe().first()) {
                is NetworkStatus.Available -> {
                    try {
                        firestoreManager.deleteNote(note.id, userId)
                    } catch (e: Exception) {
                        // Handle delete error if needed
                    }
                }
                is NetworkStatus.Unavailable -> {
                    // Could implement deletion queue if needed
                }
            }
        } ?: throw IllegalStateException("No user logged in")
    }


    override suspend fun syncWithFireStore(userId: String?) {
        userId?.let { uid ->
            try {
                // Get local notes that need syncing
                val pendingNotes = noteDao.getNotesBySyncStatusForUser(SyncStatus.PENDING, uid)
                val failedNotes = noteDao.getNotesBySyncStatusForUser(SyncStatus.FAILED, uid)
                val notesToSync = pendingNotes + failedNotes

                // Sync pending local changes to Firestore
                notesToSync.forEach { note ->
                    try {
                        firestoreManager.syncNote(note, uid)
                        noteDao.updateSyncStatus(note.id, SyncStatus.SYNCED, uid)
                    } catch (e: Exception) {
                        noteDao.updateSyncStatus(note.id, SyncStatus.FAILED, uid)
                    }
                }

                // Only fetch Firestore notes if local DB is empty
                if (noteDao.getAllNotesForUserOneShot(uid).isEmpty()) {
                    val firestoreNotes = firestoreManager.getAllUserNotes(uid)
                    firestoreNotes.forEach { firestoreNote ->
                        // Preserve the image URI when storing from Firestore
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
        if (userId != null) {
            repositoryScope.launch {
                syncWithFireStore(userId)
            }
        }
    }

    override suspend fun updateNoteImage(id: Long, imageUri: String?) {
        currentUserId?.let { userId ->
            // Update image locally first
            noteDao.updateNoteImage(id, imageUri, userId)

            // Get the updated note
            val note = noteDao.getNoteByIdForUser(id, userId).first()

            // Only sync with Firestore if we have a note and network
            note?.let { updatedNote ->
                when (networkObserver.observe().first()) {
                    is NetworkStatus.Available -> {
                        try {
                            // Mark as pending before sync
                            noteDao.updateSyncStatus(id, SyncStatus.PENDING, userId)

                            // Sync the complete note to ensure image URI is included
                            firestoreManager.syncNote(updatedNote, userId)

                            // Update sync status after successful sync
                            noteDao.updateSyncStatus(id, SyncStatus.SYNCED, userId)
                        } catch (e: Exception) {
                            // Keep the sync status as is if sync fails
                            noteDao.updateSyncStatus(id, SyncStatus.FAILED, userId)
                        }
                    }
                    is NetworkStatus.Unavailable -> {
                        // Mark for later sync
                        noteDao.updateSyncStatus(id, SyncStatus.PENDING, userId)
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
                            // Only sync pending changes when network becomes available
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