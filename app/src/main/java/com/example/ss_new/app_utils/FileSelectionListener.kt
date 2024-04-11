package com.example.ss_new.app_utils

interface FileSelectionListener {
    fun selected(b: Boolean,path :String){}
    fun clicked(path :String){}
}