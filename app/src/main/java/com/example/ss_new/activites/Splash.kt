package com.example.ss_new.activites

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.ss_new.subscription.BillingViewModel
import com.example.ss_new.MainApp
import com.example.ss_new.ads.InterstitialHelper
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.GoogleMobileAdsConsentManager
import com.example.ss_new.databinding.ActivitySplashBinding
import com.google.android.gms.ads.MobileAds
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val COUNTER_TIME = 8000L

class Splash : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    var manageExternalStorageLauncher: ActivityResultLauncher<Intent>? = null
    lateinit var prefs: SharedPreferences
    private lateinit var viewModel: BillingViewModel
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var secondsRemaining: Long = 0L
    var appRegins = false

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(binding.root)
        Log.e("TAG", "onCreate1: $appCount")
        if (getInt("appCoiount") != null) {
            appCount = getInt("appCoiount")
        }
        InterstitialHelper.adCount = 0
        appRegins = AllFilesUtils.isAppRegion(
            this@Splash
        )
        appCount++
        Log.e("TAG", "onCreate2: $appCount")

        setInt("appCoiount", appCount)

        Log.e("TAG", "onCreate3: $appCount")

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(BillingViewModel::class.java)

        prefs = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)

        progress()
        binding.btnGetStarted.setOnClickListener {
            if (!appHavePermissions(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    requestPermissionsFor13()
                } else {
                    requestPermissionsForBelow()
                }
            } else {
                nextScreen()
            }
        }
        binding.versionName.text = getVersion()
        binding.privacyPolicy.setOnClickListener {
            val url = "https://sites.google.com/view/privacypolicysmartswitchapp/home"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivityInfo(packageManager, 0) != null) {
                startActivity(intent)
            }
        }

        manageExternalStorageLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                if (isManageStoragePermissionsGranted()) {
                    nextScreen()
                }
            }
        }


    }

    private fun progress() {
        val countDownTimer: CountDownTimer = object : CountDownTimer(COUNTER_TIME, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1
            }

            override fun onFinish() {
                secondsRemaining = 0
                (application as? MainApp)?.showAdIfAvailable(
                    this@Splash,
                    object : MainApp.OnShowAdCompleteListener {
                        override fun onShowAdComplete() {
                            binding.progress.visibility = View.GONE
                            binding.btnGetStarted.visibility = View.VISIBLE

                        }
                    }
                )
            }
        }
        countDownTimer.start()
        googleMobileAdsConsentManager =
            GoogleMobileAdsConsentManager.getInstance(applicationContext)
        googleMobileAdsConsentManager.gatherConsent(this) { consentError ->
            if (consentError != null) {
                // Consent not obtained in current session.
                Log.w(
                    "LOG_TAG",
                    String.format("%s: %s", consentError.errorCode, consentError.message)
                )
            }

            if (googleMobileAdsConsentManager.canRequestAds) {
                if (googleMobileAdsConsentManager.isConsentAvailable) {
                    europeanAreas()
                } else {
                    otherAreas()
                }
            }

            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                invalidateOptionsMenu()
            }
            if (secondsRemaining <= 0) {
                binding.progress.visibility = View.GONE
                binding.btnGetStarted.visibility = View.VISIBLE
            }
        }
//old
        // This sample attempts to load ads using consent obtained in the previous session.
//        if (googleMobileAdsConsentManager.canRequestAds) {
//            initializeMobileAdsSdk()
//        }

        //new
        if (googleMobileAdsConsentManager.canRequestAds) {
            if (googleMobileAdsConsentManager.isConsentAvailable) {

                Log.e("TESTTAG", "if part")
                europeanAreas()
            } else {
                Log.e("TESTTAG", "else part")
                otherAreas()
            }
        }

    }

    private fun europeanAreas() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        try {
            if (!AllFilesUtils.isSubscribed(this)) {
                MobileAds.initialize(this)
//                (application as? MainApp)?.loadAd(this)
                InterstitialHelper.loadSplashInterstitialAd(this@Splash)
            }

        } catch (e: Exception) {
        }

    }

    private fun otherAreas() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        try {
            if (!AllFilesUtils.isSubscribed(this)) {
                MobileAds.initialize(this)
                (application as? MainApp)?.loadAd(this)
                InterstitialHelper.loadSplashInterstitialAd(this@Splash)
            }

        } catch (e: Exception) {
        }

    }

    private fun getVersion(): String {
        return try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            "Version:$versionName"
        } catch (e: Exception) {
            "V"
        }
    }

    fun nextScreen() {
        //new code changes
        if (!AllFilesUtils.isSubscribed(this)) {
            if (appRegins) {
                startActivity(
                    Intent(applicationContext, SubscriptionActivity::class.java).putExtra(
                        "isFromSplash",
                        true
                    )
                )
                finish()
            } else {
                if (appCount % 5 == 0 || appCount == 1) {
                    startActivity(
                        Intent(applicationContext, SubscriptionActivity::class.java).putExtra(
                            "isFromSplash",
                            true
                        )
                    )
                    finish()
                } else {
                    startActivity(Intent(applicationContext, DashboardActivity::class.java))
                    finish()
                }
            }
        } else {
            startActivity(Intent(applicationContext, DashboardActivity::class.java))
            finish()
        }


////        InterstitialHelper.showSplashInterstitial(this, object :
////            InterstitialHelper.InterstitialListener {
////            override fun onAdDismiss() {
//        if (prefs.getBoolean("from_activity", true)) {
//            startActivity(Intent(applicationContext, SubscriptionActivity::class.java))
//            finish()
//        } else {
//            isFromActv = false
////            startActivity(DashboardActivity.getIntentForDashboard(this@Splash))
//            startActivity(Intent(applicationContext, SubscriptionActivity::class.java))
//            finish()
//        }
//            }
//        })
    }

    private fun requestPermissionsFor13() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!isManageStoragePermissionsGranted()) {
                            requestManageExternalPermission(this@Splash)
                        } else {
                            nextScreen()
                        }
                    } else {
                        nextScreen()
                    }
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    nextScreen()
                }
            })
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.NEARBY_WIFI_DEVICES,
                android.Manifest.permission.READ_CONTACTS,
            )
            .check()

    }

    private fun requestPermissionsForBelow() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        if (!isManageStoragePermissionsGranted()) {
                            requestManageExternalPermission(this@Splash)
                        } else {
                            nextScreen()
                        }
                    } else {
                        nextScreen()
                    }
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    nextScreen()
                }
            })
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_CONTACTS
            )
            .check();
    }

    companion object {
        var appCount = 0
        fun appHavePermissions(context: Context): Boolean {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                val storagePerB13 = listOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_CONTACTS
                )
                for (i in storagePerB13.indices) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            storagePerB13[i]
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }
            } else {
                val storagePer13 =
                    listOf(
                        android.Manifest.permission.READ_MEDIA_VIDEO,
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_AUDIO,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.NEARBY_WIFI_DEVICES,
                        android.Manifest.permission.READ_CONTACTS,
                    )
                for (i in storagePer13.indices) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            storagePer13[i]
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.e("TAG", "appHavePermissions: " + storagePer13[i])
                        return false
                    }
                }
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                if (!isManageStoragePermissionsGranted()) {
                    Log.e("TAG", "appHavePermissions: no permission manage")
                    return false
                }

            }
            return true
        }

        @RequiresApi(Build.VERSION_CODES.R)
        fun isManageStoragePermissionsGranted(): Boolean {
            return Environment.isExternalStorageManager()
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun requestManageExternalPermission(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        manageExternalStorageLauncher?.launch(intent)
    }

    fun Context.setInt(key: String, value: Int) {
        getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
            .edit()
            .putInt(key, value)
            .apply()
    }

    fun getInt(key: String): Int {
        return getSharedPreferences("my_shared_pref", MODE_PRIVATE)
            .getInt(key, 0) // Provide a default value if the key is not found
    }
}