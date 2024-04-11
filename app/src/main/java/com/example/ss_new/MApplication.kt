package com.example.ss_new

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.ss_new.activites.LanguagesActivity
import com.example.ss_new.ads.AppOpenAdManager
import com.google.android.gms.ads.*
import timber.log.Timber
import java.util.*

class MApplication :Application() ,Application.ActivityLifecycleCallbacks ,
    DefaultLifecycleObserver {

     val LOG_TAG = "AppOpenAdManager"
    private var currentActivity: Activity? = null
    private lateinit var appOpenAdManager: AppOpenAdManager


    override fun onCreate() {
        super<Application>.onCreate()

        LanguagesActivity.setLocale(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        val testDeviceIds = ArrayList<String>()
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)
        try {
            MobileAds.initialize(applicationContext)
        } catch (e: Exception) {
        }
//        registerActivityLifecycleCallbacks(this)
//        MobileAds.initialize(
//            this
//        ) { }
//        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // Show the ad (if available) when the app moves to foreground.
        Timber.e("starting")
//        currentActivity?.let { appOpenAdManager.showAdIfAvailable(it) }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // Updating the currentActivity only when an ad is not showing.
//        if (!appOpenAdManager.isShowingAd) {
//            currentActivity = activity
//        }
    }

    override fun onActivityResumed(activity: Activity) {
//        if (activity is AdActivity){
//            currentActivity = activity
//        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}


}