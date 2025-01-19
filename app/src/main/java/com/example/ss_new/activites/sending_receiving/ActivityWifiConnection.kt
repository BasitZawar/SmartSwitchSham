package com.example.ss_new.activites.sending_receiving

import android.content.IntentFilter
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ss_new.databinding.ActivityWifiConnectionBinding

@Suppress("DEPRECATION")
class ActivityWifiConnection : AppCompatActivity() {
    private lateinit var user: String
    private lateinit var locationManager: LocationManager
    private lateinit var wifiManager: WifiManager
    private var wifiDirectFragment: WifiDirectFragment? = null
    private lateinit var intentFilter: IntentFilter
    private lateinit var scannerFragment: scannerFragment

    private val binding: ActivityWifiConnectionBinding by lazy {
        ActivityWifiConnectionBinding.inflate(
            layoutInflater
        )
    }
    var currentPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        user = intent.getStringExtra("user").toString()
        Log.e("TAG", "onCreate user 1214: $user")
        locationManager = getSystemService(LOCATION_SERVICE) as (LocationManager)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        intentFilter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        wifiDirectFragment = WifiDirectFragment()
        scannerFragment = scannerFragment()
        wifiDirectFragment = WifiDirectFragment.newInstance(user)
        setUpConnection()
    }

    override fun onResume() {
        super.onResume()
        setUpConnection()
    }

    private fun setUpConnection() {
        if (user == "sender") {
            wifiDirectFragment?.let { updateFragment(it) }
        } else {
            scannerFragment.let { updateFragment(it) }
        }
    }

    fun updateFragment(fragment: Fragment) {
        if (!isFinishing && !supportFragmentManager.isStateSaved) {
            this.supportFragmentManager.beginTransaction()
                .replace(binding.frameConnection.id, fragment).commit()
        }
    }
}