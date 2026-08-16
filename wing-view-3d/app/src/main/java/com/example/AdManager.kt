package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdMobAdManager"

    // Real AdMob interstitial ad unit ID
    const val AD_UNIT_ID = "ca-app-pub-3709402290986948/5287971334"

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        try {
            MobileAds.initialize(context.applicationContext) { status ->
                Log.d(TAG, "AdMob initialized: ${status.adapterStatusMap}")
                loadAd(context.applicationContext)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize AdMob", e)
        }
    }

    fun loadAd(context: Context) {
        if (interstitialAd != null || isLoading) {
            Log.d(TAG, "Ad already loaded or currently loading")
            return
        }
        isLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "AdMob Interstitial Ad loaded successfully!")
                        interstitialAd = ad
                        isLoading = false
                        setupAdCallback(ad, context)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e(TAG, "AdMob Interstitial Ad failed to load: ${adError.message}")
                        interstitialAd = null
                        isLoading = false
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Exception while loading AdMob Interstitial Ad", e)
            isLoading = false
        }
    }

    private fun setupAdCallback(ad: InterstitialAd, context: Context) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "AdMob Interstitial Ad shown")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "AdMob Interstitial Ad failed to show: ${adError.message}")
                interstitialAd = null
                loadAd(context)
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "AdMob Interstitial Ad dismissed")
                interstitialAd = null
                loadAd(context)
            }

            override fun onAdClicked() {
                Log.d(TAG, "AdMob Interstitial Ad clicked")
            }

            override fun onAdImpression() {
                Log.d(TAG, "AdMob Interstitial Ad impression recorded")
            }
        }
    }

    fun showAd(context: Context) {
        val activity = context as? Activity
        if (activity == null) {
            Log.e(TAG, "Context is not an Activity, cannot show ad")
            return
        }
        val ad = interstitialAd
        if (ad != null) {
            Log.d(TAG, "Showing AdMob Interstitial Ad...")
            interstitialAd = null
            ad.show(activity)
        } else {
            Log.d(TAG, "AdMob Interstitial Ad not ready yet. Preloading for next time...")
            loadAd(context.applicationContext)
        }
    }
}
