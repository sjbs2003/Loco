package com.example.loco.model.network

import android.content.Context
import com.example.loco.model.firebase.FireStoreManager
import com.example.loco.model.room.NoteDatabase
import com.google.firebase.auth.FirebaseAuth

interface AppContainer {
    val noteRepository: NoteRepository
    fun cleanUp()
}

class AppDataContainer(private val context: Context) : AppContainer {
    private val database: NoteDatabase by lazy { NoteDatabase.getDatabase(context) }
    private val firestoreManager: FireStoreManager by lazy { FireStoreManager() }
    private val networkObserver: NetworkConnectivityObserver by lazy {
        NetworkConnectivityObserver(context)
    }

    override val noteRepository: NoteRepository by lazy {
        val repository = OfflineNoteRepository(
            database.noteDao(),
            firestoreManager,
            networkObserver
        )

        // Set current user if already logged in
        FirebaseAuth.getInstance().currentUser?.let { user ->
            repository.setCurrentUser(user.uid)
        }

        repository
    }

    override fun cleanUp() {
        (noteRepository as? OfflineNoteRepository)?.cleanup()
    }
}