package com.cazimir.relaxoo.ui.more_apps

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cazimir.relaxoo.R
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_more_apps.*
import org.json.JSONObject


class MoreAppsActivity : AppCompatActivity() {

    lateinit var moreAppsViewModel: MoreAppsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_apps)
        moreAppsViewModel = ViewModelProvider(this).get(MoreAppsViewModel::class.java)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("More apps by this developer")

        loadAds()

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

    private fun loadAds() {
        //    if (BuildConfig.DEBUG) {
//      adView.setAdUnitId(getResources().getString(R.string.ad_test));
//    } else {
//      adView.setAdUnitId(getResources().getString(R.string.ad_prod));
//    }
        ad_view.loadAd(AdRequest.Builder().build())
    }

    private fun getJsonData() {

        val moreAppsJson2 = moreAppsViewModel.remoteConfig.getString("MORE_APPS_JSON")
        val newStr: String = moreAppsJson2.replace("'", "")
        val newStr2: String = newStr.replace("\n", "")
        val newStr3: String = newStr2.replace(" ", "")

        Log.d("MoreApps", "getJsonData called with: " + moreAppsJson2)

        val json = JSONObject(newStr3)
        val jsonArray = json.getJSONArray("apps");
//        val logoUrl = json.getString("logoUrl")
//        val description = json.getString("description")

        more_apps_list.layoutManager = LinearLayoutManager(this)
        //more_apps_list.adapter = MoreAppsAdapter(this, arrayListOf(MoreApp(logoUrl = logoUrl, description = description)))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}