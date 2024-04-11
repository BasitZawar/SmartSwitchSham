package com.example.ss_new.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AllFilesTable")
class FilesEntity(@PrimaryKey(autoGenerate = true)  val id: Int = 0, var path:String,var folder: String,
                 var fileType : String,  var isSelected: Boolean,var isSent: Boolean, var isReceived : Boolean, var date: String)