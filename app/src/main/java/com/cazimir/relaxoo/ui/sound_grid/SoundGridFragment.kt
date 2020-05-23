package com.cazimir.relaxoo.ui.sound_grid

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.adapter.GridRecyclerViewAdapter
import com.cazimir.relaxoo.analytics.AnalyticsEvents.Companion.muteClicked
import com.cazimir.relaxoo.analytics.AnalyticsEvents.Companion.randomClicked
import com.cazimir.relaxoo.analytics.AnalyticsEvents.Companion.removeFromDashboard
import com.cazimir.relaxoo.analytics.AnalyticsEvents.Companion.soundClicked
import com.cazimir.relaxoo.analytics.AnalyticsEvents.Companion.timerClicked
import com.cazimir.relaxoo.dialog.custom.BottomCustomDeleteFragment
import com.cazimir.relaxoo.dialog.custom.CustomBottomCallback
import com.cazimir.relaxoo.eventbus.*
import com.cazimir.relaxoo.model.ListOfSavedCustom
import com.cazimir.relaxoo.model.SavedCombo
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.model.TimerData
import com.cazimir.relaxoo.service.SoundService
import com.cazimir.relaxoo.service.commands.*
import com.cazimir.relaxoo.shared.SharedViewModel
import com.cazimir.utilitieslibrary.SharedPreferencesUtil.loadFromSharedPreferences
import com.cazimir.utilitieslibrary.SharedPreferencesUtil.saveToSharedPreferences
import com.cazimir.utilitieslibrary.observeOnceOnListEmptyWithOwner
import com.cazimir.utilitieslibrary.observeOnceOnListNotEmptyWithOwner
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.sound_list_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class SoundGridFragment : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel
    private var timerRunning: Boolean = false
    private var soundsAdapter: GridRecyclerViewAdapter? = null
    private lateinit var viewModel: SoundGridViewModel
    private lateinit var currentlyPlayingSounds: List<Sound>
    private lateinit var activityCallback: OnActivityCallback
    private val timerPickerListener =
            OnTimeSetListener { view: TimePicker?, hours: Int, minutes: Int ->
                if ((hours * 60) + minutes != 0) {
                    toggleCountdownTimer((hours * 60) + minutes)
                } else {
                    activityCallback.showMessageToUser(getString(R.string.custom_timer_zero), Snackbar.LENGTH_SHORT)
                }
            }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view =
                inflater.inflate(R.layout.sound_list_fragment, container, false)
        return view
    }

    private fun playStopSound(sound: Sound) {
        if (sound.playing) {
            sendCommandToService(
                    SoundService.getCommand(
                            context,
                            StopCommand(sound)
                    )
            )
        } else {
            if (sound.loaded) {
                sendCommandToService(
                        SoundService.getCommand(
                                context,
                                PlayCommand(sound)
                        )
                )
            } else {
                activityCallback.showMessageToUser(getString(R.string.playing_except_loading), Snackbar.LENGTH_SHORT)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnActivityCallback) {
            activityCallback = context
        }
    }

    private fun stopAllSounds() {
        sendCommandToService(
                SoundService.getCommand(
                        context,
                        StopAllSoundsCommand()
                )
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        sharedViewModel = ViewModelProvider(activity as ViewModelStoreOwner).get(SharedViewModel::class.java)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        // used to pass the savedstate to ViewModel when application is beeing destroyed by the Android OS
        val factory = SavedStateViewModelFactory(activity!!.application, activity as FragmentActivity)

        viewModel = ViewModelProvider(this, factory).get(SoundGridViewModel::class.java)

        setListenersForButtons()

        sounds_recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = sounds_recycler_view.layoutManager as GridLayoutManager
                sharedViewModel.currentScrollPosition = layoutManager.findFirstVisibleItemPosition()
            }
        })

        // region Observers

        viewModel.initialFetchFinished.observeOnceOnListNotEmptyWithOwner(viewLifecycleOwner, Observer { list: List<Sound> ->
            //get these observables to sync with the service after an application restart
            // using this if because of ROTATION
            // what about when relaunching app from notification and resync is needed

            //check is number of sounds on service same as fetched number of sounds
            sendCommandToService(SoundService.getCommand(context, GetNumberOfSoundsCommand(list)))

        })

        // listen for changes to the sound lists live data object to set the adapter for the gridview
        // along with the callback methods (clicked & volume changed)
        viewModel
                .soundsStorage
                .observe( // TODO: 19.12.2019 move in a separate file or inner class
                        viewLifecycleOwner,
                        Observer { sounds: List<Sound> ->
                            val newList = sounds.toList()
                            updateOrSetupAdapter(newList)

                            // if list same size (only changes on properties like playing, loaded etc)
                            if (newList.size == soundsAdapter?.sounds?.size) {
                                val diff = newList.subtract(soundsAdapter?.sounds as List<Sound>)

                                //no difference between the lists
                                if (diff.isNotEmpty()) {
                                    for ((index, value) in diff.withIndex()) {
                                        soundsAdapter?.modifySingleSoundInList(diff.elementAt(index))
                                    }
                                }
                                // if new list size smaller than what is currently in the adapter remove the 'in plus' item from the adapter
                            } else if (newList.size < soundsAdapter?.sounds?.size!!) {

                                val toDelete = soundsAdapter?.sounds?.find {
                                    !newList.contains(it)
                                }

                                soundsAdapter?.removeSingleSoundInList(toDelete!!)

                            } else {
                                newList.forEach {
                                    if (!soundsAdapter?.sounds?.contains(it)!!) {
                                        soundsAdapter?.sounds?.add(it)
                                    }
                                }
                            }
                        }
                )

        // listen to the playing sounds live data object to change the play stop getLogoPath icon on top
        viewModel
                .playingSounds()
                .observe(
                        viewLifecycleOwner, Observer<List<Sound?>> { playingList ->

                    currentlyPlayingSounds = playingList as List<Sound>

                    if (playingList.isEmpty()) {
                        play_button.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
                        play_button.tag = getString(R.string.play_button_tag)
                        set_timer_button.setImageDrawable(resources.getDrawable(R.drawable.ic_timer_off))
                    } else {
                        play_button.setImageDrawable(resources.getDrawable(R.drawable.ic_stop_white))
                        play_button.tag = getString(R.string.stop_button_tag)
                    }
                })

        viewModel.soundsLoadedToSoundPool
                .observe(
                        viewLifecycleOwner,
                        Observer { soundsAdded ->
                            if (viewModel.soundsStorage.value?.size != 0) {
                                if (soundsAdded == 3) {
                                    activityCallback.hideSplashScreen()
                                }
                            }
                        })
        // endregion
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun callbackWithNumberOfCurrentSounds(lists: EventBusSendNumberOfSounds) {

        // fetched size same as sounds on service. just synchronize
        if (lists.fetchedSounds.size == lists.soundOnService.size) {
            sendCommandToService(SoundService.getCommand(context, AllSoundsCommand()))
            sendCommandToService(SoundService.getCommand(context, TimerTextCommand()))
            sendCommandToService(SoundService.getCommand(context, MuteStatusCommand()))
            //normally you would hide the splash when at least 3 sounds have been loaded to the soundpool. in this case the load happens almost instantaneously. set small delay

            activityCallback.hideSplashScreen()

        } else {
            if (shouldLoadToSoundpool()) {
                loadListToSoundPool(lists.fetchedSounds)
                // because you already did it once per app run
                viewModel.shouldLoadToSoundPool = false
            }

            sendCommandToService(SoundService.getCommand(context, AllSoundsCommand()))
            sendCommandToService(SoundService.getCommand(context, TimerTextCommand()))
            sendCommandToService(SoundService.getCommand(context, MuteStatusCommand()))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun recordingNameUpdated(eventBusUpdateRecordingName: EventBusUpdateRecordingName) {

        val recordingId = eventBusUpdateRecordingName.id
        val recordingNewName = eventBusUpdateRecordingName.newName

        val findRecording = viewModel.soundsStorage.value?.find { it.id == recordingId }

        findRecording?.let {
            removeCustomSoundFromDashboardIfThere(recordingId)

            val sound = Sound(custom = true,
                    filePath = "/storage/emulated/0/Relaxoo/own_sounds/$recordingNewName.wav",
                    logoPath = "/storage/emulated/0/Relaxoo/logos/thunder.png",
                    name = recordingNewName,
                    id = "$recordingNewName.wav")

            activityCallback.pinToDashBoardActionCalled(sound)
        }
    }

    private fun updateOrSetupAdapter(allSounds: List<Sound>) {
        if (soundsAdapter == null) {
            soundsAdapter = GridRecyclerViewAdapter(context!!,
                    allSounds as ArrayList<Sound>,
                    object : OnSoundClickListener {
                        override fun clicked(sound: Sound) {
                            FirebaseAnalytics.getInstance(context!!).logEvent(soundClicked(sound).first, soundClicked(sound).second)
                            if (!sound.loaded) {
                                activityCallback.showMessageToUser(getString(R.string.sound_loading), Snackbar.LENGTH_SHORT)
                            } else if (sound.pro && !sound.playing) {
                                viewModel.setClickedProSound(sound)
                                activityCallback.showBottomDialogForPro()
                            } else {
                                playStopSound(sound)
                            }
                        }

                        override fun volumeChange(
                                sound: Sound,
                                volume: Int
                        ) { // TODO: 18.12.2019 refactor this float to string to double transformation
                            val volumeToSet: Float =
                                    (volume.toDouble() / 100).toString().toFloat()

                            val volumeCommand = SoundService.getCommand(
                                    context, VolumeCommand(sound.id,
                                    sound.streamId,
                                    volumeToSet,
                                    volumeToSet
                            )
                            )

                            sendCommandToService(volumeCommand)
                        }

                        override fun volumeChangeStopped(
                                sound: Sound,
                                progress: Int
                        ) {
                            viewModel.updateVolume(
                                    sound, (progress.toDouble() / 100).toString().toFloat()
                            )
                        }

                        override fun moreOptionsClicked(sound: Sound) {
                            BottomCustomDeleteFragment(sound, object : CustomBottomCallback {
                                override fun deletedClicked(sound: Sound) {
                                    FirebaseAnalytics.getInstance(context!!).logEvent(removeFromDashboard().first, removeFromDashboard().second)
                                    removeRecordingFromSoundPool(sound)
                                }
                            }).show(parentFragmentManager, "deleteCustom")
                        }
                    })

            val numberOfColumns = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 4

            val gridLayoutManager = GridLayoutManager(context, numberOfColumns)
            sounds_recycler_view.layoutManager = gridLayoutManager
            sounds_recycler_view.adapter = soundsAdapter

            sounds_recycler_view.smoothScrollToPosition(sharedViewModel.currentScrollPosition)

        }
    }

    // region Listeners for buttons on top
    private fun setListenersForButtons() {
        mute_button.setOnClickListener {
            FirebaseAnalytics.getInstance(context!!).logEvent(muteClicked().first, muteClicked().second)
            //if no sound is playing do not allow user to mute
            if (currentlyPlayingSounds.isNotEmpty()) {
                sendCommandToService(
                        SoundService.getCommand(
                                context,
                                ToggleMuteCommand()
                        )
                )
            } else {
                activityCallback.showMessageToUser(getString(R.string.play_one_sound), Snackbar.LENGTH_SHORT)
            }
        }
        random_button.setOnClickListener {
            FirebaseAnalytics.getInstance(context!!).logEvent(randomClicked().first, randomClicked().second)
            stopAllSounds()

            activityCallback.showMessageToUser(getString(R.string.playing_random), Snackbar.LENGTH_SHORT)

            /*observe playing sounds once to make sure that an empty list is delivered and the observe once and remove observer*/
            viewModel.playingSounds().observeOnceOnListEmptyWithOwner(viewLifecycleOwner, Observer {
                // total number of available sounds can be found in viewmodel in sounds variable
                val listAllSounds: List<Sound> = (viewModel.soundsStorage.value)!!

                val processed = mutableListOf<Sound>()

                while (processed.size < calculateLimit(listAllSounds)) {
                    val randomSound = listAllSounds.random()
                    if (randomSound.loaded && !randomSound.pro && !randomSound.playing && !processed.contains(randomSound)
                    ) {
                        playStopSound(randomSound)
                        processed.add(randomSound)
                    }
                }
            })
        }
        play_button.setOnClickListener {
            if (currentlyPlayingSounds.isNotEmpty()) {
                stopAllSounds()
            }
        }
        save_fav_button.setOnClickListener {
            val atLeastOneIsPlaying = viewModel.soundsStorage.value?.find { sound -> sound.playing }
            if (atLeastOneIsPlaying != null) {
                activityCallback.showAddToFavoritesDialog(currentlyPlayingSounds)
            } else {
                activityCallback.showMessageToUser(getString(R.string.play_one_sound), Snackbar.LENGTH_SHORT)
            }
        }

        set_timer_button.setOnClickListener {
            FirebaseAnalytics.getInstance(context!!).logEvent(timerClicked().first, timerClicked().second)
            val atLeastOneIsPlaying = currentlyPlayingSounds.isNotEmpty()
            if (atLeastOneIsPlaying) {
                if (timerRunning) {
                    stopAllSounds()
                } else {
                    activityCallback.showTimerDialog()
                }
            } else {
                activityCallback.showMessageToUser(getString(R.string.play_one_sound), Snackbar.LENGTH_SHORT)
            }
        }
    }

    private fun calculateLimit(listSize: List<Sound>): Int {

        val notPro = listSize.filter { !it.pro }
        if (notPro.size < MAX_RANDOM_SOUNDS) {
            return notPro.size
        }

        return MAX_RANDOM_SOUNDS
    }

    // endregion
    private fun loadListToSoundPool(sounds: List<Sound>) {
        sendCommandToService(
                SoundService.getCommand(
                        context,
                        LoadSoundsCommand(sounds)
                )
        )
    }

    private fun showTimerText() {
        timerText.visibility = View.VISIBLE
    }

    private fun hideTimerText() {
        timerText.visibility = View.GONE
    }

    private fun setTimerText(text: String) {
        timerText.text = text
    }

    fun startCountDownTimer(minutes: Int) {
        if (minutes == 999) {
            TimePickerDialog(context, timerPickerListener, 0, 0, true).show()
        } else {
            toggleCountdownTimer(minutes)
        }
    }

    private fun toggleCountdownTimer(minutes: Int) {
        sendCommandToService(SoundService.getCommand(context, ToggleCountDownTimerCommand(minutes)))
    }

    fun triggerCombo(savedCombo: SavedCombo, boughtPro: Boolean?) {
        sendCommandToService(SoundService.getCommand(context, TriggerComboCommand(savedCombo.sounds, boughtPro ?: false)))
    }

    fun fetchSoundsOnline() {
        viewModel.fetchSounds()
    }

    fun fetchSoundsOffline() {
        viewModel.fetchSoundsOffline()
    }

    override fun onResume() {
        super.onResume()
        activityCallback.soundGridFragmentStarted()
    }

    fun shouldLoadToSoundpool(): Boolean {
        return viewModel.shouldLoadToSoundPool
    }

    private fun removeRecordingFromSoundPool(sound: Sound) {
        sendCommandToService(
                SoundService.getCommand(
                        context,
                        UnloadSoundCommand(sound)
                )
        )
    }

    private fun scrollToBottom() {
        sounds_recycler_view!!.smoothScrollToPosition(soundsAdapter!!.itemCount)
    }

    fun rewardUserByPlayingProSound() {
        // if not null
        viewModel.currentlyClickedProSound?.let { sound ->
            playStopSound(sound)
        }
    }

    fun sendCommandToService(intent: Intent) {
        context?.startService(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun updateViewModelWithLoad(eventBusLoad: EventBusLoad) {
        viewModel.loadedToSoundPool()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun updateViewModelWithUnload(eventBusUnload: EventBusUnload) {
        val newList = mutableListOf<Sound>()
        val pinnedRecordings = loadFromSharedPreferences<ListOfSavedCustom>(MainActivity.PINNED_RECORDINGS)
        val list = pinnedRecordings?.savedCustomList ?: mutableListOf()

        list.filterTo(newList, {
            it.id != eventBusUnload.sound.id
        })

        val newObject = ListOfSavedCustom(newList)
        saveToSharedPreferences(newObject, MainActivity.PINNED_RECORDINGS)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun updateTimerLiveDataInViewModel(eventBusTimerStarted: EventBusTimer) {
        // no need to put this livedata in viewmodel because it is being observed from a foreground service which never dies unless the user shuts it down.
        eventBusTimerStarted._timer
                .observe(
                        viewLifecycleOwner,
                        Observer { timerData: TimerData ->

                            if (timerData.timerRunning) {
                                showTimerText()
                                setTimerText(timerData.timerText)
                                set_timer_button.setImageDrawable(resources.getDrawable(R.drawable.ic_timer_on))
                                timerRunning = true
                            } else {
                                hideTimerText()
                                setTimerText(timerData.timerText)
                                set_timer_button.setImageDrawable(resources.getDrawable(R.drawable.ic_timer_off))
                                timerRunning = false
                            }
                        }
                )
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun singleSoundLoadedToSoundpoolCallback(eventBusLoad: EventBusLoadSingle) {
        scrollToBottom()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun muteStatusReceived(eventBusLoad: EventBusMuteStatus) {
        eventBusLoad.muteStatus.observe(viewLifecycleOwner, Observer {
            if (it) {
                mute_button.setImageDrawable(resources.getDrawable(R.drawable.ic_mute_on))
                // used for espresso
                mute_button.tag = getString(R.string.mute_on_tag)
            } else {
                mute_button.setImageDrawable(resources.getDrawable(R.drawable.ic_mute_off_white))
                // used for espresso
                mute_button.tag = getString(R.string.mute_off_tag)
            }
        })
    }


    fun removeCustomSoundFromDashboardIfThere(recordingId: String) {
        val filtered = mutableListOf<Sound>()
        viewModel.soundsStorage.value?.filterTo(filtered, predicate = {
            it.id == recordingId
        })

        // i imagine there will be only one
        if (filtered.size != 0) {
            //found recording in the sounds store
            removeRecordingFromSoundPool(filtered.first())
        }
    }

    fun areSoundsStillLoading(): Boolean {

        var areLoading = false

        if (viewModel.soundsStorage.value?.find { sound -> !sound.loaded } != null) {
            areLoading = true
        }

        return areLoading
    }

    companion object {
        private val TAG = "SoundGridFragment"
        private val MAX_RANDOM_SOUNDS = 3

        fun newInstance(): SoundGridFragment {
            return SoundGridFragment()
        }
    }
}
