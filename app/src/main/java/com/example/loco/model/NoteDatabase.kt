package com.example.loco.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    // declare an abstract function that returns the ItemDao so that the database knows about the DAO.
    abstract fun noteDao(): NoteDao

    // define a companion object, which allows access to the methods to create or get the database
    // and uses the class name as the qualifier.
    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null
        /*
         * Multiple threads can potentially ask for a database instance at the same time,
         * which results in two databases instead of one. This issue is known as a race condition.
         * Wrapping the code to get the database inside a synchronized block means that only one thread of execution
         * at a time can enter this block of code, which makes sure the database only gets initialized once.
         * Use synchronized{} block to avoid the race condition.
         */
        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}