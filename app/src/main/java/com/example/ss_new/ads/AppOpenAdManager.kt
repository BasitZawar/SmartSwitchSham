package com.example.ss_new.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.ss_new.BuildConfig
import com.example.ss_new.MApplication
import com.example.ss_new.activites.Splash
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import timber.log.Timber
import java.util.*

class AppOpenAdManager {
    private val AD_UNIT_ID = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/9257395921" else "ca-app-pub-8472119687456393/1315728939"
    private var loadTime: Long = 0

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    var isOverTimed = false

    /** Request an ad. */
    fun loadAd(context: Context, listener : OnShowAdCompleteListener?) {
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            isOverTimed = true
        },5000)
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context, AD_UNIT_ID, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(ad: AppOpenAd) {
                    // Called when an app open ad has loaded.
                    Timber.e("Ad was loaded.")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    if (!isOverTimed) {
                        listener!!.onShowAdComplete()
                    }
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Called when an app open ad has failed to load.
                    Timber.e("error loading"+ loadAdError.message)
                    isLoadingAd = false;
                }
            })
    }

    fun showAdIfAvailable(
        activity: Activity,
        /* onShowAdCompleteListener: OnShowAdCompleteListener*/
    ) {
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Timber.e( "The app open ad is already showing.")
            return
        }
        if(isOverTimed){
            return
        }
        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable()) {
            Timber.e( "The app open ad is not ready yet.")
//                onShowAdCompleteListener.onShowAdComplete()
            loadAd(activity,object : OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    showAdIfAvailable(activity)
                }

            })
            return
        }

        appOpenAd?.setFullScreenContentCallback(
            object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    // Called when full screen content is dismissed.
                    // Set the reference to null so isAdAvailable() returns false.
                    Timber.e( "Ad dismissed fullscreen content.")
                    appOpenAd = null
                    isShowingAd = false

//                        onShowAdCompleteListener.onShowAdComplete()
                    if(activity is Splash){
                        (activity as Splash).nextScreen()
                    }
                    loadAd(activity,null)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Called when fullscreen content failed to show.
                    // Set the reference to null so isAdAvailable() returns false.
                    Timber.e( "error showing "+adError.message)
                    appOpenAd = null
                    isShowingAd = false
                    if(activity is Splash){
                        (activity as Splash).nextScreen()
                    }
//                        onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity,null)
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when fullscreen content is shown.
                    Timber.e( "Ad showed fullscreen content.")
                }
            })
        isShowingAd = true
        appOpenAd?.show(activity)
    }

    /** Check if ad exists and can be shown. */

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }
}