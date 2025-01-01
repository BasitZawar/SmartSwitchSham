package com.example.ss_new.connection

import android.util.Log
import com.example.ss_new.activites.sending_receiving.ConnectionInterface
import java.net.ServerSocket
import java.net.Socket

class MyServer(private val connectionInterface: ConnectionInterface) : Thread() {
    private val TAG = javaClass.canonicalName
    var socket: Socket? = null
    var serverSocket: ServerSocket? = null
    override fun run() {
        try {
            serverSocket?.reuseAddress = true
            serverSocket = ServerSocket(8080)

            socket = serverSocket!!.accept()

            SocketHandler.setSocket(socket!!)
            connectionInterface.onConnectionSuccessful()

        } catch (e: Exception) {
            connectionInterface.onConnectionFailed()
            Log.e("TAG", "Exception server 1: ${e.message}")
        }
    }
}