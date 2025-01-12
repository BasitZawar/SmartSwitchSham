package com.example.ss_new.ads

import android.app.Activity
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.ss_new.BuildConfig
import com.google.android.gms.ads.*

class BannerAdManager {

    private val AD_UNIT_ID = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/6300978111" else "ca-app-pub-8472119687456393/2687981359"

    fun loadAdmobBanner(activity: Activity, bannerContainer: LinearLayout) {
        bannerContainer.gravity = Gravity.CENTER
        val mAdmobBanner = AdView(activity)
        val adSize = getAdSize(activity)
        mAdmobBanner.setAdSize(adSize)
        mAdmobBanner.adUnitId = AD_UNIT_ID
        val adRequest1 = AdRequest.Builder().build()
        mAdmobBanner.loadAd(adRequest1)
        mAdmobBanner.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                bannerContainer.removeAllViews()
                bannerContainer.addView(mAdmobBanner)
            }

            override fun onAdClosed() {
                super.onAdClosed()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)

            }

            override fun onAdOpened() {
                super.onAdOpened()
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }
        }
    }

    private fun getAdSize(activity: Activity): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }
}