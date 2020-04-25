package com.cazimir.relaxoo.ui.sound_grid

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.adapter.GridRecyclerViewAdapter
import com.cazimir.relaxoo.dialog.custom.BottomCustomDeleteFragment
import com.cazimir.relaxoo.dialog.custom.CustomBottomCallback
import com.cazimir.relaxoo.eventbus.*
import com.cazimir.relaxoo.model.ListOfSavedCustom
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.model.SavedCombo
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.repository.ModelPreferencesManager
import com.cazimir.relaxoo.service.SoundService
import com.cazimir.relaxoo.service.SoundService.Companion.SOUND_POOL_ACTION
import com.cazimir.relaxoo.service.commands.*
import kotlinx.android.synthetic.main.sound_list_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class SoundGridFragment() : Fragment() {

    private val _allSoundsStopped: MutableLiveData<Boolean> = MutableLiveData(false)
    private var timerRunning: Boolean = false
    private var soundsAdapter: GridRecyclerViewAdapter? = null
    private lateinit var viewModel: SoundGridViewModel
    private lateinit var playingSounds: ArrayList<Sound>
    private lateinit var activityCallback: OnActivityCallback
    private val timerPickerListener =
        OnTimeSetListener { view: TimePicker?, hours: Int, minutes: Int -> toggleCountdownTimer((hours * 60) + minutes) }

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
            Log.d(TAG, "stopping sound")
            sendCommandToService(
                    SoundService.getCommand(
                            context,
                            StopCommand(sound)
                    )
            )
        } else {
            Log.d(TAG, "playing sound")
            sendCommandToService(
                    SoundService.getCommand(
                            context,
                        PlayCommand(sound)
                    )
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "SoundGridFragment onAttach() called")

        if (context is OnActivityCallback) {
            activityCallback = context
        }
    }

    private fun stopAllSounds() {
        Log.d(TAG, "stopAllSounds: called")
        sendCommandToService(
                SoundService.getCommand(
                        context,
                        StopAllSoundsCommand()
                )
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: called")
        super.onActivityCreated(savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        // used to pass the savedstate to ViewModel when application is beeing destroyed by the Android OS
        val factory = SavedStateViewModelFactory(activity!!.application, activity as FragmentActivity)

        viewModel = ViewModelProvider(this, factory).get(SoundGridViewModel::class.java)

        setListenersForButtons()

        // region Observers
        viewModel._fetchFinished.observeOnce(viewLifecycleOwner, Observer { finished: Boolean ->
            Log.d(TAG, "_fetchFinished.observeOnce called with: $finished ")
            sendCommandToService(SoundService.getCommand(context, PlayingSoundsCommand()))
            sendCommandToService(SoundService.getCommand(context, TimerTextCommand()))
            sendCommandToService(SoundService.getCommand(context, MuteStatusCommand()))
        })

        // listen for changes to the sound lists live data object to set the adapter for the gridview
        // along with the callback methods (clicked & volume changed)
        viewModel
                .allSounds()
                .observe( // TODO: 19.12.2019 move in a separate file or inner class
                        viewLifecycleOwner,
                        Observer { sounds: List<Sound> ->
                            Log.d(
                                    TAG,
                                    "Sound list changed: $sounds"
                            )

                            if (soundsAdapter == null) {
                                soundsAdapter = GridRecyclerViewAdapter(
                                        sounds as ArrayList<Sound>,
                                        object : OnSoundClickListener {
                                            override fun clicked(sound: Sound) {
                                                if (sound.pro && !sound.playing) {
                                                    viewModel.setClickedProSound(sound)
                                                    activityCallback.showBottomDialogForPro()
                                                } else if (!sound.loaded) {
                                                    activityCallback.showSnackBar(getString(R.string.sound_loading))
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
                                                Log.d(
                                                        TAG,
                                                        "volumeChange: called with volume: $volumeToSet"
                                                )

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
                                                        removeRecordingFromSoundPool(sound)
                                                    }
                                                }).show(parentFragmentManager, "deleteCustom")
                                            }
                                        })

                                val numberOfColumns = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 4

                                val gridLayoutManager = GridLayoutManager(context, numberOfColumns)
                                sounds_recycler_view.layoutManager = gridLayoutManager
                                sounds_recycler_view.adapter = soundsAdapter


                            } else {
                                // TODO: 21-Apr-20 this is called so many times!!
                                if (sounds.isNotEmpty()) {
                                    Log.d(TAG, "gridArrayAdapter!!.refreshList called with: $sounds ")
                                    soundsAdapter!!.refreshList(sounds)
                                }

                            }

                            // if sound not loaded yet and sounds list not yet populated
                            if (sounds.isNotEmpty() && atLeastOneSoundWithoutSoundPoolId(sounds)) {
                                // saveListToSharedPreferences(sounds)
                                // hopefully the service is started by the 'play command'
                                loadListToSoundPool(sounds)
                            }
                        }
                )
        // listen to the playing sounds live data object to change the play stop getLogoPath icon on top
        viewModel
                .playingSounds()
                .observe(
                        viewLifecycleOwner, Observer<List<Sound?>> { playingList ->

                    Log.d(TAG, "observer for playingSounds called with: $playingList")

                    playingSounds = playingList as ArrayList<Sound>

                    if (playingList.isEmpty()) {
                        play_button.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
                        set_timer_button.setImageDrawable(resources.getDrawable(R.drawable.ic_timer_off))
                    } else {
                        play_button.setImageDrawable(resources.getDrawable(R.drawable.ic_stop))
                    }
                })

        // TODO: 06-Jan-20 move this to viewmodel

        viewModel.soundsLoadedToSoundPool
                .observe(
                        viewLifecycleOwner,
                        Observer { soundsAdded ->
                            Log.d(TAG, "viewModel.soundsLoadedToSoundPool: called with: $soundsAdded")
                            if (viewModel.allSounds().value?.size != 0) {
                                if (soundsAdded == 3) {
                                    activityCallback.hideSplashScreen()
                                }
                            }
                        })

        // first find out when the timer text observable from the service has arrived and after that start observing the timerText
        viewModel
                .timerTextLiveFetched()
                .observe(viewLifecycleOwner, Observer {
                    viewModel
                            .timerText()
                            .observe(
                                    viewLifecycleOwner,
                                    Observer { timerText: String ->
                                        setTimerText(timerText)
                                    }
                            )
                }

                )

        viewModel
                .timerRunningFetched()
                .observe(
                        viewLifecycleOwner,
                        Observer {
                            Log.d(TAG, "timerRunningFetched: called")
                            viewModel.timerRunning().observe(viewLifecycleOwner, Observer { running ->
                                if (running) {
                                    timerRunning = true
                                    showTimerText()
                                    set_timer_button.setImageDrawable(resources.getDrawable(R.drawable.ic_timer_on))
                                } else {
                                    timerRunning = false
                                    hideTimerText()
                                    set_timer_button.setImageDrawable(resources.getDrawable(R.drawable.ic_timer_off))
                                }
                            })
                        })
        // endregion
    }

    private fun atLeastOneSoundWithoutSoundPoolId(sounds: List<Sound>): Boolean {
        for (sound: Sound in sounds) {
            if (sound.soundPoolId == -1) {
                return true
            }
        }
        return false
    }

    // region Listeners for buttons on top
    private fun setListenersForButtons() {
        Log.d(TAG, "setListenersForButtons: called")
        mute_button.setOnClickListener(
            object : View.OnClickListener {
                override fun onClick(v: View) {
                    sendCommandToService(
                        SoundService.getCommand(
                            context,
                            ToggleMuteCommand()
                        )
                    )
                }
            })
        random_button.setOnClickListener {

            // observe _allsoundstopped and trigger a random play if all sounds are stopped

            // i need to set this to false because for whatever reason it stays on true
            _allSoundsStopped.value = false
            // observe once -> wait for above stop to finish
            _allSoundsStopped.observe(viewLifecycleOwner, object : Observer<Boolean> {
                override fun onChanged(allSoundsStopped: Boolean) {
                    if (allSoundsStopped) {
                        // total number of available sounds can be found in viewmodel in sounds variable
                        val listAllSounds: List<Sound> = (viewModel.allSounds().value)!!

                        val processed = mutableListOf<Sound>()

                        while (processed.size < calculateLimit(listAllSounds)) {
                            Log.d(TAG, "while called: with processed size: ${processed.size}")
                            val randomSound = listAllSounds.random()
                            if (!randomSound.pro && !randomSound.playing && !processed.contains(randomSound)
                            ) {
                                playStopSound(randomSound)
                                processed.add(randomSound)
                            }
                        }
                        // remove observer after observer emitted 'true'. we need a single emit otherwise this observer
                        // will trigger each time all the sounds are stopped
                        _allSoundsStopped.removeObserver(this)
                    }
                }

                private fun calculateLimit(listSize: List<Sound>): Int {

                    val notPro = listSize.filter { !it.pro }
                    if (notPro.size < MAX_RANDOM_SOUNDS) {
                        return notPro.size
                    }

                    return MAX_RANDOM_SOUNDS
                }
            })
            stopAllSounds()
        }
        play_button.setOnClickListener(
                object : View.OnClickListener {
                    override fun onClick(v: View) {
                        if (playingSounds.size > 0) {
                            stopAllSounds()
                        }
                    }
                })
        save_fav_button.setOnClickListener {
            val atLeastOneIsPlaying = viewModel.allSounds().value?.find { sound -> sound.playing }
            if (atLeastOneIsPlaying != null) {
                activityCallback.showAddToFavoritesDialog(playingSounds)
            } else {
                activityCallback.showSnackBar(getString(R.string.play_one_sound))
            }
        }

        set_timer_button.setOnClickListener {
            val atLeastOneIsPlaying = playingSounds.size > 0
            if (atLeastOneIsPlaying) {
                if (timerRunning) {
                    stopAllSounds()
                } else {
                    activityCallback.showTimerDialog()
                }
            } else {
                activityCallback.showSnackBar(getString(R.string.play_one_sound))
            }
        }
    }

    // endregion
    private fun loadListToSoundPool(sounds: List<Sound>) {
        Log.d(TAG, "loadToSoundPool: called")

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
        Log.d(TAG, "hideTimerText: called")
        timerText.visibility = View.GONE
    }

    private fun setTimerText(text: String) {
        Log.d(TAG, "setTimerText() called with: $text")
        timerText.text = text
    }

    fun startCountDownTimer(minutes: Int) {
        Log.d(
            TAG,
            "startCountDownTimer: minutes: $minutes"
        )
        if (minutes == 999) {
            TimePickerDialog(context, timerPickerListener, 0, 0, true).show()
        } else {
            toggleCountdownTimer(minutes)
        }
    }

    private fun toggleCountdownTimer(minutes: Int) {
        sendCommandToService(SoundService.getCommand(context, ToggleCountDownTimerCommand(minutes)))
    }

    fun triggerCombo(savedCombo: SavedCombo) {
        sendCommandToService(SoundService.getCommand(context, TriggerComboCommand(savedCombo.sounds)))
    }

    fun fetchSounds() {
        viewModel.fetchSounds()
    }

    override fun onResume() {
        super.onResume()
        Log.d(
            TAG,
            "onResume() called in: $TAG"
        )
        activityCallback.soundGridFragmentStarted()
    }

    fun soundsAlreadyFetched(): Boolean {
        return viewModel.soundsAlreadyFetched()
    }

    private fun removeRecordingFromSoundPool(sound: Sound) {

        Log.d(TAG, "removeRecordingFromSoundPool: called with sound: $sound")

        sendCommandToService(
                SoundService.getCommand(
                        context,
                        UnloadSoundCommand(sound)
                )
        )
    }

    fun addRecordingToSoundPool(sound: Sound) {
        sendCommandToService(SoundService.getCommand(context, LoadCustomSoundCommand(sound)))
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
        Log.d(TAG, "sendCommandToService: called with command: ${intent.getSerializableExtra(SOUND_POOL_ACTION)}")
        context?.startService(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun serviceCallbackStop(eventBusStop: EventBusStop) {
        Log.d(TAG, "updateViewModelWithStop: called with: soundPoolId: ${eventBusStop.sound.soundPoolId}")
        viewModel.updatePlayingSound(eventBusStop.sound)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun serviceCallbackStopAll(eventBusStopAll: EventBusStopAll) {
        Log.d(TAG, "serviceCallbackStopAll: called")
        viewModel.updateViewModelWithPlayingSoundsFalse()
        _allSoundsStopped.value = true
        // TODO: 16.04.2020 move this to the observer of playing sounds
        hideTimerText()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun serviceCallbackSoundPoolLoad(eventBusLoadedToSoundPool: EventBusLoadedToSoundPool) {
        Log.d(
                TAG,
                "updateViewModelSoundsLoadedToSoundPool: called with ${eventBusLoadedToSoundPool.soundPoolId}"
        )
        viewModel.loadedToSoundPool(eventBusLoadedToSoundPool.soundPoolId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun updateViewModelWithLoad(eventBusLoad: EventBusLoad) {
        viewModel.addToSounds(eventBusLoad.sounds)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun updateViewModelWithUnload(eventBusUnload: EventBusUnload) {
        viewModel.removeSingleSoundFromSounds(eventBusUnload.sound.id)

        val newList = mutableListOf<Sound>()
        val pinnedRecordings = ModelPreferencesManager.get<ListOfSavedCustom>(MainActivity.PINNED_RECORDINGS)
        val list = pinnedRecordings?.savedCustomList ?: mutableListOf()

        list.filterTo(newList, {
            it.id != eventBusUnload.sound.id
        })

        Log.d(TAG, "unload called from service. Saving new list to SP: $newList")

        val newObject = ListOfSavedCustom(newList)
        ModelPreferencesManager.save(newObject, MainActivity.PINNED_RECORDINGS)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun singleSoundLoadedToSoundpoolCallback(eventBusLoad: EventBusLoadSingle) {
        Log.d(TAG, "singleSoundLoadedToSoundpoolCallback: called")
        viewModel.addSingleSound(eventBusLoad.sound)
        scrollToBottom()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun muteStatusReceived(eventBusLoad: EventBusMuteStatus) {
        eventBusLoad.muteStatus.observe(viewLifecycleOwner, Observer {
            if (it) {
                mute_button.setImageDrawable(resources.getDrawable(R.drawable.ic_mute_on))
                // used for espresso
                mute_button.tag = getString(R.string.mute_on)
            } else {
                mute_button.setImageDrawable(resources.getDrawable(R.drawable.ic_mute_off))
                // used for espresso
                mute_button.tag = getString(R.string.mute_off)
            }
        })
    }

    fun removeCustomSoundFromDashboardIfThere(recording: Recording) {
        Log.d(TAG, "removeCustomSoundFromDashboardIfThere: called with recording id: ${recording.id}")
        val filtered = mutableListOf<Sound>()
        viewModel.allSounds().value?.filterTo(filtered, predicate = {
            it.id == recording.id
        })

        // i imagine there will be only one
        if (filtered.size != 0) {

            //found recording in the sounds store
            removeRecordingFromSoundPool(filtered.first())
        }
    }

    fun renameCustomSoundFromDashboardIfThere(recording: Recording, newName: String) {
        val filtered = mutableListOf<Sound>()
        viewModel.allSounds().value?.filterTo(filtered, predicate = {
            it.id == recording.id
        })

        if (filtered.size != 0) {
            viewModel.updateNameOnSound(filtered.first(), newName)
        }

        val newList = mutableListOf<Sound>()
        val pinnedRecordings = ModelPreferencesManager.get<ListOfSavedCustom>(MainActivity.PINNED_RECORDINGS)
        val list = pinnedRecordings?.savedCustomList ?: mutableListOf()

        list.mapTo(newList, {
            if (it.id == recording.id) {
                it.copy(name = newName, id = "$newName.wav", filePath = "/storage/emulated/0/Relaxoo/own_sounds/$newName.wav")
            } else {
                it
            }
        })

        val newObject = ListOfSavedCustom(newList)
        ModelPreferencesManager.save(newObject, MainActivity.PINNED_RECORDINGS)

    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                if (t as Boolean) {
                    observer.onChanged(t)
                    removeObserver(this)
                }

            }
        })
    }

    companion object {
        private val TAG = "SoundGridFragment"
        private val MAX_RANDOM_SOUNDS = 3

        fun newInstance(): SoundGridFragment {
            return SoundGridFragment()
        }
    }
}
