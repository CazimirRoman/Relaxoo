package com.cazimir.relaxoo

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.lifecycle.Observer
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
import com.cazimir.relaxoo.dialog.permission.OnStoragePermissionCallback
import com.cazimir.relaxoo.dialog.permission.PermissionNeededDialog
import com.cazimir.relaxoo.dialog.pro.BottomProDialogFragment
import com.cazimir.relaxoo.dialog.recording.BottomRecordingDialogFragment
import com.cazimir.relaxoo.dialog.timer.OnTimerDialogCallback
import com.cazimir.relaxoo.dialog.timer.TimerDialog
import com.cazimir.relaxoo.eventbus.EventBusServiceDestroyed
import com.cazimir.relaxoo.model.ListOfSavedCustom
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.model.SavedCombo
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.service.SoundService
import com.cazimir.relaxoo.service.commands.LoadCustomSoundCommand
import com.cazimir.relaxoo.service.commands.UnlockProCommand
import com.cazimir.relaxoo.shared.SharedViewModel
import com.cazimir.relaxoo.shared.UnlockProEvent
import com.cazimir.relaxoo.ui.about.AboutFragment
import com.cazimir.relaxoo.ui.create_sound.CreateSoundFragment
import com.cazimir.relaxoo.ui.create_sound.OnRecordingStarted
import com.cazimir.relaxoo.ui.favorites.FavoritesFragment
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback
import com.cazimir.relaxoo.ui.sound_grid.OnFavoriteSaved
import com.cazimir.relaxoo.ui.sound_grid.SoundGridFragment
import com.cazimir.utilitieslibrary.InternetUtil
import com.cazimir.utilitieslibrary.SharedPreferencesUtil.loadFromSharedPreferences
import com.cazimir.utilitieslibrary.SharedPreferencesUtil.saveToSharedPreferences
import com.cazimir.utilitieslibrary.showSnackbar
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
        private const val TAG = "MainActivity"
        private const val LIFECYCLE = "Lifecycle"
        private const val RECORDING_REQ_CODE = 6788
        private const val STORAGE_PERMISSIONS_REQ_CODE = 123
        private const val RECORDING_PERMISSIONS_REQ_CODE = 321
        const val PINNED_RECORDINGS = "PINNED_RECORDINGS"
        private const val REMOVE_ADS = "remove_ads"
        private const val BUY_PRO = "buy_pro"
    }

    private var showSnackbar: Snackbar? = null
    private var doubleBackToExitPressedOnce: Boolean = false
    private lateinit var adView: AdView
    private lateinit var adUnitId: String
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var billingClient: BillingClient

    private lateinit var rewardedVideoAd: RewardedVideoAd

    private val areWritePermissionsGranted = MutableLiveData<Boolean>()
    private val isSoundGridFragmentStarted = MutableLiveData<Boolean>()
    private val isInternetAvailable = MutableLiveData<Boolean>()

    private var preconditionsToStartFetchingData: PreconditionsToStartFetchingData = PreconditionsToStartFetchingData()

    private val skuListAds = listOf(REMOVE_ADS)
    private val skuListPro = listOf(BUY_PRO)

    private var timerDialog: TimerDialog? = null
    private var soundGridFragment: SoundGridFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        EventBus.getDefault().register(this)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        subscribeObservers()
        //using this because the splash will be shown again if the user rotates the screen
        shouldHideSplash()
        setupViewPager()
        setupViewPagerDots()
        startColorChangeAnimation()
        checkPermissions()
        // should also be done in onResume()
        checkUserPurchases()
        setupRewardVideoAd()
    }

    private fun subscribeObservers() {
        val result = MediatorLiveData<PreconditionsToStartFetchingData>()
        InternetUtil.init(application)
        InternetUtil.observe(this, Observer { internetUp ->
            isInternetAvailable.value = internetUp
            when {
                internetUp -> {
                    if (splash.visibility == VISIBLE) {
                        no_internet_text.visibility = GONE

                    } else {
                        showSnackbar?.dismiss()
                    }
                }
                else -> {
                    if (splash.visibility == VISIBLE) {
                        no_internet_text.visibility = VISIBLE

                    } else {
                        showMessageToUser(getString(R.string.no_internet), Snackbar.LENGTH_INDEFINITE)
                    }
                }
            }
        })

        result.addSource(
                areWritePermissionsGranted) { permissionsGranted: Boolean ->
            Log.d(TAG, "permissions granted: $permissionsGranted")
            preconditionsToStartFetchingData = preconditionsToStartFetchingData.copy(arePermissionsGranted = permissionsGranted)
            result.setValue(preconditionsToStartFetchingData)
        }

        result.addSource(isSoundGridFragmentStarted) { fragmentStarted: Boolean ->
            Log.d(TAG, "soundGridFragmentStarted: $fragmentStarted")
            preconditionsToStartFetchingData = preconditionsToStartFetchingData.copy(isFragmentStarted = fragmentStarted)
            result.setValue(preconditionsToStartFetchingData)
        }

        result.addSource(isInternetAvailable) { internetUp ->
            preconditionsToStartFetchingData = preconditionsToStartFetchingData.copy(isInternetUp = internetUp)
            result.setValue(preconditionsToStartFetchingData)
        }

        // TODO: 14-Mar-20 This observer is called 2 times - fix it
        result.observeUntil(
                this,
                Observer { preconditionsToStartFetchingData: PreconditionsToStartFetchingData ->
                    Log.d(
                            TAG, (
                            "onChanged() called with: preconditionsToStartFetchingData: " +
                                    preconditionsToStartFetchingData.toString()))
                    if (preconditionsToStartFetchingData.isFragmentStarted && preconditionsToStartFetchingData.arePermissionsGranted && preconditionsToStartFetchingData.isInternetUp) {

                        if (!sharedViewModel.splashScreenShown) {
                            Log.d(
                                    TAG, "sounds already fetched: " + getSoundGridFragment().shouldLoadToSoundpool())
                            getSoundGridFragment().fetchSounds()
                        }


                    }
                })

        // remove ads logic - listen to observer in viewmodel
        sharedViewModel.adsBought.observe(this, Observer { adsBought: Boolean ->
            if (adsBought) {
                removeAdsViewAndButtonInAbout()
            } else {
                loadAds()
            }
        })

        sharedViewModel.proBought.observe(this, Observer { event: UnlockProEvent ->
            if (event.proBought && !event.eventProcessed) {
                activateAllProSounds()
                // TODO: 27.04.2020 create a similar event to UnlockProEvent to not call this method if it has been already called in the above 'observe'
                removeAdsViewAndButtonInAbout()
            }
        })
    }

    private fun activateAllProSounds() {
        sendCommandToService(SoundService.getCommand(this, UnlockProCommand()))
        sharedViewModel.updateProcessedUnlockProEvent()
    }

    // extension function - observe until members on a object are all true then terminate (remove observer)
    private fun <T> LiveData<T>.observeUntil(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                val conditions = t as PreconditionsToStartFetchingData
                if (conditions.areAllConditionsMet()) {
                    observer.onChanged(t)
                    removeObserver(this)
                }
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

        showMessageToUser(getString(R.string.back_exit), Snackbar.LENGTH_SHORT)

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    private fun shouldHideSplash() {
        if (sharedViewModel.splashScreenShown) {
            hideSplashScreen()
        }
    }

    private fun checkUserPurchases() {
        billingClient =
                BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
        billingClient.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "onBillingSetupFinished() called with: success!")
                            val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                            if (purchasesResult.purchasesList.size != 0) {
                                if (purchasesResult.purchasesList[0].sku == BUY_PRO) {
                                    sharedViewModel.updateBoughtPro()
                                } else if (purchasesResult.purchasesList[0].sku == REMOVE_ADS) {
                                    sharedViewModel.updateBoughtAds()
                                }
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
                    .setSkusList(skuListAds)
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
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED) {
            areWritePermissionsGranted.value = true
        } else {
            PermissionNeededDialog(object : OnStoragePermissionCallback {
                override fun okClicked() {
                    requestStoragePermissions()
                }
            }, getString(R.string.permission_storage_denied_message), getString(R.string.permission_storage_denied_title)).show(supportFragmentManager, "storagePermission")
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            STORAGE_PERMISSIONS_REQ_CODE -> {
                handleStoragePermissionResult(permissions, grantResults)
            }

            RECORDING_PERMISSIONS_REQ_CODE -> {
                handleRecordingPermissionResult(permissions, grantResults)
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun handleStoragePermissionResult(permissions: Array<out String>, grantResults: IntArray) {
        // clear any left over dialog // the retainInstance thingie creates a new dialog on each rotation, therefore this hack is needed to dismiss all created dialogs
        val dialogFragmentList: List<DialogFragment>? = supportFragmentManager.fragments.filter { it.tag == "storagePermission" } as List<DialogFragment>
        dialogFragmentList?.forEach {
            it.dismiss()
        }

        val denied = grantResults.indices.filter { grantResults[it] != PackageManager.PERMISSION_GRANTED }

        // all permission granted
        if (denied.isEmpty()) {
            areWritePermissionsGranted.value = true
        } else {
            permissions.forEach {
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, it)
                // will it show again?
                if (!showRationale) {
                    PermissionNeededDialog(object : OnStoragePermissionCallback {
                        override fun okClicked() {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                            finish()
                        }
                    }, getString(R.string.permission_storage_denied_do_not_show_message), getString(R.string.permission_storage_denied_title)).show(supportFragmentManager, "storagePermission")

                } else {
                    PermissionNeededDialog(object : OnStoragePermissionCallback {
                        override fun okClicked() {
                            requestStoragePermissions()
                        }
                    }, getString(R.string.permission_storage_denied_message), getString(R.string.permission_storage_denied_title)).show(supportFragmentManager, "storagePermission")

                }
            }
        }
    }

    private fun handleRecordingPermissionResult(permissions: Array<out String>, grantResults: IntArray) {
        // clear any left over dialog  // the retainInstance thingie creates a new dialog on each rotation, therefore this hack is needed to dismiss all created dialogs
        val dialogFragmentList: List<DialogFragment>? = supportFragmentManager.fragments.filter { it.tag == "storagePermission" } as List<DialogFragment>
        dialogFragmentList?.forEach {
            it.dismiss()
        }

        val denied = grantResults.indices.filter { grantResults[it] != PackageManager.PERMISSION_GRANTED }

        // all permission granted
        if (denied.isEmpty()) {
            startRecordingActivity()
        } else {
            permissions.forEach {
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, it)
                // will it show again?
                if (!showRationale) {
                    PermissionNeededDialog(object : OnStoragePermissionCallback {
                        override fun okClicked() {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                    }, getString(R.string.permission_recording_denied_do_not_show), getString(R.string.permission_recording_denied_title)).show(supportFragmentManager, "storagePermission")

                } else {
                    PermissionNeededDialog(object : OnStoragePermissionCallback {
                        override fun okClicked() {
                            requestStoragePermissions()
                        }
                    }, getString(R.string.permission_recording_denied), getString(R.string.permission_recording_denied_title)).show(supportFragmentManager, "storagePermission")

                }
            }
        }
    }

    private fun requestStoragePermissions() {
        requestPermissionsGeneric(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSIONS_REQ_CODE)
    }

    private fun requestRecordingPermissions() {
        requestPermissionsGeneric(arrayOf(Manifest.permission.RECORD_AUDIO), RECORDING_PERMISSIONS_REQ_CODE)
    }

    private fun requestPermissionsGeneric(permissions: Array<String>, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
    }

    private fun startColorChangeAnimation() {
        sharedViewModel
                .nextColor
                .observe(
                        this,
                        Observer { backgroundColor: Int? ->
                            val duration = 1500
                            val animator: ObjectAnimator = ObjectAnimator.ofObject(
                                            parent_layout_main,
                                    "backgroundColor",
                                    ArgbEvaluator(),
                                    sharedViewModel.previousColor,
                                    sharedViewModel.nextColor.value)
                                    .setDuration(duration.toLong())
                            animator.addListener(
                                    object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator) {
                                            Log.d(TAG, "onAnimationEnd: called")
                                            sharedViewModel.previousColor = sharedViewModel.nextColor.value
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
                                sharedViewModel.nextColor.value = randomColor
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
            return colorArray[random.nextInt(colorArray.size)]
        }

    private val colorArray = listOf(
            Color.argb(255, 115, 44, 44),
            Color.argb(255, 66, 75, 84),
            Color.argb(255, 179, 141, 151),
            Color.argb(255, 255, 67, 101),
            Color.argb(255, 105, 153, 93),
            Color.argb(255, 2, 8, 135),
            Color.argb(255, 9, 56, 36),
            Color.argb(255, 123, 136, 107),
            Color.argb(255, 46, 82, 102),
            Color.argb(255, 164, 74, 63),
            Color.argb(255, 175, 27, 63)
    )

    override fun showAddToFavoritesDialog(playingSounds: List<Sound>) {
        SaveToFavoritesDialog(playingSounds).show(supportFragmentManager, "save")
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
        getSoundGridFragment().triggerCombo(savedCombo, sharedViewModel.proBought.value?.proBought)
        if (sharedViewModel.proBought.value?.proBought == false) {
            showMessageToUser(getString(R.string.playing_except_pro), Snackbar.LENGTH_SHORT)
        } else {
            showMessageToUser(getString(R.string.playing_saved_combo), Snackbar.LENGTH_SHORT)
        }
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
        showMessageToUser(getString(R.string.saved_combo), Snackbar.LENGTH_SHORT)
    }

    private val favoriteFragment: FavoritesFragment?
        get() = supportFragmentManager
                .findFragmentByTag("f1") as FavoritesFragment?

    private fun getSoundGridFragment(): SoundGridFragment {

        return if (supportFragmentManager.findFragmentByTag("f0") != null) {
            supportFragmentManager
                    .findFragmentByTag("f0") as SoundGridFragment
        } else {
            supportFragmentManager.fragments[0] as SoundGridFragment
        }
    }

    private fun getAboutFragment(): AboutFragment {
        return supportFragmentManager
                .findFragmentByTag("f3") as AboutFragment
    }

    private val createSoundFragment: CreateSoundFragment?
        get() = supportFragmentManager
                .findFragmentByTag("f2") as CreateSoundFragment?

    override fun startCountDownTimer(minutes: Int) {
        soundGridFragment!!.startCountDownTimer(minutes)
    }

    override fun showBottomDialogForPro() {
        BottomProDialogFragment(this).show(supportFragmentManager, "pro")
    }

    override fun soundGridFragmentStarted() {
        Log.d(TAG, "soundGridFragmentStarted() called")
        if (soundGridFragment == null) soundGridFragment = getSoundGridFragment()
        isSoundGridFragmentStarted.value = true
    }

    override fun hideSplashScreen() {
        Log.d(TAG, "hideSplash: called")
        hideProgress()
        splash.visibility = GONE
        main_layout.visibility = VISIBLE
        sharedViewModel.splashScreenShown = true
        Log.d(TAG, "EspressoIdlingResource.decrement called")
        EspressoIdlingResource.decrement()
    }

    override fun startBuyingProFlow() {

        if (billingClient.isReady) {
            val skuDetailsParams = SkuDetailsParams.newBuilder()
                    .setSkusList(skuListPro)
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
                    }
                } else {
                    Log.d(TAG, "loadAllSKUs() called with: skuDetailsList is empty")
                }
            }
        } else {
            Log.d(TAG, "loadAllSKUs() called with: billingClient is not ready")
        }


        // this should happen after purchase confirmation
//        getSoundGridFragment().activateAllProSounds()
    }

    override fun removeAds() {
        launchFlowToRemoveAds()
    }

    override fun removeAdsViewAndButtonInAbout() {

        getAboutFragment().hideRemoveAdsButton()

        adView.parent?.let {
            val parent: ViewGroup = it as ViewGroup
            parent.removeView(adView)
            parent.invalidate()
        }
    }

    override fun deleteRecording(recording: Recording) {
        //should also search is any custom sounds are pinned to dashboard and remove those as well
        createSoundFragment!!.deleteRecording(recording)
        getSoundGridFragment().removeCustomSoundFromDashboardIfThere(recording)
    }

    override fun showMessageToUser(messageToShow: String, length: Int) {
        showSnackbar = showSnackbar(coordinator, messageToShow, length)

    }

    override fun renameRecording(recording: Recording, newName: String) {
        createSoundFragment!!.renameRecording(recording, newName)
        getSoundGridFragment().renameCustomSoundFromDashboardIfThere(recording, newName)
    }

    override fun pinToDashBoardActionCalled(sound: Sound) {

        val pinnedRecordings = loadFromSharedPreferences<ListOfSavedCustom>(PINNED_RECORDINGS)
        val list = pinnedRecordings?.savedCustomList ?: mutableListOf()

        if (list.contains(sound)) {
            showMessageToUser(getString(R.string.sound_already_pinned), Snackbar.LENGTH_SHORT)
            return
        }

        Log.d(TAG, "pinToDashBoardActionCalled: called. Sending command to service: LoadCustomSoundCommand")
        sendCommandToService(SoundService.getCommand(this, LoadCustomSoundCommand(sound)))

        try {
            val pinnedRecordings = loadFromSharedPreferences<ListOfSavedCustom>(PINNED_RECORDINGS)
            val list = pinnedRecordings?.savedCustomList ?: mutableListOf()
            list.add(sound)
            val newObject = ListOfSavedCustom(list)
            saveToSharedPreferences(newObject, PINNED_RECORDINGS)
        } catch (e: JsonSyntaxException) {
            saveToSharedPreferences(ListOfSavedCustom(mutableListOf(sound)), PINNED_RECORDINGS)
        }

        scrollViewPager()
    }

    private fun sendCommandToService(intent: Intent) {
        Log.d(TAG, "sendCommandToService: called with command: ${intent.getSerializableExtra(SoundService.SOUND_POOL_ACTION)}")
        startService(intent)
    }

    override fun playRewardAd() {
        if (rewardedVideoAd.isLoaded) {
            rewardedVideoAd.show()
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
    }

    private fun checkRecordingPermission() {

        val recordingPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

        if (recordingPermission == PackageManager.PERMISSION_GRANTED) {
            startRecordingActivity()
        } else {
            requestRecordingPermissions()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                showMessageToUser(getString(R.string.sound_saved), Snackbar.LENGTH_SHORT)
                createSoundFragment!!.updateList()
                // Great! User has recorded and saved the audio file
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showMessageToUser(getString(R.string.canceled), Snackbar.LENGTH_SHORT)
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
                    "onPurchasesUpdated: called with: Purchase succesfull or Item already purchased: $purchases[0]!!.sku"
            )

            // pro includes also remove ads
            if (purchases?.get(0)?.sku == BUY_PRO) {
                sharedViewModel.updateBoughtPro()
            } else if (purchases?.get(0)?.sku == REMOVE_ADS) {
                sharedViewModel.updateBoughtAds()
            }

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
        getSoundGridFragment().rewardUserByPlayingProSound()
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
