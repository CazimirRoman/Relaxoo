package com.cazimir.relaxoo.ui.about

import android.app.Activity
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
import com.cazimir.relaxoo.analytics.AnalyticsEvents
import com.cazimir.relaxoo.model.AboutItem
import com.cazimir.relaxoo.model.AboutItemType
import com.cazimir.relaxoo.shared.SharedViewModel
import com.cazimir.relaxoo.ui.privacy_policy.PrivacyPolicyActivity
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback
import com.cazimir.utilitieslibrary.shareMyApp
import com.cazimir.utilitieslibrary.showMyListingInStoreForRating
import com.cazimir.utilitieslibrary.showMyOtherApplicationsInGooglePlay
import com.google.firebase.analytics.FirebaseAnalytics
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
                context!!,
                aboutItems as ArrayList<AboutItem>,
                object : Interactor {
                    override fun onItemClick(item: AboutItem) {
                        when (item.name) {

                            is AboutItemType.SendFeedback -> startSendFeedbackAction(listOf("cazimir.developer@gmail.com").toTypedArray(), "Feedback for Relaxoo", "Your feedback helps a lot." +
                                    "\n \n What can we do to make the product better for you?\n\nYour message here:\n")
                            is AboutItemType.RemoveAds -> startRemoveAdsAction()
                            is AboutItemType.Share -> startShareAction()
                            is AboutItemType.PrivacyPolicy -> startPrivacyPolicyActivity()
                            is AboutItemType.RateApp -> startRateAppAction()
                            is AboutItemType.MoreApps -> startMoreAppsActivity()
                        }
                    }
                }
        )
    }

    private fun startSendFeedbackAction(
            addresses: Array<String>, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        if (intent.resolveActivity(activity?.packageManager!!) != null) {
            startActivity(intent)
        }
    }

    private fun startRemoveAdsAction() {
        activityCallback!!.removeAds()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityCallback = context as OnActivityCallback
    }

    private fun startMoreAppsActivity() {
        FirebaseAnalytics.getInstance(context!!).logEvent(AnalyticsEvents.moreAppsClicked().first, AnalyticsEvents.moreAppsClicked().second)
        showMyOtherApplicationsInGooglePlay(activity as Activity)
    }

    private fun startPrivacyPolicyActivity() {
        FirebaseAnalytics.getInstance(context!!).logEvent(AnalyticsEvents.privacyPolicyClicked().first, AnalyticsEvents.privacyPolicyClicked().second)
        val intent = Intent(activity, PrivacyPolicyActivity::class.java)
        startActivity(putAdsBoughExtra(intent))
    }

    private fun putAdsBoughExtra(intent: Intent): Intent {
        intent.putExtra("ads_bought", sharedViewModel!!.adsBought.value)
        intent.putExtra("pro_bought", sharedViewModel!!.proBought.value?.proBought)
        return intent
    }

    private fun startRateAppAction() {
        FirebaseAnalytics.getInstance(context!!).logEvent(AnalyticsEvents.rateAppClicked().first, AnalyticsEvents.rateAppClicked().second)
        showMyListingInStoreForRating(activity as Context)
    }

    private fun startShareAction() {
        FirebaseAnalytics.getInstance(context!!).logEvent(AnalyticsEvents.shareClicked().first, AnalyticsEvents.shareClicked().second)
        shareMyApp(activity as Context, resources.getString(R.string.share_text))
    }

    private fun populateAboutItems(adsBought: Boolean?): ArrayList<AboutItem> {
        val aboutItems = mutableListOf<AboutItem>()

        aboutItems.add(AboutItem(AboutItemType.SendFeedback(getString(R.string.send_feedback)), R.drawable.ic_feedback))

        if (!adsBought!!) {
            aboutItems.add(AboutItem(AboutItemType.RemoveAds(getString(R.string.remove_ads)), R.drawable.ic_shop_white))
        }
        aboutItems.add(AboutItem(AboutItemType.Share(getString(R.string.share_app)), R.drawable.ic_share_white))
        aboutItems.add(AboutItem(AboutItemType.PrivacyPolicy(getString(R.string.privacy_policy)), R.drawable.ic_info_white))
        aboutItems.add(AboutItem(AboutItemType.RateApp(getString(R.string.rate_app)), R.drawable.ic_star_white))
        aboutItems.add(AboutItem(AboutItemType.MoreApps(getString(R.string.more_apps)), R.drawable.ic_more_vert))
        return aboutItems as ArrayList<AboutItem>
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
