package com.example.ss_new.app_utils.data_classes.connection

//class ClientClass internal constructor(
//    private val address: InetAddress,
//    private val connectionCallBack: ConnectionCallBack
//) : Thread() {
//    private val TAG = javaClass.simpleName
//    var socket: Socket = Socket()
//    override fun run() {
//        try {
//            Timber.e("run: host address" + address.hostAddress)
//            socket = Socket()
//            val socketAddress = InetSocketAddress(address.hostAddress, PORT)
//            socket.connect(socketAddress, 10000)
//            socket.soTimeout = 10000 // Set a timeout for socket operations (reading/writing)
//            Timber.e("run: local address" + address.hostAddress)
//            Sockets.setSocket(socket)
//            connectionCallBack.onSuccess()
//        } catch (e: Exception) {
//            Timber.e("$TAG: Exception Client: ${e}")
//            connectionCallBack.onFailure(e.message!!)
//        }
//    }
//}