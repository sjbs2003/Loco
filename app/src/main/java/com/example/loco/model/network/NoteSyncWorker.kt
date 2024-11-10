package com.example.loco.model.network

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.loco.LocoApplication
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class NoteSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as LocoApplication).container.noteRepository
        val currentUser = FirebaseAuth.getInstance().currentUser

        return try {
            if (currentUser != null) {
                repository.setCurrentUser(currentUser.uid)
                repository.refreshNotes()  // Force a refresh of notes
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<NoteSyncWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "noteSyncWork",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}