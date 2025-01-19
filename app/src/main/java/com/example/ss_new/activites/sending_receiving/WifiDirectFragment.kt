package com.example.ss_new.activites.sending_receiving

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import com.example.ss_new.adapters.recycler_adapter.WifiAvailableDeviceAdapter
import com.example.ss_new.app_utils.data_classes.MReceiverFragment
import com.example.ss_new.app_utils.data_classes.my_interfaces.MyClickCallbackInterface
import com.example.ss_new.connection.MyClient
import com.example.ss_new.connection.MyServer
import com.example.ss_new.databinding.FragmentWifiDirectBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import java.net.InetAddress

private const val KEY_USER = "key_user_type"
private const val KEY_OPTIONAL = "key_user_type2"
private const val KEY_OPTIONAL2 = "key_user_type3"

class WifiDirectFragment : Fragment(), ConnectionInterface {
    private var binding: FragmentWifiDirectBinding? = null
    private lateinit var manageAllFilesPermissionLauncher: ActivityResultLauncher<Intent>
    private var wifiP2pManager: WifiP2pManager? = null
    private lateinit var wifiP2PChannel: WifiP2pManager.Channel
    var receiver: MReceiverFragment? = null
    private lateinit var intentFilter: IntentFilter
    private var senderDevice: String = ""
    private val peersList: ArrayList<WifiP2pDevice> = ArrayList()
    private var devicesArrayList: ArrayList<String> = ArrayList()
    private lateinit var adapter: WifiAvailableDeviceAdapter
    var pairingSuccess = false
    var gotIt = false
    var name = ""
    private var userType: String = ""
    private var fromScanner: String = ""
    lateinit var attachedContext: Context
    lateinit var attachedActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userType = it.getString(KEY_USER).toString()
        }
        arguments?.let {
            senderDevice = it.getString(KEY_OPTIONAL).toString()
        }
        arguments?.let {
            fromScanner = it.getString(KEY_OPTIONAL2).toString()
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
        if (fromScanner == "fromScanner") {
            hideQrLayout()
        } else {
            showQrLayout()
        }
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
                    requestPairing(position)
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

    private fun requestPairing(pos: Int) {
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
            if (devicesArrayList.contains(senderDevice)) {
                requestPairing(devicesArrayList.indexOf(senderDevice))
                Log.d(
                    TAG,
                    "device found start connection now: ${devicesArrayList.indexOf(senderDevice)}"
                )
                senderDevice = ""
            }
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

    fun hideQrLayout() {
        binding?.qr?.visibility = View.GONE
        binding?.layoutConnecting?.visibility = View.VISIBLE
    }

    fun showQrLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isAdded) getWifiP2pDeviceName(requireContext()) { deviceName1 ->
                if (!deviceName1.isNullOrBlank()) {
                    deviceName = deviceName1
                    val qrCodeBitmap = generateQRCode(deviceName)
                    binding?.deviceName?.text = deviceName
                    qrCodeBitmap?.let {
                        binding?.qrCodeImageView?.setImageBitmap(it)
                    }
                    Log.d("TESTTAG", "Device name: $deviceName1")
//                WifiDirectFragment().scanDevices()
                } else {
                    Log.d("TESTTAG", "Permissions not granted or device name not available")
                    Toast.makeText(
                        requireContext(),
                        "Device info request not supported on this version",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Device info request not supported on this version",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getWifiP2pDeviceName(context: Context, onDeviceNameAvailable: (String?) -> Unit) {
        val mManager: WifiP2pManager =
            context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        val wifiP2PChannel: WifiP2pManager.Channel =
            mManager.initialize(context, Looper.getMainLooper(), null)

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            // Return null if permissions are not granted
            onDeviceNameAvailable(null)
            return
        }

        // Request device info
        mManager.requestDeviceInfo(wifiP2PChannel,
            @RequiresApi(Build.VERSION_CODES.Q) object : WifiP2pManager.DeviceInfoListener {
                override fun onDeviceInfoAvailable(wifiP2pDevice: WifiP2pDevice?) {
                    val deviceName = wifiP2pDevice?.deviceName ?: "Unknown Device"
                    onDeviceNameAvailable(deviceName) // Pass the device name to the callback
                }
            })
    }

    private fun generateQRCode(text: String): Bitmap? {
        val size = 512 // Size of the QR code
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(
                        x, y, if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                    )
                }
            }
            bmp
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        var name1 = ""
        var deviceName: String = ""
        fun newInstance(param1: String, param2: String? = null, param3: String? = null) =
            WifiDirectFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_USER, param1)
                    param2?.let { putString(KEY_OPTIONAL, it) }
                    param3?.let { putString(KEY_OPTIONAL2, it) }
                }

            }
    }
}