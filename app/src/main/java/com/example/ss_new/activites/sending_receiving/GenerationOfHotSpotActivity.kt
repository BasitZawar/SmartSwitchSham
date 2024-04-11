package com.example.ss_new.activites.sending_receiving
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.ss_new.R
import com.example.ss_new.app_utils.data_classes.QrCodeGeneratorClass
import com.example.ss_new.app_utils.data_classes.PORTNO
import com.example.ss_new.app_utils.data_classes.connection.Sockets
import com.example.ss_new.app_utils.data_classes.my_interfaces.SuccessAndFailureInterface
import com.example.ss_new.app_utils.data_classes.ss_models.ConnectionModel
import com.example.ss_new.databinding.ActivityGenerateHotSpotBinding
import com.google.gson.Gson
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import timber.log.Timber
import java.lang.reflect.Method
import java.net.ServerSocket
import java.net.Socket

class GenerationOfHotSpotActivity : AppCompatActivity(), SuccessAndFailureInterface {

    companion object {
        var requestLocationServicesLauncher: ActivityResultLauncher<Intent>? = null
    }

    private val TAG = javaClass.simpleName
    private lateinit var binding: ActivityGenerateHotSpotBinding
    private lateinit var wifiManager: WifiManager
    var reservationOfHotspot: WifiManager.LocalOnlyHotspotReservation? = null
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateHotSpotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager


        requestLocationServicesLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (isLocationEnabled(this)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        wifiManager.startLocalOnlyHotspot(object :
                            WifiManager.LocalOnlyHotspotCallback() {
                            override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                                super.onStarted(reservation)
                                reservationOfHotspot = reservation
                                binding.deviceName.text =
                                    reservation.wifiConfiguration!!.SSID.toString()
                                Timber.e(reservation.wifiConfiguration!!.SSID + "____" + reservation.wifiConfiguration!!.preSharedKey.toString())
                                displayQRCode(
                                    reservation.wifiConfiguration!!.SSID,
                                    reservation.wifiConfiguration!!.preSharedKey
                                )
                            }

                            override fun onFailed(reason: Int) {
                                super.onFailed(reason)
                                Timber.e("$TAG Local Hotspot failed to start")
                                Toast.makeText(
                                    this@GenerationOfHotSpotActivity,
                                    getString(R.string.somethingWrong),
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }, Handler(Looper.getMainLooper()))
                    } else {
                        var method: Method =
                            wifiManager.javaClass.getMethod("getWifiApConfiguration")
                        val netConfig = method.invoke(wifiManager) as WifiConfiguration
                        method = wifiManager.javaClass.getMethod(
                            "setWifiApEnabled",
                            WifiConfiguration::class.java,
                            Boolean::class.javaPrimitiveType
                        )
                        val isSuccess = method.invoke(wifiManager, netConfig, true) as Boolean
                        if (isSuccess) {
                            binding.deviceName.text = netConfig.SSID.toString()
                            displayQRCode(netConfig.SSID, netConfig.preSharedKey)
                        }
                    }
                } else {
                    finish()
                }
            }

        if (isLocationEnabled(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    || !isLocationEnabled(this)
                ) {

                    Toast.makeText(
                        this@GenerationOfHotSpotActivity,
                        getString(R.string.somethingWrong),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (reservationOfHotspot != null) {
                        reservationOfHotspot?.close()
                    }

                    wifiManager.startLocalOnlyHotspot(object :
                        WifiManager.LocalOnlyHotspotCallback() {
                        override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                            super.onStarted(reservation)
                            reservationOfHotspot = reservation
                            binding.deviceName.text =
                                reservation.wifiConfiguration!!.SSID.toString()
                            Timber.e(reservation.wifiConfiguration!!.SSID + "____" + reservation.wifiConfiguration!!.preSharedKey.toString())
                            displayQRCode(
                                reservation.wifiConfiguration!!.SSID,
                                reservation.wifiConfiguration!!.preSharedKey
                            )
                        }

                        override fun onFailed(reason: Int) {
                            super.onFailed(reason)
                            finish()
                        }
                    }, Handler(Looper.getMainLooper()))
                } else {
                    var method: Method = wifiManager.javaClass.getMethod("getWifiApConfiguration")
                    val netConfig = method.invoke(wifiManager) as WifiConfiguration
                    method = wifiManager.javaClass.getMethod(
                        "setWifiApEnabled",
                        WifiConfiguration::class.java,
                        Boolean::class.javaPrimitiveType
                    )
                    val isSuccess = method.invoke(wifiManager, netConfig, true) as Boolean
                    if (isSuccess) {
                        binding.deviceName.text = netConfig.SSID.toString()
                        displayQRCode(netConfig.SSID, netConfig.preSharedKey)
                    }
                }
            } else {
                var method: Method = wifiManager.javaClass.getMethod("getWifiApConfiguration")
                val netConfig = method.invoke(wifiManager) as WifiConfiguration
                method = wifiManager.javaClass.getMethod(
                    "setWifiApEnabled",
                    WifiConfiguration::class.java,
                    Boolean::class.javaPrimitiveType
                )
                val isSuccess = method.invoke(wifiManager, netConfig, true) as Boolean
                if (isSuccess) {
                    binding.deviceName.text = netConfig.SSID.toString()
                    displayQRCode(netConfig.SSID, netConfig.preSharedKey)
                }
            }
        } else {
            Toast.makeText(
                this@GenerationOfHotSpotActivity,
                getString(R.string.enableLocationTxtToast),
                Toast.LENGTH_SHORT
            ).show()
            val locationSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            requestLocationServicesLauncher?.launch(locationSettingsIntent)
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

    }

    private fun displayQRCode(ssid: String, preSharedKey: String) {
        val serializeString = Gson().toJson(ConnectionModel(ssid, preSharedKey))
        val bitmap: Bitmap = QrCodeGeneratorClass
            .newInstance(this)
            ?.setContent(serializeString)
            ?.setErrorCorrectionLevel(ErrorCorrectionLevel.Q)
            ?.setMargin(2)
            ?.qRCOde!!

        binding.ivCode.setImageBitmap(bitmap)


        var socket: Socket? = null
        var serverSocket: ServerSocket?
        val thread = Thread {
            try {
                serverSocket = ServerSocket(PORTNO.PORT)
                serverSocket?.reuseAddress = true

                socket = serverSocket?.accept()

                socket?.let { Sockets.setSocket(it) }
                this.onSuccess()

            } catch (e: Exception) {
                Timber.e("$TAG Exception server 1: ${e.message}")
                this.onFailure(e.message!!)
            }
        }


//        val server = ServerClass(this)
        thread.start()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (reservationOfHotspot != null) {
                reservationOfHotspot?.close()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (reservationOfHotspot != null) {
                reservationOfHotspot?.close()
            }
        }
    }

    override fun onSuccess() {
        startActivity(Intent(this, FilerDataSendingActivityJava::class.java))
    }

    override fun onFailure(reason: String) {
    }


    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

}