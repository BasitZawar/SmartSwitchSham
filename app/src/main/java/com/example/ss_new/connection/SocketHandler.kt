package com.example.ss_new.connection

import android.util.Log
import java.net.Socket
object SocketHandler {
    private var socket: Socket? = null

    fun setSocket(newSocket: Socket) {
        socket = newSocket
    }

    fun getSocket(): Socket? {
        return socket
    }

    fun isConnected(): Boolean {
        return socket?.isConnected == true
    }

    fun closeSocket() {
        try {
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        socket = null
    }
    fun initializeSocket(serverIp: String, serverPort: Int) {
        try {
            val socket = Socket(serverIp, serverPort)
            SocketHandler.setSocket(socket)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SocketError", "Failed to connect socket: ${e.localizedMessage}")
        }
    }
}

//class SocketHandler {
//    companion object {
//        private var socket: Socket? = null
//
//        @Synchronized
//        fun getSocket(): Socket? {
//            return socket
//        }
//
//        @Synchronized
//        fun setSocket(socket: Socket) {
//            Companion.socket = socket
//        }
//    }
//}