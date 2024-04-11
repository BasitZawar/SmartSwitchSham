//package com.example.ss_new.app_utils.data_classes.connection
//
//import com.example.ss_new.app_utils.data_classes.SSUtils.PORT
//import com.example.ss_new.app_utils.data_classes.ss_interfaces.ConnectionCallBack
//import timber.log.Timber
//import java.net.ServerSocket
//import java.net.Socket
//
//class ServerClass(private val connectionCallBack: ConnectionCallBack) : Thread() {
//    private val TAG = javaClass.simpleName
//    var socket: Socket? = null
//    var serverSocket: ServerSocket? = null
//    override fun run() {
//        try {
//            serverSocket = ServerSocket(PORT)
//            serverSocket?.reuseAddress = true
//
//            socket = serverSocket!!.accept()
//
//            Sockets.setSocket(socket!!)
//            connectionCallBack.onSuccess()
//
//        } catch (e: Exception) {
//            Timber.e( "$TAG Exception server 1: ${e.message}")
//            connectionCallBack.onFailure(e.message!!)
//        }
//    }
//}