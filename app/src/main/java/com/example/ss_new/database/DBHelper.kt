package com.example.ss_new.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FilesEntity::class],version = 1)
abstract class DBHelper : RoomDatabase() {
    companion object Db{
        private var instance: DBHelper? = null
        @Synchronized
        fun getDB(context: Context): DBHelper {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    DBHelper::class.java,
                    "SS_database"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            }
            return instance!!
        }
    }

    abstract fun sSwitchDao(): Dao?
}