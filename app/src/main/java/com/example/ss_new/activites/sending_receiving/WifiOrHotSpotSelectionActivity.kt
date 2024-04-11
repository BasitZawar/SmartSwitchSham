package com.example.ss_new.activites.sending_receiving

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.example.ss_new.R
import com.example.ss_new.ads.NativeAdManager
import com.example.ss_new.databinding.ActivityWifiOrHotBinding
import com.example.ss_new.app_utils.AllFilesUtils

class WifiOrHotSpotSelectionActivity : AppCompatActivity() {

    companion object{
        const val phoneType = "phoneType"
        fun getIntentForWifiOrHotSpotActivity(context: Context, type :String):Intent{
            return Intent(context, WifiOrHotSpotSelectionActivity::class.java).putExtra(phoneType,type)
        }
    }
    val TAG = javaClass.simpleName
    val binding by lazy {
        ActivityWifiOrHotBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        NativeAdManager(this).loadNative(this,binding.adView)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnWifi.setOnClickListener {
            if(AllFilesUtils.isWiFiConnected(this)) {
                startActivity(Intent(this,WifiConnectionJava::class.java).putExtra(WifiConnectionJava.phoneKey,intent.getStringExtra(phoneType).toString()))
                finish()
            }else{
                Toast.makeText(this,getString(R.string.ensureWIFIOnTxt),Toast.LENGTH_SHORT).show()
            }
        }



        binding.btnHotspot.setOnClickListener {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as (LocationManager)
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            if(intent.getStringExtra(phoneType).toString() == "old") {

                if (!isNetworkEnabled(locationManager) && !isGPSEnabled(
                        locationManager
                    )
                ) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } else {
                    if (!Settings.System.canWrite(this@WifiOrHotSpotSelectionActivity)) {
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                        intent.data = Uri.parse("package:${packageName}")
                        startActivity(intent)
                    } else {
                        startActivity(Intent(this@WifiOrHotSpotSelectionActivity,GenerationOfHotSpotActivity::class.java))
                        finish()
                    }
                }
            }else{
                if (!wifiManager.isWifiEnabled) {
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                } else {
                    startActivity(Intent(this@WifiOrHotSpotSelectionActivity, ScanizationOfHotSpotActivity::class.java))
                    finish()
                }
            }
        }

    }

    fun isNetworkEnabled(locationManager: LocationManager): Boolean {
        try {
            return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }
        return false
    }


    fun isGPSEnabled(locationManager: LocationManager): Boolean {

        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }
        return false
    }
}