package com.example.ss_new.activites.sending_receiving

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ss_new.R
import com.example.ss_new.adapters.recycler_adapter.WifiAvailableDeviceAdapter
import com.example.ss_new.app_utils.data_classes.MReceiverFragment
import com.example.ss_new.connection.MyClient
import com.example.ss_new.connection.MyServer
import com.example.ss_new.databinding.ActivityWifiConnectionBinding
import java.net.InetAddress

private const val KEY_USER = "key_user_type"

class WifiDirectFragment : Fragment(), ConnectionInterface {

    private var binding: ActivityWifiConnectionBinding? = null
    private var wifiP2pManager: WifiP2pManager? = null
    private lateinit var wifiP2PChannel: WifiP2pManager.Channel
    var receiver: MReceiverFragment? = null
    private lateinit var intentFilter: IntentFilter
    private val peersList: ArrayList<WifiP2pDevice> = ArrayList()
    private var devicesArrayList: ArrayList<String> = ArrayList()
    private lateinit var adapter: WifiAvailableDeviceAdapter
    var pairingSuccess = false
//    private lateinit var spotsDialog: SpotsDialog

    //    private lateinit var dialog: Dialog
    var name = ""

    private var userType: String = ""
    lateinit var attachedContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userType = it.getString(KEY_USER).toString()
            Log.e("TESTTTAG", "onCreate: $userType")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = ActivityWifiConnectionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initScanning()

        binding?.refresh?.setOnClickListener {
//            binding?.layoutLoading?.visibility = View.GONE
//            binding?.layoutNoDevice?.visibility = View.GONE

            scanDevices()

        }
    }

    private fun initView() {
        attachedContext?.let {
            adapter = WifiAvailableDeviceAdapter(it, devicesArrayList, object : ClickInterface {
                override fun onItemClick(position: Int) {
//                    if (userType != "sender")
//                    if (pairingSuccess) {
                    requestPairing(position, name1)
//                        Log.e("TESTTAG", "onItemClick pairingSuccess iff: $pairingSuccess")
//                    } else {
//                        Log.e("TESTTAG", "onItemClick pairingSuccess else: $pairingSuccess")
//                    }
//                    else Toast.makeText(
//                        requireContext(),
//                        "only receiver can request to connect",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
                }
            })
            binding?.recSearchedDevices?.layoutManager = LinearLayoutManager(it)
            binding?.recSearchedDevices?.adapter = adapter
        }

    }

    private fun initScanning() {
        try {
            attachedContext?.let {
                wifiP2pManager =
                    attachedContext?.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
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
            }

        } catch (e: Exception) {
        }
        scanDevices()
    }

    @SuppressLint("HardwareIds")
    fun scanDevices() {
        Log.e(TAG, "scanDevices: called")
        attachedContext?.let {
            binding?.let { it1 ->
//                Glide.with(attachedContext).asGif().load(R.drawable.icon_searched_devices)
//                    .into(it1.animView)
            }

        } ?: return

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


    private fun requestPairing(pos: Int, name: String) {
        val device = peersList[pos]
        val wifiConfig = WifiP2pConfig()
        wifiConfig.deviceAddress = device.deviceAddress
        wifiConfig.wps.setup = WpsInfo.PBC

        if (attachedContext?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {
            return
        }
        wifiP2pManager?.connect(wifiP2PChannel, wifiConfig, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                pairingSuccess = true
                Log.e("TESTTAG", "CONNECT with $name1")
            }

            override fun onFailure(reason: Int) {
                pairingSuccess = true

                activity?.let {
//                    AppConstants.presentToast(
//                        it, getString(R.string.connection_failed)
//                    )
//                    AppConstants.presentToast(
//                        it, "Connecting Please wait"
//                    )
                }
                Log.e("TESTTAG", "Failure22::$reason")
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

            Log.e("TESTTAG", "DEVICE LIST121 ${peersList.size}")

            for (device in it.deviceList.withIndex()) {
                devicesArrayList.add(device.value.deviceName)
                name = device.value.deviceName
                Log.e("TESTTAG", "DEVICES 1 ${name}")
            }


//            binding?.animView?.pauseAnimation()
//            binding?.layoutLoading?.visibility = View.GONE
//            if (devicesArrayList.isEmpty()) binding?.layoutNoDevice?.visibility = View.VISIBLE
//            else binding?.layoutNoDevice?.visibility = View.GONE
//            adapter.notifyDataSetChanged()
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

    override fun onResume() {
        super.onResume()
        attachedContext?.let { it.registerReceiver(receiver!!, intentFilter) }
    }

    override fun onPause() {
        super.onPause()
        attachedContext?.let { it.unregisterReceiver(receiver!!) }
    }

    override fun onAttach(context1: Context) {
        super.onAttach(context1)
        attachedContext = context1
    }

    override fun onConnectionSuccessful() {

        if (userType == "sender") {

            startActivity(
                Intent(
                    attachedContext, FilerDataSendingActivityJava::class.java
                )
//                    .putExtra("DeviceList", name)
                    .putExtra("DeviceList", name1).putExtra("user", "Sender")
            )

            activity?.finish()
            Log.e("TAG", "onConnectionSuccessful: sender: $name")
        } else {
            startActivity(
                Intent(
                    attachedContext,
                    FilerDataReceivingActivity::class.java
                ).putExtra("DeviceList", name)
                    .putExtra("user", "Receiver")
            )

            activity?.finish()
            Log.e("TAG", "onConnectionSuccessful: receiver: $name")

        }
    }

    override fun onConnectionFailed() {

    }

    companion object {
        var name1 = ""
        fun newInstance(param1: String) = WifiDirectFragment().apply {
            arguments = Bundle().apply {
                putString(KEY_USER, param1)
            }
        }
    }
}
