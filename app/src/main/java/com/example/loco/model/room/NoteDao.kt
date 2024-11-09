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

    @Query("SELECT * FROM notes ORDER BY creationDate DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Long): Flow<NoteEntity?>

    @Query("UPDATE notes SET imageUri = :imageUri WHERE id = :id")
    suspend fun updateNoteImage(id: Long, imageUri: String?)

    @Query("SELECT imageUri FROM notes WHERE id = :id")
    suspend fun getNoteImageUri(id: Long): String?

    @Query("UPDATE notes SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: SyncStatus)

    @Query("SELECT * FROM notes WHERE syncStatus = :status")
    suspend fun getNotesBySyncStatus(status: SyncStatus): List<NoteEntity>

    @Query("SELECT * FROM notes ORDER BY creationDate DESC")
    suspend fun getAllNotesOneShot(): List<NoteEntity>
}