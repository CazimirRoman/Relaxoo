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
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
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
import com.cazimir.relaxoo.service.SoundService
import com.cazimir.relaxoo.service.SoundService.Companion.SOUND_POOL_ACTION
import com.cazimir.relaxoo.service.commands.*
import com.cazimir.relaxoo.shared.SharedViewModel
import com.cazimir.utilitieslibrary.SharedPreferencesUtil.loadFromSharedPreferences
import com.cazimir.utilitieslibrary.SharedPreferencesUtil.saveToSharedPreferences
import com.cazimir.utilitieslibrary.observeOnceOnListEmptyWithOwner
import com.cazimir.utilitieslibrary.observeOnceOnListNotEmptyWithOwner
import com.google.android.material.snackbar.Snackbar
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

        sharedViewModel = ViewModelProvider(activity as ViewModelStoreOwner).get(SharedViewModel::class.java)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        // used to pass the savedstate to ViewModel when application is beeing destroyed by the Android OS
        val factory = SavedStateViewModelFactory(activity!!.application, activity as FragmentActivity)

        viewModel = ViewModelProvider(this, factory).get(SoundGridViewModel::class.java)

        setListenersForButtons()

        // region Observers

        viewModel.initialFetchFinished.observeOnceOnListNotEmptyWithOwner(viewLifecycleOwner, Observer { list: List<Sound> ->
            Log.d(TAG, "_fetchFinished.observeOnce called with: $list ")
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

                    Log.d(TAG, "observer for playingSounds called with: $playingList")

                    if (playingList.isEmpty()) {
                        play_button.setImageDrawable(resources.getDrawable(R.drawable.ic_play))
                        play_button.tag = getString(R.string.play_button_tag)
                        set_timer_button.setImageDrawable(resources.getDrawable(R.drawable.ic_timer_off))
                    } else {
                        play_button.setImageDrawable(resources.getDrawable(R.drawable.ic_stop))
                        play_button.tag = getString(R.string.stop_button_tag)

                    }
                })

        viewModel.soundsLoadedToSoundPool
                .observe(
                        viewLifecycleOwner,
                        Observer { soundsAdded ->
                            Log.d(TAG, "viewModel.soundsLoadedToSoundPool: called with: $soundsAdded")
                            if (viewModel.soundsStorage.value?.size != 0) {
                                if (soundsAdded == 3) {
                                    activityCallback.hideSplashScreen()
                                }
                            }
                        })

        // first find out when the timer text observable from the service has arrived and after that start observing the timerText
        viewModel
                .timerTextFetched
                .observe(viewLifecycleOwner, Observer {
                    viewModel
                            .timerText
                            .observe(
                                    viewLifecycleOwner,
                                    Observer { timerText: String ->
                                        setTimerText(timerText)
                                    }
                            )
                }

                )

        // TODO: 02-May-20 Why do we need to observe another live data aobject again after observing the first one??

        viewModel
                .timerRunningFetched
                .observe(
                        viewLifecycleOwner,
                        Observer {
                            Log.d(TAG, "timerRunningFetched: called")
                            viewModel.timerRunning.observe(viewLifecycleOwner, Observer { running ->
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

    private fun updateOrSetupAdapter(allSounds: List<Sound>) {
        if (soundsAdapter == null) {
            soundsAdapter = GridRecyclerViewAdapter(context!!,
                    allSounds as ArrayList<Sound>,
                    object : OnSoundClickListener {
                        override fun clicked(sound: Sound) {
                            if (sound.pro && !sound.playing) {
                                viewModel.setClickedProSound(sound)
                                activityCallback.showBottomDialogForPro()
                            } else if (!sound.loaded) {
                                activityCallback.showMessageToUser(getString(R.string.sound_loading), Snackbar.LENGTH_SHORT)
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

        }
//        } else {
//            // TODO: 21-Apr-20 this is called so many times!!
//                Log.d(TAG, "gridArrayAdapter!!.refreshList called with: $allSounds ")
//                soundsAdapter!!.refreshList(allSounds)
//        }
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
        mute_button.setOnClickListener {
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
            stopAllSounds()

            /*observe playing sounds once to make sure that an empty list is delivered and the observe once and remove observer*/
            viewModel.playingSounds().observeOnceOnListEmptyWithOwner(viewLifecycleOwner, Observer {
                // total number of available sounds can be found in viewmodel in sounds variable
                val listAllSounds: List<Sound> = (viewModel.soundsStorage.value)!!

                val processed = mutableListOf<Sound>()

                while (processed.size < calculateLimit(listAllSounds)) {
                    Log.d(TAG, "while called: with processed size: ${processed.size}")
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
            val atLeastOneIsPlaying = currentlyPlayingSounds.size > 0
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

    fun triggerCombo(savedCombo: SavedCombo, boughtPro: Boolean?) {
        sendCommandToService(SoundService.getCommand(context, TriggerComboCommand(savedCombo.sounds, boughtPro ?: false)))
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

    fun shouldLoadToSoundpool(): Boolean {
        return viewModel.shouldLoadToSoundPool
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

        Log.d(TAG, "unload called from service. Saving new list to SP: $newList")

        val newObject = ListOfSavedCustom(newList)
        saveToSharedPreferences(newObject, MainActivity.PINNED_RECORDINGS)
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
                mute_button.setImageDrawable(resources.getDrawable(R.drawable.ic_mute_off))
                // used for espresso
                mute_button.tag = getString(R.string.mute_off_tag)
            }
        })
    }


    fun removeCustomSoundFromDashboardIfThere(recording: Recording) {
        Log.d(TAG, "removeCustomSoundFromDashboardIfThere: called with recording id: ${recording.id}")
        val filtered = mutableListOf<Sound>()
        viewModel.soundsStorage.value?.filterTo(filtered, predicate = {
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
        viewModel.soundsStorage.value?.filterTo(filtered, predicate = {
            it.id == recording.id
        })

        if (filtered.size != 0) {
            viewModel.updateNameOnSound(filtered.first(), newName)
        }

        val newList = mutableListOf<Sound>()
        val pinnedRecordings = loadFromSharedPreferences<ListOfSavedCustom>(MainActivity.PINNED_RECORDINGS)
        val list = pinnedRecordings?.savedCustomList ?: mutableListOf()

        list.mapTo(newList, {
            if (it.id == recording.id) {
                it.copy(name = newName, id = "$newName.wav", filePath = "/storage/emulated/0/Relaxoo/own_sounds/$newName.wav")
            } else {
                it
            }
        })

        val newObject = ListOfSavedCustom(newList)
        saveToSharedPreferences(newObject, MainActivity.PINNED_RECORDINGS)

    }

    companion object {
        private val TAG = "SoundGridFragment"
        private val MAX_RANDOM_SOUNDS = 3

        fun newInstance(): SoundGridFragment {
            return SoundGridFragment()
        }
    }
}
