package com.cazimir.relaxoo

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder
import cafe.adriel.androidaudiorecorder.model.AudioChannel
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate
import cafe.adriel.androidaudiorecorder.model.AudioSource
import com.android.billingclient.api.*
import com.cazimir.relaxoo.adapter.PagerAdapter
import com.cazimir.relaxoo.dialog.DeleteConfirmationDialog
import com.cazimir.relaxoo.dialog.OnDeleted
import com.cazimir.relaxoo.dialog.favorite.FavoriteDeleted
import com.cazimir.relaxoo.dialog.favorite.SaveToFavoritesDialog
import com.cazimir.relaxoo.dialog.pro.ProBottomDialogFragment
import com.cazimir.relaxoo.dialog.recording.BottomRecordingDialogFragment
import com.cazimir.relaxoo.dialog.timer.OnTimerDialogCallback
import com.cazimir.relaxoo.dialog.timer.TimerDialog
import com.cazimir.relaxoo.eventbus.EventBusServiceDestroyed
import com.cazimir.relaxoo.model.ListOfSavedCustom
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.model.SavedCombo
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.repository.ModelPreferencesManager
import com.cazimir.relaxoo.repository.ModelPreferencesManager.save
import com.cazimir.relaxoo.shared.SharedViewModel
import com.cazimir.relaxoo.ui.create_sound.CreateSoundFragment
import com.cazimir.relaxoo.ui.create_sound.OnRecordingStarted
import com.cazimir.relaxoo.ui.favorites.FavoritesFragment
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback
import com.cazimir.relaxoo.ui.sound_grid.OnFavoriteSaved
import com.cazimir.relaxoo.ui.sound_grid.SoundGridFragment
import com.cazimir.relaxoo.util.InternetUtil
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.JsonSyntaxException
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.main_activity.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : FragmentActivity(),
    OnActivityCallback,
    OnFavoriteSaved,
    OnTimerDialogCallback,
    OnDeleted,
    OnRecordingStarted,
    PurchasesUpdatedListener,
    RewardedVideoAdListener {

    companion object {
        private val TAG = "MainActivity"
        private val LIFECYCLE = "Lifecycle"
        private val CHANNEL_WHATEVER = "" + ""
        private val RECORDING_REQ_CODE = 0
        val PINNED_RECORDINGS = "PINNED_RECORDINGS"
    }

    private var splashShown: Boolean = false
    private var fetchRunning: Boolean = false
    private var doubleBackToExitPressedOnce: Boolean = false
    private lateinit var adView: AdView
    private lateinit var adUnitId: String
    private lateinit var notificationManager: NotificationManager
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var billingClient: BillingClient

    private lateinit var rewardedVideoAd: RewardedVideoAd

    private val areWritePermissionsGranted = MutableLiveData<Boolean>()
    private val isSoundGridFragmentStarted = MutableLiveData<Boolean>()
    private val isInternetAvailable = MutableLiveData<Boolean>()

    private var preconditionsToStartFetchingData: PreconditionsToStartFetchingData = PreconditionsToStartFetchingData()

    private val skuList = listOf("remove_ads")

    private var timerDialog: TimerDialog? = null
    private var soundGridFragment: SoundGridFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        EventBus.getDefault().register(this)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        //shouldShowSplash()
        setupViewPager()
        setupViewPagerDots()
        setupNotifications()
        startColorChangeAnimation()
        checkPermissions()
        // should also be done in onResume()
        setupBillingClient()
        setupRewardVideoAd()

        val result = MediatorLiveData<PreconditionsToStartFetchingData>()

        InternetUtil.init(application)
        InternetUtil.observe(this, Observer { internetUp ->
            isInternetAvailable.value = internetUp
            when {
                internetUp -> {
                    no_internet_text.visibility = GONE
                }
                else -> {
                    no_internet_text.visibility = VISIBLE
                }
            }
        })

        result.addSource(
                areWritePermissionsGranted) { permissionsGranted: Boolean ->
            Log.d(TAG, "permissions granted: " + permissionsGranted)
            preconditionsToStartFetchingData = preconditionsToStartFetchingData.copy(arePermissionsGranted = permissionsGranted)
            result.setValue(preconditionsToStartFetchingData)
        }

        result.addSource(isSoundGridFragmentStarted) { fragmentStarted: Boolean ->
            Log.d(TAG, "soundGridFragmentStarted: " + fragmentStarted)
            preconditionsToStartFetchingData = preconditionsToStartFetchingData.copy(isFragmentStarted = fragmentStarted)
            result.setValue(preconditionsToStartFetchingData)
        }

        result.addSource(isInternetAvailable) { internetUp ->
            preconditionsToStartFetchingData = preconditionsToStartFetchingData.copy(isInternetUp = internetUp)
            result.setValue(preconditionsToStartFetchingData)
        }

        // TODO: 14-Mar-20 This observer is called 2 times - fix it
        result.observe(
                this,
                Observer { preconditionsToStartFetchingData: PreconditionsToStartFetchingData ->
                    Log.d(
                            TAG, (
                            "onChanged() called with: preconditionsToStartFetchingData: " +
                                    preconditionsToStartFetchingData.toString()))
                    if (preconditionsToStartFetchingData.isFragmentStarted && preconditionsToStartFetchingData.arePermissionsGranted && preconditionsToStartFetchingData.isInternetUp) {
                        if (!getSoundGridFragment().soundsAlreadyFetched()) {
                            Log.d(
                                    TAG, "sounds already fetched: " + getSoundGridFragment().soundsAlreadyFetched())

                            if (!fetchRunning) {
                                getSoundGridFragment().fetchSounds()
                                fetchRunning = true
                            }
                        }
                    }
                })

        // remove ads logic - listen to observer in viewmodel
        sharedViewModel.adsBought.observe(this, Observer { adsBought: Boolean ->
            if (adsBought) {
                removeAdsView()
            } else {
                loadAds()
            }
        })
    }

    private fun setupRewardVideoAd() {
        this.adUnitId =
            if (BuildConfig.DEBUG) resources.getString(R.string.reward_ad_test) else resources.getString(
                R.string.reward_ad_prod
            )
        MobileAds.initialize(this, adUnitId)
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        rewardedVideoAd.rewardedVideoAdListener = this
        loadRewardedVideoAd()
    }

    private fun setupViewPagerDots() {
        // new way to attach the tablayout to viewpager2
        TabLayoutMediator(tabDots, pager) { tab, position -> }.attach()
    }

    private fun setupViewPager() {
        pager.offscreenPageLimit = 3
        pager.adapter = PagerAdapter(this)
    }

    override fun onPause() {
        Log.d(LIFECYCLE, "onPause: called")
        super.onPause()
        rewardedVideoAd.pause(this)
    }

    override fun onResume() {
        Log.d(LIFECYCLE, "onResume: called")
        super.onResume()
        rewardedVideoAd.resume(this)
    }

    override fun onDestroy() {
        Log.d(LIFECYCLE, "onDestroy: called")

//        //find a way to determine if onDestroy is called because user swiped app away or because rotation happened
//        if (!isChangingConfigurations) {
//            startService(SoundPoolService.getCommand(this, StopServiceCommand()))
//        }
        rewardedVideoAd.destroy(this)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Snackbar.make(this.window.decorView.findViewById(android.R.id.content), "Please click BACK again to exit", Snackbar.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    private fun shouldShowSplash() {
        if (sharedViewModel.splashShown) {
            hideSplash()
        }
    }

    private fun setupBillingClient() {
        billingClient =
            BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
        billingClient.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "onBillingSetupFinished() called with: success!")
                            val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                            if (purchasesResult.purchasesList.size != 0 && (purchasesResult.purchasesList[0].sku == "remove_ads")) {
                                sharedViewModel.adsBought(true)
                            }
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        Log.d(TAG, "onBillingServiceDisconnected() called with: failed")
                        // Try to restart the connection on the next request to
                        // Google Play by calling the startConnection() method.
                    }
                })
    }

    private fun launchFlowToRemoveAds() {
        if (billingClient.isReady) {
            val skuDetailsParams = SkuDetailsParams.newBuilder()
                    .setSkusList(skuList)
                    .setType(BillingClient.SkuType.INAPP)
                    .build()
            billingClient.querySkuDetailsAsync(
                    skuDetailsParams
            ) { billingResult: BillingResult, skuDetailsList: List<SkuDetails> ->
                if ((billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                                !skuDetailsList.isEmpty())) {
                    for (skuDetail: SkuDetails in skuDetailsList) {
                        val flowParams: BillingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetail).build()
                        billingClient.launchBillingFlow(this, flowParams)
                        Toast.makeText(this, skuDetail.description, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "loadAllSKUs() called with: skuDetailsList is empty")
                }
            }
        } else {
            Log.d(TAG, "loadAllSKUs() called with: billingClient is not ready")
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

    private fun checkPermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(
                        object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                Log.d(
                                        TAG,
                                        "onPermissionsChecked: " + report.grantedPermissionResponses.toString())
                                if (report.areAllPermissionsGranted()) {
                                    areWritePermissionsGranted.value = true
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                    permissions: List<PermissionRequest>,
                                    token: PermissionToken
                            ) {
                                Log.d(TAG, "onPermissionRationaleShouldBeShown: called")
                                /* ... */
                            }
                        })
                .check()
    }

    private fun startColorChangeAnimation() {
        sharedViewModel
                .nextColor
                .observe(
                        this,
                        Observer { backgroundColor: Int? ->
                            val duration = 1500
                            val animator: ObjectAnimator = ObjectAnimator.ofObject(
                                    parentLayout,
                                    "backgroundColor",
                                    ArgbEvaluator(),
                                    sharedViewModel.previousColor,
                                    sharedViewModel.nextColor.getValue())
                                    .setDuration(duration.toLong())
                            animator.addListener(
                                    object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator) {
                                            Log.d(TAG, "onAnimationEnd: called")
                                            sharedViewModel.previousColor = sharedViewModel.nextColor.getValue()
                                        }
                                    })
                            animator.start()
                        })
        if (sharedViewModel.timerTaskExtended == null) {
            sharedViewModel.setTimerTaskExtended(
                    this,
                    object : TimerTask() {
                        override fun run() {
                            runOnUiThread {
                                sharedViewModel.nextColor.setValue(randomColor)
                                Log.d(
                                        TAG, String.format(
                                        "run: called. previousColor: %s and nextColor: %s",
                                        sharedViewModel.previousColor, sharedViewModel.nextColor.value))
                            }
                        }
                    })
            sharedViewModel.startOrStop()
        }
    }

    private val randomColor: Int
        get() {
            val random = Random()
            return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }

    private fun setupNotifications() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                        notificationManager.getNotificationChannel(CHANNEL_WHATEVER) == null)) {
            val notificationChannel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(
                        CHANNEL_WHATEVER,
                        "Whatever",
                        NotificationManager.IMPORTANCE_LOW
                )
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun showAddToFavoritesDialog(playingSounds: List<Sound>) {
        SaveToFavoritesDialog(playingSounds).show(supportFragmentManager, "save")
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showTimerDialog() {
        if (timerDialog == null) {
            timerDialog = TimerDialog(this)
        }
        timerDialog?.show(supportFragmentManager, "timer")
    }

    override fun hideProgress() {
        progress_bar.visibility = GONE
    }

    override fun triggerCombo(savedCombo: SavedCombo) {
        Log.d(
                TAG, (
                "triggerCombo in MainActivity: called with: " +
                        savedCombo.sounds.toString()))
        getSoundGridFragment()!!.triggerCombo(savedCombo)
    }

    override fun showDeleteConfirmationDialog(deleted: OnDeleted) {
        DeleteConfirmationDialog(deleted)
                .show(
                        supportFragmentManager,
                        if (deleted is FavoriteDeleted) "favDeleted" else "recDeleted")
    }

    override fun saved(savedCombo: SavedCombo) {
        Log.d(TAG, "saved: called")
        favoriteFragment!!.updateList(savedCombo)
    }

    private val favoriteFragment: FavoritesFragment?
        get() = supportFragmentManager
                .findFragmentByTag("f1") as FavoritesFragment?

    private fun getSoundGridFragment(): SoundGridFragment {
        return supportFragmentManager
                .findFragmentByTag("f0") as SoundGridFragment
    }

    private val createSoundFragment: CreateSoundFragment?
        get() = supportFragmentManager
                .findFragmentByTag("f2") as CreateSoundFragment?

    override fun startCountDownTimer(minutes: Int) {
        soundGridFragment!!.startCountDownTimer(minutes)
    }

    override fun showBottomDialogForPro() {
        ProBottomDialogFragment(this).show(supportFragmentManager, "pro")
    }

    override fun soundGridFragmentStarted() {
        Log.d(TAG, "soundGridFragmentStarted() called")
        isSoundGridFragmentStarted.value = true
        soundGridFragment = getSoundGridFragment()
    }

    override fun hideSplash() {
        Log.d(TAG, "hideSplash: called")
        if (!splashShown) {
            splash.visibility = GONE
            main_layout.visibility = VISIBLE
            splashShown = true
            fetchRunning = false
            Log.d(TAG, "EspressoIdlingResource.decrement called")
            EspressoIdlingResource.decrement()
        }
    }

    override fun removeAds() {
        launchFlowToRemoveAds()
    }

    private fun removeAdsView() {

        adView.parent?.let {
            val parent: ViewGroup = it as ViewGroup
            parent.removeView(adView)
            parent.invalidate()
        }
    }

    override fun deleteRecording(recording: Recording) {
        createSoundFragment!!.deleteRecording(recording)
    }

    override fun renameRecording(recording: Recording, newName: String) {
        createSoundFragment!!.renameRecording(recording, newName)
    }

    override fun pinToDashBoardActionCalled(sound: Sound) {
        getSoundGridFragment()!!.addRecordingToSoundPool(sound)
        try {
            val pinnedRecordings = ModelPreferencesManager.get<ListOfSavedCustom>(PINNED_RECORDINGS)
            val list = pinnedRecordings?.savedCustomList ?: mutableListOf()
            list?.add(sound)
            val newObject = ListOfSavedCustom(list)
            save(newObject, PINNED_RECORDINGS)
        } catch (e: JsonSyntaxException) {
            save(ListOfSavedCustom(mutableListOf(sound)), PINNED_RECORDINGS)
        }

        scrollViewPager()
    }

    override fun playRewardAd() {
        if (rewardedVideoAd.isLoaded) {
            rewardedVideoAd.show()
        }
    }

    private fun redirectUserToPlayStore() {
        val marketUri =
            Uri.parse("https://play.google.com/store/apps/details?id=com.cazimir.relaxoo")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, marketUri))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, "Couldn't find PlayStore on this device", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun recordingStarted() {
        checkRecordingPermission()
    }

    override fun showBottomDialogForRecording(recording: Recording) {
        BottomRecordingDialogFragment(recording, this)
                .show(supportFragmentManager, "rec_bottom")
    }

    private fun scrollViewPager() {
        pager.currentItem = 0
        getSoundGridFragment()!!.scrollToBottom()
    }

    private fun checkRecordingPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(
                        object : PermissionListener {
                            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                                startRecordingActivity()
                            }

                            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                                showToast("You need to grant recording permissions to record your own sound")
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                    permission: PermissionRequest,
                                    token: PermissionToken
                            ) { /* ... */
                            }
                        })
                .check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                showToast("Sound saved to file")
                createSoundFragment!!.updateList()
                // Great! User has recorded and saved the audio file
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showToast("User canceled!")
            }
        }
    }

    private fun startRecordingActivity() {
        val ownSoundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/own_sounds")
        if (!ownSoundsFolder.exists()) {
            ownSoundsFolder.mkdir()
        }
        val fileName = SimpleDateFormat("yyyyMMddHHmmss'.wav'").format(Date())
        val filePath = Environment.getExternalStorageDirectory().toString() + "/Relaxoo/own_sounds/" + fileName
        val color = resources.getColor(R.color.colorPrimaryDark)
        val requestCode = RECORDING_REQ_CODE
        AndroidAudioRecorder.with(this) // Required
                .setFilePath(filePath)
                .setColor(color)
                .setRequestCode(requestCode) // Optional
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.STEREO)
                .setSampleRate(AudioSampleRate.HZ_48000)
                .setAutoStart(false)
                .setKeepDisplayOn(true) // Start recording
                .record()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated: called with: $billingResult")
        val responseCode = billingResult.responseCode
        if (responseCode == BillingClient.BillingResponseCode.OK || responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Log.d(
                TAG,
                "onPurchasesUpdated: called with: " + "Purchase successfull or Item already purchased"
            )
            sharedViewModel.adsBought(true)
        }
    }

    override fun onRewardedVideoAdClosed() {
        loadRewardedVideoAd()
    }

    override fun onRewardedVideoAdLeftApplication() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onRewardedVideoAdLoaded() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onRewardedVideoAdOpened() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onRewardedVideoCompleted() {
        Log.d(TAG, "onRewardedVideoCompleted: called")
        getSoundGridFragment()?.rewardUserByPlayingProSound()
        loadRewardedVideoAd()
    }

    private fun loadRewardedVideoAd() {
        rewardedVideoAd.loadAd(adUnitId, AdRequest.Builder().build())
    }

    override fun onRewarded(p0: RewardItem?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onRewardedVideoStarted() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun serviceDestroyed(eventBusServiceDestroyed: EventBusServiceDestroyed) {
        // the reason for this is that if the service gets destroyed, which holds all the logic for the soundpool
        // and timer, the activity needs to restart to recreate the service. Therefore forcing a finish on the applications
        // i am thinking that the user does not want to use the service and the application anymore if he presses 'X'
        Log.d(TAG, "serviceDestroyed: called")
        this.finish()
    }
}
