package com.cazimir.relaxoo.ui.privacy_policy

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cazimir.relaxoo.BuildConfig
import com.cazimir.relaxoo.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.activity_privacy_policy.*

class PrivacyPolicyActivity : AppCompatActivity() {
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        initializeAdView()

        val adsBought = intent?.getBooleanExtra("ads_bought", false);

        adsBought?.let {
            if (it) {
                removeAdsView()
            } else {
                loadAds()
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Privacy Policy")

        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://sites.google.com/view/relaxoo-app/home")
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
    }

    private fun initializeAdView() {
        this.adView = AdView(this)
        adView.adSize = AdSize.SMART_BANNER
        adView.id = View.generateViewId()

        if (BuildConfig.DEBUG) {
            adView.adUnitId = resources.getString(R.string.ad_test)
        } else {
            adView.adUnitId = resources.getString(R.string.ad_prod)
        }
    }

    private fun removeAdsView() {
        adView.parent?.let {
            val parent: ViewGroup = it as ViewGroup
            parent.removeView(adView)
            parent.invalidate()
        }
    }

    private fun loadAds() {
        this.adView = AdView(this)
        adView.adSize = AdSize.SMART_BANNER
        adView.id = View.generateViewId()

        if (BuildConfig.DEBUG) {
            adView.adUnitId = resources.getString(R.string.ad_test)
        } else {
            adView.adUnitId = resources.getString(R.string.ad_prod)
        }

        adMobView.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}