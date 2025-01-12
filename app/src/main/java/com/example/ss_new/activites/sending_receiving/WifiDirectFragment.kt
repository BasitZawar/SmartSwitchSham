package com.example.ss_new.activites.sending_receiving

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.MacAddress
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ss_new.adapters.recycler_adapter.WifiAvailableDeviceAdapter
import com.example.ss_new.app_utils.data_classes.MReceiverFragment
import com.example.ss_new.app_utils.data_classes.my_interfaces.MyClickCallbackInterface
import com.example.ss_new.connection.MyClient
import com.example.ss_new.connection.MyServer
import com.example.ss_new.databinding.FragmentWifiDirectBinding
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import java.net.InetAddress

private const val KEY_USER = "key_user_type"

class WifiDirectFragment : Fragment(), ConnectionInterface {
    private var binding: FragmentWifiDirectBinding? = null
    private lateinit var manageAllFilesPermissionLauncher: ActivityResultLauncher<Intent>
    private var wifiP2pManager: WifiP2pManager? = null
    private lateinit var wifiP2PChannel: WifiP2pManager.Channel
    var receiver: MReceiverFragment? = null
    private lateinit var intentFilter: IntentFilter
    private val peersList: ArrayList<WifiP2pDevice> = ArrayList()
    private var devicesArrayList: ArrayList<String> = ArrayList()
    private lateinit var adapter: WifiAvailableDeviceAdapter
    var pairingSuccess = false
    var gotIt = false
    var name = ""
    private var userType: String = ""
    lateinit var attachedContext: Context
    lateinit var attachedActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userType = it.getString(KEY_USER).toString()
        }
        Log.e("TESTTAG", "onCreate of wifiDirectFragment: $userType")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWifiDirectBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manageAllFilesPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
        }
        initView()
        initScanning()
        Log.e("TESTTAG", "onViewCreated: wifidirect fragment")


        binding?.btnRetry?.setOnClickListener {
            binding?.layoutLoading?.visibility = View.INVISIBLE
            binding?.layoutNoDevice?.visibility = View.INVISIBLE
            scanDevices()

        }
    }

    private fun initView() {
        attachedContext?.let {
            adapter = WifiAvailableDeviceAdapter(it, devicesArrayList, object :
                MyClickCallbackInterface {
                override fun onItemClick(position: Int) {
                    requestPairing(position, name1)
                }
            })
            binding?.rv?.layoutManager = LinearLayoutManager(it)
            binding?.rv?.adapter = adapter
        }
    }

    private fun initScanning() {
        try {
            attachedContext.let {
                wifiP2pManager =
                    attachedContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
                wifiP2PChannel = wifiP2pManager!!.initialize(it, Looper.getMainLooper(), null)
                disconnectWifiDirect()
                receiver = MReceiverFragment(
                    it, wifiP2pManager!!, wifiP2PChannel, this
                )
                intentFilter = IntentFilter()

                intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
                //                intentFilter
                //                    .addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
                intentFilter.addAction(android.location.LocationManager.PROVIDERS_CHANGED_ACTION)
            }
        } catch (e: Exception) {
        }
        scanDevices()
    }

    fun scanDevices() {
        Log.e(TAG, "scanDevices: called")

        if (attachedContext?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {
            return
        }
        wifiP2pManager?.discoverPeers(wifiP2PChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                pairingSuccess = true
            }

            override fun onFailure(reason: Int) {
                pairingSuccess = true
            }

        })
        if (Build.VERSION.SDK_INT >= 33) {
            val wifiManager: WifiManager = attachedContext?.getSystemService(
                Context.WIFI_SERVICE
            ) as WifiManager
            val wInfo: WifiInfo = wifiManager.getConnectionInfo()
            val macAddress: String = wInfo.getMacAddress()
            wifiP2pManager?.setConnectionRequestResult(wifiP2PChannel,
                MacAddress.fromString(macAddress),
                WifiP2pManager.CONNECTION_REQUEST_ACCEPT,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.e("TESTTAG", "onSuccess: connection success For android 33")
                    }

                    override fun onFailure(reason: Int) {
                        Log.e("TESTTAG", "onFailure: connection failed  For android 33$reason")
                    }
                })
        }
    }


    fun checkLocationPermission(context: Context?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val coarseLocationPermission = ContextCompat.checkSelfPermission(
                attachedContext, Manifest.permission.ACCESS_COARSE_LOCATION
            )
            val fineLocationPermission = ContextCompat.checkSelfPermission(
                attachedContext, Manifest.permission.ACCESS_FINE_LOCATION
            )
            val nearbyPermission = ContextCompat.checkSelfPermission(
                attachedContext, Manifest.permission.NEARBY_WIFI_DEVICES
            )

            return coarseLocationPermission == PackageManager.PERMISSION_GRANTED && fineLocationPermission == PackageManager.PERMISSION_GRANTED && nearbyPermission == PackageManager.PERMISSION_GRANTED
        } else {
            val coarseLocationPermission = ContextCompat.checkSelfPermission(
                attachedContext, Manifest.permission.ACCESS_COARSE_LOCATION
            )
            val fineLocationPermission = ContextCompat.checkSelfPermission(
                attachedContext, Manifest.permission.ACCESS_FINE_LOCATION
            )

            return (coarseLocationPermission == PackageManager.PERMISSION_GRANTED
                    && fineLocationPermission == PackageManager.PERMISSION_GRANTED)
        }
    }

    fun requestLocationPermission() {
        val permissions: Array<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = arrayOf(
                Manifest.permission.NEARBY_WIFI_DEVICES,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        Permissions.check(attachedContext, permissions, null, null, object : PermissionHandler() {
            override fun onGranted() {
                Toast.makeText(attachedContext, "Location Granted", Toast.LENGTH_SHORT)
                    .show()
            }

            @SuppressLint("LongLogTag")
            override fun onDenied(
                context: Context,
                deniedPermissions: java.util.ArrayList<String>
            ) {
                if (deniedPermissions != null) {
                    for (permission in deniedPermissions) {
                        Log.e("TESTTAG deniedPermissions", permission!!)
                    }
                }
                Toast.makeText(
                    attachedContext,
                    "Location Permissions required!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun requestPairing(pos: Int, name: String) {
        val device = peersList[pos]
        val wifiConfig = WifiP2pConfig()
        wifiConfig.deviceAddress = device.deviceAddress
        wifiConfig.wps.setup = WpsInfo.PBC

        if (attachedContext.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {
            return
        }
        wifiP2pManager?.connect(wifiP2PChannel, wifiConfig, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                pairingSuccess = true
            }

            override fun onFailure(reason: Int) {
                wifiP2pManager?.cancelConnect(
                    wifiP2PChannel,
                    object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            pairingSuccess = true
                            activity?.let {
                                Toast.makeText(it, "Retry", Toast.LENGTH_SHORT).show()

                            }
                            Log.e(TAG, "onSuccess: Cancel Connection")
                        }

                        override fun onFailure(p0: Int) {
                            Log.e(TAG, "onFailure: Cancel Connection")
                        }
                    })
                return
            }
        })
    }

    val connectionListener = WifiP2pManager.ConnectionInfoListener() {
        val wifiP2pInfo = it
        if (wifiP2pInfo.groupFormed) {
            val groupOwnerAddress: InetAddress = wifiP2pInfo.groupOwnerAddress

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                val serverClass = MyServer(this)
                serverClass.start()
            } else if (wifiP2pInfo.groupFormed) {
                val clientClass = MyClient(groupOwnerAddress, this)
                clientClass.start()
            }
        }
    }
    val peerList = WifiP2pManager.PeerListListener { it ->
        if (!peersList.containsAll(it.deviceList) || !it.deviceList.containsAll(peersList)) {
            peersList.clear()

            peersList.addAll(it.deviceList)
            devicesArrayList.clear()
            for (device in it.deviceList.withIndex()) {
                devicesArrayList.add(device.value.deviceName)
                name = device.value.deviceName
                Log.e("TESTTAG", "DEVICES 1 ${name}")
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun disconnectWifiDirect() {
        if (attachedContext?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {
            return
        }
        wifiP2pManager?.requestGroupInfo(wifiP2PChannel) { group ->
            if (group != null) {
                wifiP2pManager?.removeGroup(wifiP2PChannel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                    }

                    override fun onFailure(reason: Int) {
                    }
                })
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        try {
            attachedContext?.let {
                it.registerReceiver(
                    receiver!!, intentFilter, Context.RECEIVER_EXPORTED
                )
            }

        } catch (e: Exception) {
        }

    }

    override fun onPause() {
        super.onPause()
        attachedContext?.let { it.unregisterReceiver(receiver!!) }
    }

    override fun onAttach(context1: Context) {
        super.onAttach(context1)
        attachedContext = context1
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        attachedActivity = activity
    }

    override fun onConnectionSuccessful() {
        Log.e("TESTTAG", "onConnectionSuccessful1: $userType")
        if (userType == "sender") {
            activity?.runOnUiThread {
                startActivity(
                    Intent(
                        attachedContext, FilerDataSendingActivityJava::class.java
                    )
                )
                requireActivity().finish()

            }
        } else {
            startActivity(
                Intent(
                    attachedContext, FilerDataReceivingActivity::class.java
                )
            )
            requireActivity().finish()
        }
    }

    override fun onConnectionFailed() {
    }

    companion object {
        var name1 = ""
        fun newInstance(param1: String) = WifiDirectFragment().apply {
            arguments = Bundle().apply {
                Log.e("TAG", "newInstance param1 :$param1")
                putString(KEY_USER, param1)
            }
        }
    }
}