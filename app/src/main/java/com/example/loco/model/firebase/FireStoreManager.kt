package com.example.loco.model.firebase

import com.example.loco.model.room.NoteEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FireStoreManager {
    private val firestore = FirebaseFirestore.getInstance()

    // Define collection names as constants
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val NOTES_COLLECTION = "notes"
    }

    fun getUserNotes(userId: String): Flow<List<NoteEntity>> = callbackFlow {
        val listenerRegistration = firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(NOTES_COLLECTION)
            .orderBy("lastModified", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
                    val notes = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(FireStoreNote::class.java)?.toNoteEntity()
                    }
                    trySend(notes)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun syncNote(note: NoteEntity, userId: String) {
        try {
            // Get the latest version from Firestore
            val remoteNote = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTES_COLLECTION)
                .document(note.id.toString())
                .get()
                .await()
                .toObject(FireStoreNote::class.java)

            // If remote note exists and was modified more recently, skip local update
            if (remoteNote != null &&
                remoteNote.lastModified.seconds * 1000 > note.creationDate) {
                return
            }

            // Otherwise, update Firestore with local version
            val firestoreNote = FireStoreNote.fromNoteEntity(note, userId)
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTES_COLLECTION)
                .document(note.id.toString())
                .set(firestoreNote)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteNote(noteId: Long, userId: String) {
        try {
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTES_COLLECTION)
                .document(noteId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun syncAllNotes(notes: List<NoteEntity>, userId: String) {
        try {
            notes.forEach { note ->
                syncNote(note, userId)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAllUserNotes(userId: String): List<NoteEntity> {
        return try {
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTES_COLLECTION)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toObject(FireStoreNote::class.java)?.toNoteEntity()
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}