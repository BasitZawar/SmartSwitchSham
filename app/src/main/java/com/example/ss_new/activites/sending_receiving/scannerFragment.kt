package com.example.ss_new.activites.sending_receiving


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ss_new.R
import com.example.ss_new.databinding.FragmentJoinHotspotBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.net.InetAddress

class scannerFragment : Fragment(), ZXingScannerView.ResultHandler, ConnectionInterface {

    var binding: FragmentJoinHotspotBinding? = null

    //    private var spotsDialog: SpotsDialog? = null
    private lateinit var wifiManager: WifiManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinHotspotBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wifiManager =
            context?.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
        initScanner()
        if (checkCameraPermission()) {
//            initScanner()
        } else {
            binding!!.layoutBeforePermission.visibility = View.VISIBLE
        }
        binding!!.btnGrant.setOnClickListener {
            requestCameraPermission()
        }
//        initSpotsDialog()
    }
    //    private fun initSpotsDialog() {
//        context?.let {
//            spotsDialog = SpotsDialog.Builder().setContext(context)
//                .setContext(context)
//                .setMessage(getString(R.string.connecting))
//                .setTheme(R.style.MyDialog)
//                .build() as SpotsDialog
//        }
//    }

    private fun initScanner() {
        binding?.hotspotScanner?.setFormats(listOf(BarcodeFormat.QR_CODE))
        binding?.hotspotScanner?.setAutoFocus(true)
        binding?.hotspotScanner?.setLaserColor(R.color.color_blue)
        binding?.hotspotScanner?.setMaskColor(R.color.white)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun handleResult(rawResult: Result?) {
        Log.e("TESTTAG", "handleResult: $rawResult")
        WifiDirectFragment.name1 = rawResult.toString()

        // Use the value here
//        FirebaseCustomEvents(requireContext()).createAdsFirebaseEvents(
//            LOCAL_QR_SCANNED_SUCCESS,
//            "true ",
//        )
        (activity as ActivityWifiConnection).updateFragment(
            WifiDirectFragment.newInstance(
                "receiver",
                rawResult.toString(), "fromScanner"
            )
        )

        /*try {
            val model: MHotSpotModel = Gson().fromJson(rawResult!!.text, MHotSpotModel::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectToWifiForAndroidQ(model)
            } else {
                connectToWifi(model)
            }
        } catch (e: Exception) {
            Log.e("TESTTAg", "handleResult Exception: ")
        }*/
    }

    fun checkCameraPermission(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        )
        return cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        var permissions: Array<String>
        permissions = arrayOf(
            Manifest.permission.CAMERA
        )
        Permissions.check(requireContext(), permissions, null, null, object : PermissionHandler() {
            override fun onGranted() {
                activity?.let { it1 ->
                    binding!!.layoutBeforePermission.visibility = View.GONE
                    initScanner()
                }
            }

            @SuppressLint("LongLogTag")
            override fun onDenied(
                context: Context?, deniedPermissions: ArrayList<String>?
            ) {

                deniedPermissions?.forEach {
                    Log.e("TESTTAG deniedPermissions", "$it")
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        try {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    binding?.hotspotScanner?.startCamera()
                }
                binding?.hotspotScanner?.setResultHandler(this@scannerFragment)
            }
        } catch (_: Exception) {
        }

    }

    override fun onPause() {
        super.onPause()
        binding?.hotspotScanner?.let {
            try {
                it.stopCamera()
            } catch (e: RuntimeException) {
//                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    override fun onConnectionSuccessful() {
        Log.e("TESTTAG", "onConnectionSuccessful: join Fragment ")

//        startActivity(
//            Intent(context, ReceiverActivity::class.java)
//                .putExtra("user", "sender")
//        )
//        activity?.finish()
    }

    override fun onConnectionFailed() {
        Log.e("TESTTAG", "onConnectionFailed: join Fragment ")
    }

}