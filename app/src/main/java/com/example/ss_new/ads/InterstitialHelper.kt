package com.example.ss_new.ads

import android.app.Activity
import android.os.CountDownTimer
import com.example.ss_new.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

object InterstitialHelper {
    private var mInterstitialAdSplash: InterstitialAd? = null
    private var isAdLoading = false
    var isShowing = false
    private var timer: CountDownTimer? = null
    var isInterSplashLowCpmFailed = false
    var adCount = 0
    private val AD_UNIT_ID =
        if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712" else "ca-app-pub-8472119687456393/4737249958"


    fun showSplashInterstitial(activity: Activity, listener: InterstitialListener) {
        LoadingDialog.showLoadingDialog(activity)
        if (mInterstitialAdSplash != null) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                withContext(Dispatchers.Main) {
                    Timber.e("showSplashInterstitial ad impression null-> " + mInterstitialAdSplash + "StartActivity.isActivityPause->")

                    if (mInterstitialAdSplash != null) {
                        mInterstitialAdSplash?.show(activity)
                        mInterstitialAdSplash?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdImpression() {
                                    super.onAdImpression()
                                    Timber.e("showSplashInterstitial ad impression")
                                    isShowing = true
                                    mInterstitialAdSplash = null
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    isShowing = false
                                    mInterstitialAdSplash = null
                                    loadSplashInterstitialAd(activity)
                                    listener.onAdDismiss()
                                    LoadingDialog.hideLoadingDialog()
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    isShowing = false
                                    isInterSplashLowCpmFailed = true
                                    LoadingDialog.hideLoadingDialog()
                                    listener.onAdDismiss()
                                    Timber.e("showSplashInterstitial failed to show")
                                }
                            }
                    }
                }
            }
        } else {
            LoadingDialog.hideLoadingDialog()
            listener.onAdDismiss()
        }
    }

    fun loadSplashInterstitialAd(activity: Activity) {
        if (adCount >= 6) return
        if (mInterstitialAdSplash == null) {
            isAdLoading = true
            InterstitialAd.load(activity,
                AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {

                    override fun onAdFailedToLoad(adError: LoadAdError) {

                        Timber.e(adError.message)
                        mInterstitialAdSplash = null
                        isAdLoading = false
                        isInterSplashLowCpmFailed = true
                        Timber.e("loadSplashInterstitialAd  ad failed to loaded " + adError.message)
                        timer?.onFinish()
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Timber.e("loadSplashInterstitialAd  ad loaded")
                        adCount++
                        mInterstitialAdSplash = interstitialAd
                        isAdLoading = false
                        isInterSplashLowCpmFailed = false
                    }

                })
        }
    }

    interface InterstitialListener {
        fun onAdDismiss()
    }

}