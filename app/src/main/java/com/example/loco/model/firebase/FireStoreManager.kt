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
            val firestoreNote = FireStoreNote.fromNoteEntity(note)  // Remove userId parameter
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
                    document.toObject(FireStoreNote::class.java)?.toNoteEntity()  // Remove userId parameter
                }
        } catch (e: Exception) {
            emptyList()
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
}