package com.cazimir.relaxoo.ui.sound_grid

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.adapter.GridAdapter
import com.cazimir.relaxoo.dialog.custom.BottomCustomDeleteFragment
import com.cazimir.relaxoo.dialog.custom.CustomBottomCallback
import com.cazimir.relaxoo.eventbus.EventBusLoad
import com.cazimir.relaxoo.eventbus.EventBusLoadedToSoundPool
import com.cazimir.relaxoo.eventbus.EventBusStop
import com.cazimir.relaxoo.eventbus.EventBusStopAll
import com.cazimir.relaxoo.model.SavedCombo
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.service.SoundPoolService
import com.cazimir.relaxoo.service.SoundPoolService.Companion.SOUND_POOL_ACTION
import com.cazimir.relaxoo.service.events.*
import kotlinx.android.synthetic.main.sound_list_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class SoundGridFragment() : Fragment() {

    private var gridArrayAdapter: GridAdapter? = null
    private lateinit var viewModel: SoundGridViewModel
    private lateinit var playingSounds: ArrayList<Sound>
    private var muted = false
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
                    SoundPoolService.getCommand(
                            context,
                            StopCommand(sound.id, sound.streamId, sound.soundPoolId)
                    )
            )
        } else {
            Log.d(TAG, "playing sound")
            sendCommandToService(
                    SoundPoolService.getCommand(
                            context,
                            PlayCommand(sound.id, sound.soundPoolId, sound.streamId, 0.5f, 0.5f, 0, -1, 1f)
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
                SoundPoolService.getCommand(
                        context,
                        StopAllSoundsCommand()
                )
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        //used to pass the savedstate to ViewModel when application is beeing destroyed by the Android OS
        val factory = SavedStateViewModelFactory(activity!!.application, activity as FragmentActivity)

        viewModel = ViewModelProvider(this, factory).get(SoundGridViewModel::class.java)

        setListenersForButtons()
        // region Observers
        // change icon to unmute


        viewModel._fetchFinished.observe(viewLifecycleOwner, Observer { finished ->
            if (finished) sendCommandToService(SoundPoolService.getCommand(context, PlayingSoundsCommand()))
        })

        viewModel
                .mutedLiveData()
                .observe(
                        viewLifecycleOwner,
                        Observer { muted ->
                            if (muted) {
                                mute_button.setImageDrawable(resources.getDrawable(R.drawable.ic_mute_off))
                            } else {
                                mute_button.setImageDrawable(resources.getDrawable(R.drawable.ic_mute_on))
                    }
                })
        // listen for changes to the sound lists live data object to set the adapter for the gridview
// along with the callback methods (clicked & volume changed)
        viewModel
                .allSounds()
            .observe( // TODO: 19.12.2019 move in a separate file or inner class
                viewLifecycleOwner,
                Observer { sounds: ArrayList<Sound> ->
                    Log.d(
                        TAG,
                        "Sound list changed: " + sounds
                    )
                    gridArrayAdapter = GridAdapter(
                            context,
                            sounds,
                            object : OnSoundClickListener {
                                override fun clicked(sound: Sound) {
                                    if (sound.pro && !sound.playing) {
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
                                Log.d(
                                    TAG,
                                    "volumeChange: called with volume: " + volumeToSet
                                )

                                val volumeCommand = SoundPoolService.getCommand(
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
                                    }).show(getParentFragmentManager(), "deleteCustom")
                                }
                            })
                    gridView.adapter = gridArrayAdapter
                    // if sound not loaded yet and sounds list not yet populated
                    if (!sounds.isEmpty() && atLeastOneSoundWithoutSoundPoolId(sounds)) {
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
                    set_timer_button.setImageDrawable(resources.getDrawable(R.drawable.ic_timer_on))
                    if (viewModel.countDownTimer() != null) {
                        viewModel.timerEnabled().value = false
                    }
                } else {
                    play_button.setImageDrawable(resources.getDrawable(R.drawable.ic_stop))
                }
            })

        // TODO: 06-Jan-20 move this to viewmodel
        viewModel
            .timerEnabled()
            .observe(
                viewLifecycleOwner,
                Observer<Boolean> { isTimerEnabled ->
                    Log.d(
                        TAG,
                        "timerEnabled: $isTimerEnabled"
                    )
                    if (isTimerEnabled) {
                        showTimerText()
                        set_timer_button.setImageDrawable(resources.getDrawable(R.drawable.ic_timer_off))
                    } else {
                        hideTimerText()
                        set_timer_button.setImageDrawable(resources.getDrawable(R.drawable.ic_timer_on))
                        viewModel.countDownTimer()!!.cancel()
                        Log.d(
                            TAG,
                            "countDownTimer canceled!"
                        )
                    }
                })
        viewModel.soundsLoadedToSoundPool
            .observe(
                    viewLifecycleOwner,
                    Observer { soundsAdded ->
                        Log.d(TAG, "viewModel.soundsLoadedToSoundPool: called with: ${soundsAdded}")
                        if (viewModel.allSounds().safeValue?.size != 0) { // hide splash if viewmodel sound livedata size equals the number of sounds added to the soundpool
                            if (soundsAdded == viewModel.allSounds().safeValue?.size) {
                                activityCallback.hideSplash()
                            }
                        }
                    })
        viewModel
            .timerFinished()
            .observe(
                viewLifecycleOwner,
                Observer { finished: Boolean ->
                    if (finished) {
                        set_timer_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_timer_on))
                        viewModel.timerEnabled().setValue(false)
                        stopAllSounds()
                        hideTimerText()
                    }
                }
            )
        viewModel
            .timerText()
            .observe(
                viewLifecycleOwner,
                Observer { timerText: String ->
                    setTimerText(
                        timerText
                    )
                }
            )

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
                    muted = !muted
                    if (muted) {
                        for (sound: Sound in playingSounds) {

                            sendCommandToService(
                                    SoundPoolService.getCommand(
                                            context,
                                            VolumeCommand(sound.id, sound.streamId, 0f, 0f)
                                    )
                            )
                        }
                    } else {
                        for (sound: Sound in playingSounds) {

                            sendCommandToService(
                                    SoundPoolService.getCommand(
                                            context,
                                            VolumeCommand(sound.id, sound.streamId, sound.volume, sound.volume)
                                    )
                            )
                        }
                    }
                    viewModel.updateMuteLiveData(muted)
                }
            })
        random_button.setOnClickListener {

            stopAllSounds()
            val totalNumberOfSounds = Random()
            // total number of available sounds can be found in viewmodel in sounds variable
            val value: List<Sound> = (viewModel.allSounds().value)!!
            for (i in value.indices) {
                Log.d(
                        TAG,
                        "randomClick for loop called: $i"
                )
                val sound = value[totalNumberOfSounds.nextInt(value.size)]
                if (!sound.pro && !sound.playing) {
                    playStopSound(sound)
                }
            }
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
                activityCallback.showToast("You must play at least one sound")
            }
        }
        set_timer_button.setOnClickListener(
                object : View.OnClickListener {
                    override fun onClick(v: View) {
                        val atLeastOneIsPlaying = viewModel.allSounds().value?.find { sound -> sound.playing }
                        if (atLeastOneIsPlaying != null) {
                            val timerIsRunning = viewModel.timerEnabled().value
                            if (timerIsRunning != null && timerIsRunning) {
                                viewModel.timerEnabled().setValue(false)
                            } else { // show TimerDialog fragment created with getFilePath template
                                activityCallback.showTimerDialog()
                            }
                        } else {
                            activityCallback.showToast("You must play at least one sound")
                        }
                    }
                })
    }

    private fun getSoundParameters(sounds: List<Sound>): MutableList<String> {

        val listWithSoundId = mutableListOf<String>()
        sounds.forEach { sound -> listWithSoundId.add(sound.id) }
        return listWithSoundId
    }

    // endregion
    private fun loadListToSoundPool(sounds: ArrayList<Sound>) {
        Log.d(TAG, "loadToSoundPool: called")

        sendCommandToService(
                SoundPoolService.getCommand(
                        context,
                        LoadSoundsCommand(sounds)
                )
        )
    }

    private fun showTimerText() {
        timerText!!.visibility = View.VISIBLE
    }

    private fun hideTimerText() {
        Log.d(TAG, "hideTimerText: called")
        timerText!!.visibility = View.INVISIBLE
    }

    private fun setTimerText(text: String) {
        Log.d(TAG, "setTimerText() called with: $text")
        timerText!!.text = text
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
        if (((viewModel.timerEnabled().value != null
                ) && viewModel.timerEnabled().value!! &&
                (viewModel.countDownTimer() != null))
        ) {
            viewModel.timerEnabled().setValue(false)
        } else {
            viewModel.setCountDownTimer(minutes)
            viewModel.timerEnabled().setValue(true)
        }
    }

    fun triggerCombo(savedCombo: SavedCombo) {
        sendCommandToService(SoundPoolService.getCommand(context, TriggerComboCommand(savedCombo.sounds)))
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
        return viewModel.soundsAlreadyFetched
    }

    private fun removeRecordingFromSoundPool(sound: Sound) {

        // TODO: 10-Apr-20 shouldn't we wait here for the eventbus callback??
        sendCommandToService(
                SoundPoolService.getCommand(
                        context,
                        UnloadSoundCommand(sound.soundPoolId)
                )
        )

        val newList = (viewModel.allSounds().value).filter { !(it.soundPoolId == sound.soundPoolId) }.toList()
        //newList.remove(Sound.withSoundPoolId(sound, sound.soundPoolId()))
        viewModel.addToSounds(newList)
    }

    fun addRecordingToSoundPool(sound: Sound) {

        // context?.startService(SoundPoolService.newIntent(context, LoadSoundEvent(sound.filePath, 1)))
        //
        //
        // // val soundId = soundPool!!.load(sound.filePath, 1)
        //
        // val newList = (viewModel.sounds().value)!!
        // val sound1 = Sound.withSoundPoolId(sound, soundId)
        // newList.add(sound1)
        // viewModel.addToSounds(newList)
    }

    fun scrollToBottom() {
        gridView!!.smoothScrollToPosition(gridArrayAdapter!!.count)
    }

    fun rewardUserByPlayingProSound() {

        //if not null
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
        Log.d(TAG, "updateViewModelWithStop: called with: soundPoolId: ${eventBusStop.soundPoolId}")
        viewModel.updateSingleSoundInViewModel(eventBusStop.soundPoolId, 0)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun serviceCallbackStopAll(eventBusStopAll: EventBusStopAll) {
        Log.d(TAG, "serviceCallbackStopAll: called")
        viewModel.updateViewModelWithPlayingSoundsFalse()

    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun serviceCallbackSoundPoolLoad(eventBusLoadedToSoundPool: EventBusLoadedToSoundPool) {
        Log.d(
                TAG,
                "updateViewModelSoundsLoadedToSoundPool: called with ${eventBusLoadedToSoundPool.soundPoolId}"
        )
        viewModel.loadedToSoundPool()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun updateViewModelWithLoad(eventBusLoad: EventBusLoad) {
        viewModel.addToSounds(eventBusLoad.sounds)
    }

    companion object {
        private val TAG = "SoundGridFragment"

        fun newInstance(): SoundGridFragment {
            return SoundGridFragment()
        }
    }
}
