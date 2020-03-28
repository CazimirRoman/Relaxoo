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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.adapter.GridAdapter
import com.cazimir.relaxoo.dialog.custom.BottomCustomDeleteFragment
import com.cazimir.relaxoo.dialog.custom.CustomBottomCallback
import com.cazimir.relaxoo.eventbus.EventBusLoad
import com.cazimir.relaxoo.eventbus.EventBusLoadedToSoundPool
import com.cazimir.relaxoo.eventbus.EventBusPlay
import com.cazimir.relaxoo.eventbus.EventBusStop
import com.cazimir.relaxoo.model.SavedCombo
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.service.SoundPoolService
import com.cazimir.relaxoo.service.events.LoadSoundsCommand
import com.cazimir.relaxoo.service.events.PlayCommand
import com.cazimir.relaxoo.service.events.ShowNotificationCommand
import com.cazimir.relaxoo.service.events.StopCommand
import com.cazimir.relaxoo.service.events.UnloadSoundCommand
import com.cazimir.relaxoo.service.events.VolumeCommand
import kotlinx.android.synthetic.main.sound_list_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.ArrayList
import java.util.HashMap
import java.util.Random
import java.util.concurrent.CopyOnWriteArrayList

class SoundGridFragment() : Fragment() {

    private var gridArrayAdapter: GridAdapter? = null
    private lateinit var viewModel: SoundGridViewModel
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

    private fun playStopSound(
        soundPoolId: Int,
        playing: Boolean,
        streamId: Int
    ) {
        if (playing) {
            Log.d(TAG, "stopping sound")
            sendCommandToService(
                SoundPoolService.newIntent(
                    context,
                    StopCommand(streamId, soundPoolId)
                )
            )
        } else {
            Log.d(TAG, "playing sound")
            sendCommandToService(
                SoundPoolService.newIntent(
                    context,
                    PlayCommand(soundPoolId, 0.5f, 0.5f, 0, -1, 1f)
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
        activityCallback = context as OnActivityCallback
    }

    private fun stopAllSounds(): Boolean {
        Log.d(TAG, "stopAllSounds: called")
        val list: List<Sound> =
            CopyOnWriteArrayList(playingSounds)
        // TODO: 22.12.2019 try to remove some of the for loops, use guava or something
        for (sound: Sound in list) {
            sendCommandToService(
                SoundPoolService.newIntent(
                    context,
                    StopCommand(sound.streamId(), sound.soundPoolId())
                )
            )
            viewModel.updateSoundList(sound.soundPoolId(), 0)
        }
        return false
    }

    val playingSounds: List<Sound>?
        get() = viewModel.playingSounds().value

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        viewModel = ViewModelProvider(this).get(SoundGridViewModel::class.java)

        setListenersForButtons()
        // region Observers
// change icon to unmute
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
        // listen if at least one sound is playing to show notification in actionbar to user
        viewModel
            .isAtLeastOneSoundPlaying
            .observe(
                viewLifecycleOwner,
                object : Observer<Boolean> {
                    override fun onChanged(playing: Boolean) {
                        if (playing) {


                            // call show notification on service

                            //activityCallback.showNotification()
                        } else {
                            //activityCallback.hideNotification()
                        }
                    }
                })
        // listen for changes to the sound lists live data object to set the adapter for the gridview
// along with the callback methods (clicked & volume changed)
        viewModel
            .sounds()
            .observe( // TODO: 19.12.2019 move in a separate file or inner class
                viewLifecycleOwner,
                Observer { sounds: ArrayList<Sound> ->
                    Log.d(
                        TAG,
                        "Sound list changed: " + sounds
                    )
                    gridArrayAdapter = GridAdapter(
                        getContext(),
                        sounds,
                        object : OnSoundClickListener {
                            override fun clicked(sound: Sound) {
                                if (sound.isPro() && !sound.isPlaying()) {
                                    viewModel.currentlyClickedProSound = sound
                                    activityCallback.showBottomDialogForPro()
                                } else {
                                    playStopSound(
                                        sound.soundPoolId(),
                                        sound.isPlaying(),
                                        sound.streamId()
                                    )
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

                                val volumeCommand = SoundPoolService.newIntent(
                                    context, VolumeCommand(
                                        sound.streamId(),
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
                    gridView!!.setAdapter(gridArrayAdapter)
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
                viewLifecycleOwner, object : Observer<List<Sound?>> {
                    override fun onChanged(playingList: List<Sound?>) {
                        if (playingList.isEmpty()) {
                            play_button.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
                            set_timer_button.setImageDrawable(resources.getDrawable(R.drawable.ic_timer_on))
                            if (viewModel.countDownTimer() != null) {
                                viewModel.timerEnabled().setValue(false)
                            }
                            sendCommandToService(
                                SoundPoolService.newIntent(
                                    context,
                                    ShowNotificationCommand(playingList.size)
                                )
                            )
                        } else {
                            play_button.setImageDrawable(resources.getDrawable(R.drawable.ic_stop))
                            // show notification
                            sendCommandToService(
                                SoundPoolService.newIntent(
                                    context,
                                    ShowNotificationCommand(playingList.size)
                                )
                            )
                        }
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
                object : Observer<Int> {
                    override fun onChanged(soundsAdded: Int) {
                        if (viewModel.sounds().value!!.size != 0) { // hide splash if viewmodel sound livedata size equals the number of sounds added to the soundpool
                            if (soundsAdded == viewModel.sounds().value!!.size) {
                                activityCallback.hideSplash()
                            }
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
            if (sound.soundPoolId() == 0) {
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
                        for (sound: Sound in playingSounds!!) {

                            sendCommandToService(
                                SoundPoolService.newIntent(
                                    context,
                                    VolumeCommand(sound.streamId(), 0f, 0f)
                                )
                            )
                        }
                    } else {
                        for (sound: Sound in playingSounds!!) {

                            sendCommandToService(
                                SoundPoolService.newIntent(
                                    context,
                                    VolumeCommand(sound.streamId(), sound.volume(), sound.volume())
                                )
                            )
                        }
                    }
                    viewModel.updateMuteLiveData(muted)
                }
            })
        random_button.setOnClickListener(
            object : View.OnClickListener {
                override fun onClick(v: View) {
                    val totalNumberOfSounds = Random()
                    // total number of available sounds can be found in viewmodel in sounds variable
                    val value: List<Sound> = (viewModel.sounds().value)!!
                    for (i in value.indices) {
                        Log.d(
                            TAG,
                            "randomClick for loop called: $i"
                        )
                        val sound = value[totalNumberOfSounds.nextInt(value.size)]
                        if (!sound.isPro) {
                            playStopSound(sound.soundPoolId(), sound.isPlaying, sound.streamId())
                        }
                    }
                }
            })
        play_button.setOnClickListener(
            object : View.OnClickListener {
                override fun onClick(v: View) {
                    if ((playingSounds != null &&
                                    playingSounds!!.size != 0)
                    ) { // stop all sounds and show play button again
                        stopAllSounds()
                    }
                }
            })
        save_fav_button.setOnClickListener(
            object : View.OnClickListener {
                override fun onClick(v: View) {
                    val atLeastOneIsPlaying =
                        viewModel.isAtLeastOneSoundPlaying.value
                    if (atLeastOneIsPlaying != null && atLeastOneIsPlaying) {
                        activityCallback.showAddToFavoritesDialog(
                            getSoundParameters(playingSounds)
                        )
                    } else {
                        activityCallback.showToast("You must play at least one sound")
                    }
                }
            })
        set_timer_button.setOnClickListener(
            object : View.OnClickListener {
                override fun onClick(v: View) {
                    val atLeastOneIsPlaying =
                        viewModel.isAtLeastOneSoundPlaying.value
                    if (atLeastOneIsPlaying != null && atLeastOneIsPlaying) {
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

    private fun getSoundParameters(sounds: List<Sound>?): HashMap<Int, Int> {
        val hashMap = HashMap<Int, Int>()
        for (sound: Sound in sounds!!) {
            hashMap[sound.soundPoolId()] = sound.streamId()
        }
        return hashMap
    }

    // endregion
    private fun loadListToSoundPool(sounds: ArrayList<Sound>) {
        Log.d(TAG, "loadToSoundPool: called")

        sendCommandToService(
            SoundPoolService.newIntent(
                context,
                LoadSoundsCommand(sounds)
            )
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun updateViewModelWithLoad(eventBusLoad: EventBusLoad) {
        viewModel.addToSounds(eventBusLoad.sounds)
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
        val areSoundsStillPlaying = stopAllSounds()
        for (entry: Map.Entry<Int, Int> in savedCombo.soundPoolParameters.entries) {
            playStopSound(entry.key, areSoundsStillPlaying, entry.value)
        }
    }

    fun fetchSounds() {
        viewModel.fetchSounds()
    }

    override fun onStart() {
        Log.d(TAG, "onStart() in SoundGridFragment called")
        super.onStart()
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

        sendCommandToService(
            SoundPoolService.newIntent(
                context,
                UnloadSoundCommand(sound.soundPoolId())
            )
        )

        // soundPool!!.unload(sound.soundPoolId())

        val newList = (viewModel.sounds().value)!!
        newList.remove(Sound.withSoundPoolId(sound, sound.soundPoolId()))
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
        val currentlyClickedProSound = viewModel.currentlyClickedProSound
        playStopSound(
            currentlyClickedProSound!!.soundPoolId(),
            currentlyClickedProSound.isPlaying,
            currentlyClickedProSound.streamId()
        )
    }

    fun sendCommandToService(intent: Intent) {
        context?.startService(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun serviceCallbackPlay(eventBusPlay: EventBusPlay) {
        Log.d(
            TAG,
            "updateViewModelWithPlay: called with: soundPoolId: ${eventBusPlay.soundPoolId} and streamId: ${eventBusPlay.streamId}"
        )
        viewModel.updateSoundList(eventBusPlay.soundPoolId, eventBusPlay.streamId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun serviceCallbackStop(eventBusStop: EventBusStop) {
        Log.d(TAG, "updateViewModelWithStop: called with: soundPoolId: ${eventBusStop.soundPoolId}")
        viewModel.updateSoundList(eventBusStop.soundPoolId, 0)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun serviceCallbackSoundPoolLoad(eventBusLoadedToSoundPool: EventBusLoadedToSoundPool) {
        Log.d(
            TAG,
            "updateViewModelSoundsLoadedToSoundPool: called with ${eventBusLoadedToSoundPool.soundPoolId}"
        )
        viewModel.loadedToSoundPool()
    }

    companion object {
        private val TAG = "SoundGridFragment"

        fun newInstance(): SoundGridFragment {
            return SoundGridFragment()
        }
    }
}
