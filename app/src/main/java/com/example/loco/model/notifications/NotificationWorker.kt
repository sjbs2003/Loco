package com.example.loco.model.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.loco.MainActivity
import com.example.loco.R

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val noteId = inputData.getLong(KEY_NOTE_ID, -1L)
        val noteTitle = inputData.getString(KEY_NOTE_TITLE) ?: "Note Reminder"
        val noteContent = inputData.getString(KEY_NOTE_CONTENT) ?: "Time to check your note!"

        showNotification(noteId, noteTitle, noteContent)
        return Result.success()
    }

    private fun showNotification(noteId: Long, title: String, content: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Note Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for note reminders"
        }
        notificationManager.createNotificationChannel(channel)

        // Create an intent to open the note in NoteDetailScreen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("note_id", noteId)
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId.toInt(), // Use noteId as request code to ensure uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Check notification permission
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(noteId.toInt(), notification)
    }

    companion object {
        const val KEY_NOTE_ID = "note_id"
        const val KEY_NOTE_TITLE = "note_title"
        const val KEY_NOTE_CONTENT = "note_content"
        private const val CHANNEL_ID = "note_reminders"
    }
}