package com.cazimir.relaxoo.ui.privacy_policy

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cazimir.relaxoo.R
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_privacy_policy.*


class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        loadAds()

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setTitle("Privacy Policy");

        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://sites.google.com/view/relaxoo-app/home")
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

    }

    private fun loadAds() {
        ad_view.loadAd(AdRequest.Builder().build())
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}