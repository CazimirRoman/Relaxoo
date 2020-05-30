package com.cazimir.relaxoo.ui.privacy_policy

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cazimir.relaxoo.BuildConfig
import com.cazimir.relaxoo.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.activity_privacy_policy.*
import java.util.*


class PrivacyPolicyActivity : AppCompatActivity() {
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        initializeAdView()

        // TODO: 28.04.2020 refactor this - create single object
        val adsBought = intent?.getBooleanExtra("ads_bought", false)
        val proBought = intent?.getBooleanExtra("pro_bought", false)

        adsBought?.let {
            if (it) {
                removeAdsView()
            } else {
                loadAds()
            }
        }

        proBought?.let {
            if (it) {
                removeAdsView()
            } else {
                loadAds()
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.privacy_policy_title)

        webView.webViewClient = WebViewClient()
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        val languagename: String = Locale.getDefault().displayLanguage

        var pathToHtml = "file:///android_asset/privacy_policy.html"

        when (languagename) {
            Locale.GERMAN.displayLanguage -> pathToHtml = "file:///android_asset/privacy_policy_de.html"
            Locale.FRENCH.displayLanguage -> pathToHtml = "file:///android_asset/privacy_policy_fr.html"
            Locale("es", "ES").displayLanguage -> pathToHtml = "file:///android_asset/privacy_policy_sp.html"
            Locale.ITALIAN.displayLanguage -> pathToHtml = "file:///android_asset/privacy_policy_it.html"
            Locale.CHINESE.displayLanguage -> pathToHtml = "file:///android_asset/privacy_policy_ch.html"
        }


        webView.loadUrl(pathToHtml)


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
