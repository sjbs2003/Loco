package com.example.loco.model.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.loco.LocoApplication
import com.google.firebase.auth.FirebaseAuth

class NoteSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as LocoApplication).container.noteRepository
        val currentUser = FirebaseAuth.getInstance().currentUser

        return try {
            if (currentUser != null) {
                repository.syncWithFireStore(currentUser.uid)
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}