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
) : NoteRepository
{
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
            noteDao.delete(note)
            // Only attempt Firebase sync for non-offline users
            if (userId != "offline_user") {
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
            }
        } ?: throw IllegalStateException("No user logged in")
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
                            // Skip sync for offline user
                            if (userId != "offline_user") {
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