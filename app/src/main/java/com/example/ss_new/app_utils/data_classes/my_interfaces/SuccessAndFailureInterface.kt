package com.example.ss_new.app_utils.data_classes.my_interfaces

interface SuccessAndFailureInterface {

    fun onSuccess()
    fun onFailure(reason: String)
}