package com.cazimir.relaxoo.ui.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.adapter.AboutListAdapter
import com.cazimir.relaxoo.adapter.AboutListAdapter.Interactor
import com.cazimir.relaxoo.model.AboutItem
import com.cazimir.relaxoo.model.MenuItemType
import com.cazimir.relaxoo.shared.SharedViewModel
import com.cazimir.relaxoo.ui.more_apps.MoreAppsActivity
import com.cazimir.relaxoo.ui.privacy_policy.PrivacyPolicyActivity
import com.cazimir.relaxoo.ui.settings.SettingsActivity
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback
import kotlinx.android.synthetic.main.about_fragment.*
import java.util.ArrayList

class AboutFragment : Fragment() {

    private var activityCallback: OnActivityCallback? = null
    private var sharedViewModel: SharedViewModel? = null
    private var aboutItems: List<AboutItem>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.about_fragment, container, false)
        about_recycler_view.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sharedViewModel =
            ViewModelProvider(activity!!).get(SharedViewModel::class.java)
        aboutItems = populateAboutItems(sharedViewModel!!.adsBought.value)
        about_recycler_view.adapter = AboutListAdapter(
            context,
            aboutItems,
            Interactor { item: AboutItem ->
                when (item.name) {
                    MenuItemType.REMOVE_ADS -> startRemoveAdsAction()
                    MenuItemType.SHARE -> startShareAction()
                    MenuItemType.PRIVACY_POLICY -> startPrivacyPolicyActivity()
                    MenuItemType.RATE_APP -> startRateAppAction()
                    MenuItemType.MORE_APPS -> startMoreAppsActivity()
                }
            }
        )
    }

    private fun startRemoveAdsAction() {
        activityCallback!!.removeAds()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityCallback = context as OnActivityCallback
    }

    private fun startMoreAppsActivity() {
        val intent = Intent(activity, MoreAppsActivity::class.java)
        startActivity(putAdsBoughExtra(intent))
    }

    private fun startPrivacyPolicyActivity() {
        val intent = Intent(activity, PrivacyPolicyActivity::class.java)
        startActivity(putAdsBoughExtra(intent))
    }

    private fun putAdsBoughExtra(intent: Intent): Intent {
        return intent.putExtra("ads_bought", sharedViewModel!!.adsBought.value)
    }

    private fun startRateAppAction() {
        val uri =
            Uri.parse("market://details?id=" + context!!.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
// to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context!!.packageName)
                )
            )
        }
    }

    private fun startShareAction() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_text))
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    private fun startSettingsActivity() {
        startActivity(Intent(activity, SettingsActivity::class.java))
    }

    private fun populateAboutItems(adsBought: Boolean?): List<AboutItem> {
        val aboutItems: MutableList<AboutItem> = ArrayList()
        if (!adsBought!!) {
            aboutItems.add(AboutItem(MenuItemType.REMOVE_ADS, R.drawable.ic_message))
        }
        aboutItems.add(AboutItem(MenuItemType.SHARE, R.drawable.ic_message))
        aboutItems.add(AboutItem(MenuItemType.PRIVACY_POLICY, R.drawable.ic_message))
        aboutItems.add(AboutItem(MenuItemType.RATE_APP, R.drawable.ic_message))
        aboutItems.add(AboutItem(MenuItemType.MORE_APPS, R.drawable.ic_message))
        return aboutItems
    }

    companion object {
        private const val TAG = "AboutFragment"

        @JvmStatic
        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }
}