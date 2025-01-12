package com.example.ss_new.connection

import android.util.Log
import com.example.ss_new.activites.sending_receiving.ConnectionInterface
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class MyClient internal constructor(
    address: InetAddress,
    connectionInterface: ConnectionInterface
) : Thread() {
    var socket: Socket = Socket()
    var hostAddress: String = address.hostAddress!!
    var mConnectionInterface = connectionInterface
    override fun run() {
        try {
            socket.connect(InetSocketAddress(hostAddress, 8080), 5000)
            SocketHandler.setSocket(socket)
            mConnectionInterface.onConnectionSuccessful()
//            if (mType == "Sender") {
//                mActivity.startActivity(
//                    Intent(
//                        mActivity,
//                        DisplayDataMainScreenActivity::class.java
//                    )
//                )
//                mActivity.finish()
//            } else {
//                mActivity.startActivity(Intent(mActivity, MyDataReceiverActivity::class.java))
//                mActivity.finish()
//            }

        } catch (e: Exception) {

            Log.e("abc", "Exception Client: ${e.message}")
        }
    }
}