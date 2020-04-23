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
import androidx.recyclerview.widget.RecyclerView
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.adapter.AboutListAdapter
import com.cazimir.relaxoo.adapter.AboutListAdapter.Interactor
import com.cazimir.relaxoo.model.AboutItem
import com.cazimir.relaxoo.model.MenuItemType
import com.cazimir.relaxoo.shared.SharedViewModel
import com.cazimir.relaxoo.ui.privacy_policy.PrivacyPolicyActivity
import com.cazimir.relaxoo.ui.settings.SettingsActivity
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback
import kotlinx.android.synthetic.main.about_fragment.view.*

class AboutFragment : Fragment() {

    lateinit var aboutRecyclerView: RecyclerView
    private var activityCallback: OnActivityCallback? = null
    private var sharedViewModel: SharedViewModel? = null
    private var aboutItems: List<AboutItem>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.about_fragment, container, false)
        this.aboutRecyclerView = view.about_recycler_view
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sharedViewModel =
                ViewModelProvider(activity!!).get(SharedViewModel::class.java)
        aboutItems = populateAboutItems(sharedViewModel!!.adsBought.value)
        aboutRecyclerView.layoutManager = LinearLayoutManager(context)
        aboutRecyclerView.adapter = AboutListAdapter(
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
        val appPackageName: String = activity!!.packageName// getPackageName() from Context or Activity object

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Cazimir+Roman&hl=en")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
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
            aboutItems.add(AboutItem(MenuItemType.REMOVE_ADS, R.drawable.ic_shop_white))
        }
        aboutItems.add(AboutItem(MenuItemType.SHARE, R.drawable.ic_share_white))
        aboutItems.add(AboutItem(MenuItemType.PRIVACY_POLICY, R.drawable.ic_info_white))
        aboutItems.add(AboutItem(MenuItemType.RATE_APP, R.drawable.ic_star_white))
        aboutItems.add(AboutItem(MenuItemType.MORE_APPS, R.drawable.ic_more_vert))
        return aboutItems
    }

    fun hideRemoveAdsButton() {
        val adapter = aboutRecyclerView.adapter as AboutListAdapter
        adapter.removeRemoveAds()
    }

    companion object {
        private const val TAG = "AboutFragment"

        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }
}
