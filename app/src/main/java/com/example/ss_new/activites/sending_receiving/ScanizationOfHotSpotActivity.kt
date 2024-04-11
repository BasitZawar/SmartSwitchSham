package com.example.ss_new.activites.sending_receiving

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.Formatter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.ss_new.R
import com.example.ss_new.app_utils.data_classes.PORTNO
import com.example.ss_new.app_utils.data_classes.connection.Sockets
import com.example.ss_new.app_utils.data_classes.my_interfaces.SuccessAndFailureInterface
import com.example.ss_new.app_utils.data_classes.ss_models.ConnectionModel
import com.example.ss_new.databinding.ActivityScanHotSpotBinding
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import timber.log.Timber
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class ScanizationOfHotSpotActivity : AppCompatActivity(), ZXingScannerView.ResultHandler,
    SuccessAndFailureInterface {
    private lateinit var binding: ActivityScanHotSpotBinding
    private lateinit var wifiManager: WifiManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanHotSpotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        initScan()
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initScan() {
        binding.scanner.setFormats(listOf(BarcodeFormat.QR_CODE))
        binding.scanner.setAutoFocus(true)

    }

    override fun handleResult(rawResult: Result?) {
        try {
            val model: ConnectionModel =
                Gson().fromJson(rawResult!!.text, ConnectionModel::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectToWifi1(model)
            } else {
                connectToWifi2(model)
            }
        } catch (e: Exception) {
            Timber.e("Exception: 1 $e")
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWifi1(model: ConnectionModel) {
        val connectivityManager =
            this.applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(model.name)
            .setWpa2Passphrase(model.password)
            .build()
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()
        val mNetworkCallback: ConnectivityManager.NetworkCallback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivityManager.bindProcessToNetwork(network)
                    initiateClient()
                }
            }
        connectivityManager.requestNetwork(networkRequest, mNetworkCallback)

    }

    private fun connectToWifi2(model: ConnectionModel) {

        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = model.name
        wifiConfig.preSharedKey = model.password

        val netId = wifiManager.addNetwork(wifiConfig)
        wifiManager.disconnect()
        wifiManager.enableNetwork(netId, true)
        wifiManager.reconnect()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            initiateClient()
        }, 5000)
    }

    private fun initiateClient() {
        try {
            val dhcp = wifiManager.dhcpInfo.serverAddress
            val address = Formatter.formatIpAddress(dhcp)
            try {
                val groupOwnerAddress = InetAddress.getByName(address)

                var socket: Socket

                val therad = Thread {
                    try {
                        Timber.e("run: host address" + groupOwnerAddress.hostAddress)
                        socket = Socket()
                        val socketAddress =
                            InetSocketAddress(groupOwnerAddress.hostAddress, PORTNO.PORT)
                        socket.connect(socketAddress, 10000)
                        socket.soTimeout =
                            10000 // Set a timeout for socket operations (reading/writing)
                        Timber.e("run: local groupOwnerAddress" + groupOwnerAddress.hostAddress)
                        Sockets.setSocket(socket)
                        onSuccess()
                    } catch (e: Exception) {

                        this.onFailure(e.message!!)
                    }
                }
//                val client = ClientClass(groupOwnerAddress, this)
                therad.start()
            } catch (e: Exception) {
                e.printStackTrace()
                initiateClient()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        binding.scanner.startCamera()
        binding.scanner.setResultHandler(this)
    }

    override fun onPause() {
        super.onPause()
        binding.scanner.stopCameraPreview()
        binding.scanner.stopCamera()
    }

    override fun onSuccess() {
        startActivity(Intent(this, FilerDataReceivingActivity::class.java))
        finish()
    }

    override fun onFailure(reason: String) {
        runOnUiThread {
            Toast.makeText(this, getString(R.string.errorInConnection), Toast.LENGTH_SHORT).show()
        }
    }
}