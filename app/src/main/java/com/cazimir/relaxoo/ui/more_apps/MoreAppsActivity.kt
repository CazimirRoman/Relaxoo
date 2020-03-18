package com.cazimir.relaxoo.ui.more_apps

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cazimir.relaxoo.BuildConfig
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.adapter.MoreAppsAdapter
import com.cazimir.relaxoo.model.MoreApp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.activity_more_apps.*
import org.json.JSONObject


class MoreAppsActivity : AppCompatActivity() {

    private lateinit var adView: AdView
    lateinit var moreAppsViewModel: MoreAppsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_apps)
        moreAppsViewModel = ViewModelProvider(this).get(MoreAppsViewModel::class.java)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("More apps by this developer")

        val adsBought = intent?.getBooleanExtra("ads_bought", false)

        initializeAdView()

        adsBought?.let {
            if (it) {
                removeAdsView()
            } else {
                loadAds()
            }
        }
        moreAppsViewModel.remoteConfig.setDefaultsAsync(R.xml.default_more_apps)

        moreAppsViewModel.remoteConfig.fetchAndActivate()
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    Log.d("MoreAppsActivity", "remote Config activated")
                    getJsonData()
//                        moreAppsViewModel.remoteConfigReady()
                } else {
                    Log.e("MoreAppsActivity", "remote Config failed")
                }
            }

        moreAppsViewModel.remoteReady.observe(this, Observer { ready ->
            Log.d("MoreApps", "remoteReady called")
            if (ready) getJsonData()
        })
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

        adMobView.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun getJsonData() {

        val moreAppsJson2 = moreAppsViewModel.remoteConfig.getString("MORE_APPS_JSON")
        val newStr: String = moreAppsJson2.replace("'", "")
        val newStr2: String = newStr.replace("\n", "")
        val newStr3: String = newStr2.replace(" ", "")

        Log.d("MoreApps", "getJsonData called with: " + moreAppsJson2)

        val json = JSONObject(newStr3)
        val appsJsonArray = json.getJSONArray("apps")
        more_apps_list.layoutManager = LinearLayoutManager(this)


        val moreAppsList = mutableListOf<MoreApp>()

        for (i in 0 until appsJsonArray.length()) {
            val moreApp = appsJsonArray.getJSONObject(i)
            moreAppsList.add(
                MoreApp(
                    logoUrl = moreApp.getString("logoUrl"),
                    description = moreApp.getString("description")
                )
            )
        }

        more_apps_list.adapter = MoreAppsAdapter(this, moreAppsList)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}