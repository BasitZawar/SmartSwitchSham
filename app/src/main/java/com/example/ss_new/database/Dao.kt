package com.example.ss_new.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.nio.file.Files

@Dao
interface Dao {

    @Insert
    fun insertAllFiles(files : FilesEntity)

    @Query("DELETE FROM AllFilesTable WHERE path = :path")
    fun deleteByPath(path: String)

    @Query("SELECT * FROM AllFilesTable where isSent = 1")
    fun getAllSentFile(): LiveData<List<FilesEntity>>

    @Query("SELECT * FROM AllFilesTable where isReceived = 1")
    fun getAllReceivedFile(): List<FilesEntity>



}