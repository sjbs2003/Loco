package com.example.loco.model.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao // data Access object
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY creationDate DESC")
    fun getAllNotesForUser(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id AND userId = :userId")
    fun getNoteByIdForUser(id: Long, userId: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE userId = :userId")
    suspend fun getAllNotesForUserOneShot(userId: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE syncStatus = :status AND userId = :userId")
    suspend fun getNotesBySyncStatusForUser(status: SyncStatus, userId: String): List<NoteEntity>

    @Query("UPDATE notes SET syncStatus = :status WHERE id = :id AND userId = :userId")
    suspend fun updateSyncStatus(id: Long, status: SyncStatus, userId: String)

    @Query("UPDATE notes SET imageUri = :imageUri WHERE id = :id AND userId = :userId")
    suspend fun updateNoteImage(id: Long, imageUri: String?, userId: String)

    @Query("SELECT imageUri FROM notes WHERE id = :id AND userId = :userId")
    suspend fun getNoteImageUri(id: Long, userId: String): String?

    @Query("UPDATE notes SET isMarkedForDeletion = 1 WHERE id = :noteId")
    suspend fun markForDeletion(noteId: Long)

    @Query("SELECT * FROM notes WHERE isMarkedForDeletion = 1")
    suspend fun getNotesMarkedForDeletion(): List<NoteEntity>

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun permanentlyDelete(noteId: Long)
}