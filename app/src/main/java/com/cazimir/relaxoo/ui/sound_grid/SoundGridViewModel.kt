package com.cazimir.relaxoo.ui.sound_grid

import android.util.Log
import androidx.lifecycle.*
import com.cazimir.relaxoo.eventbus.EventBusPlayingSounds
import com.cazimir.relaxoo.eventbus.EventBusTimer
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.repository.ISoundRepository
import com.cazimir.relaxoo.repository.SoundRepository
import com.cazimir.utilitieslibrary.observeOnceListOnEmpty
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SoundGridViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private var soundRepository: ISoundRepository

    companion object {
        private const val TAG = "SoundGridViewModel"
        private const val CURRENTLY_CLICKED_PRO_SOUND = "CurrentlyClickedProSound"
        private const val TEST_VALUE = "TestValue"
    }

    // TODO: 26-Apr-20 get rid of redundandt gettter methods for livedata - see example for soundsStorage
    private val _timerRunningFetched = MutableLiveData<Boolean>()
    private val _timerTextFetched = MutableLiveData<Boolean>()
    var currentlyClickedProSound: Sound? = savedStateHandle.get(CURRENTLY_CLICKED_PRO_SOUND)
    var soundsLoadedToSoundPool = MutableLiveData(0)
    private var _timerText = MutableLiveData<String>()
    private var currentSoundList: List<Sound> = emptyList()
    private var _soundsStorage: MutableLiveData<List<Sound>> = MutableLiveData()
    val soundsStorage: LiveData<List<Sound>> = _soundsStorage
    val _fetchFinished: MutableLiveData<Boolean> = MutableLiveData(false)
    private var soundsAlreadyFetched = false
    private var _timerRunning = MutableLiveData<Boolean>()

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        _soundsStorage.observeForever {
            currentSoundList = it
        }

        soundRepository = SoundRepository()
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    fun soundsAlreadyFetched(): Boolean {
        return soundsAlreadyFetched
    }

    fun timerText(): MutableLiveData<String> {
        return _timerText
    }

    fun timerRunning(): MutableLiveData<Boolean> {
        return _timerRunning
    }

    fun fetchSounds() {
        Log.d(TAG, "fetchSounds: called")
        soundRepository.getSounds().observeOnceListOnEmpty(Observer {
            Log.d(TAG, "fetchSounds: called with $it")
            triggerLiveDataEmit(it)
            soundsAlreadyFetched = true
            _fetchFinished.value = true
        })
    }

    fun playingSounds(): LiveData<ArrayList<Sound>> {
        return Transformations.map(_soundsStorage) { soundsList ->
            soundsList.filter { sound -> sound.playing } as ArrayList<Sound>
        }
    }

    fun addToSounds(sounds: List<Sound>) {
        triggerLiveDataEmit(sounds)
    }

    fun removeSingleSoundFromSounds(id: String) {
        val newList = currentSoundList.filter { sound ->
            sound.id != id
        }

        triggerLiveDataEmit(newList)
    }

    // TODO: 20-Apr-20 refactor this -> make generic update single anything, not only soundPoolId, streamId etc
    fun updatePlayingSound(sound: Sound) {
        val newList = currentSoundList.map {
            if (it.id == sound.id) {
                it.copy(soundPoolId = sound.soundPoolId, streamId = -1, playing = !it.playing)
            } else {
                it
            }
        }

        triggerLiveDataEmit(newList)
    }

    fun updateNameOnSound(sound: Sound, newName: String) {
        val newList = currentSoundList.map {
            if (it.id == sound.id) {
                it.copy(name = newName)
            } else {
                it
            }
        }

        triggerLiveDataEmit(newList)
    }

    fun updateViewModelWithPlayingSoundsFalse() {

        val newList = currentSoundList.map {
            it.copy(playing = false)
        }

        triggerLiveDataEmit(newList)
    }

    fun loadedToSoundPool(soundPoolId: Int) {
        soundsLoadedToSoundPool.value = soundsLoadedToSoundPool.value!! + 1
        // update soundStorage with loaded = true
        updateLoaded(soundPoolId)
    }

    private fun updateLoaded(soundPoolId: Int) {
        val newList = currentSoundList.map {
            if (it.soundPoolId == soundPoolId) {
                it.copy(loaded = true)
            } else {
                it
            }
        }

        triggerLiveDataEmit(newList)
    }

    fun updateVolume(sound: Sound, volume: Float) {
        val newList = currentSoundList.map {
            if (it.id == sound.id) {
                it.copy(volume = volume)
            } else {
                it
            }
        }

        triggerLiveDataEmit(newList)
    }

    fun setClickedProSound(sound: Sound) {
        Log.d(TAG, "setClickedProSound:  " + savedStateHandle.get(CURRENTLY_CLICKED_PRO_SOUND))
        Log.d(TAG, "init: savedStateHandle.getTestValue:  " + savedStateHandle.get(TEST_VALUE))
        savedStateHandle.set(CURRENTLY_CLICKED_PRO_SOUND, sound)
        savedStateHandle.set(TEST_VALUE, 5)

        currentlyClickedProSound = sound
    }

    fun timerTextLiveFetched(): LiveData<Boolean> {
        return _timerTextFetched
    }

    fun timerRunningFetched(): LiveData<Boolean> {
        return _timerRunningFetched
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun updatePlayingSoundsInViewModel(eventBusPlayingSounds: EventBusPlayingSounds) {
        Log.d(TAG, "updatePlayingSoundsInViewModel: called")
        eventBusPlayingSounds.playingSounds.observeForever { playingSounds: List<Sound> ->
            if (playingSounds.isNotEmpty()) {
                // filtered list for playing sounds received from service
                // update allSounds with playing status and streamId so you can control
                val newList = mutableListOf<Sound>()

                for (sound: Sound in currentSoundList) {
                    if (playingSounds.contains(sound)) {
                        newList.add(sound.copy(volume = playingSounds[playingSounds.indexOf(sound)].volume, streamId = playingSounds[playingSounds.indexOf(sound)].streamId, playing = true))
                    } else {
                        newList.add(sound)
                    }
                }

                triggerLiveDataEmit(newList)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun updateTimerLiveDataInViewModel(eventBusTimerStarted: EventBusTimer) {
        Log.d(TAG, "updateTimerLiveDataInViewModel: called")
        // start observing from fragment as soon as the live data 'fetched' has been triggered
        // hopefully when i am changin something on the service now it should trigger the observable and the need to send the eventbus 'EventBusTimer' event is no longer needed
        _timerRunning = eventBusTimerStarted._timerRunning
        _timerText = eventBusTimerStarted._timerText
        _timerTextFetched.value = true
        _timerRunningFetched.value = true
    }

    fun addSingleSound(sound: Sound) {
        val newList = currentSoundList as ArrayList<Sound>
        newList.add(sound)
        triggerLiveDataEmit(newList)
    }

    private fun triggerLiveDataEmit(newList: List<Sound>) {
        Log.d(TAG, "triggerLiveDataEmit: called")
        _soundsStorage.value = newList
    }


}
