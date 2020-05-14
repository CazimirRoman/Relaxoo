package com.cazimir.relaxoo.ui.sound_grid

import android.util.Log
import androidx.lifecycle.*
import com.cazimir.relaxoo.eventbus.EventBusAllSounds
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.repository.ISoundRepository
import com.cazimir.relaxoo.repository.SoundRepository
import com.cazimir.utilitieslibrary.observeOnceOnListNotEmpty
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
    // ---- Internal live data ------//
    private var _soundsStorage: MutableLiveData<List<Sound>> = MutableLiveData()
    private val _initialFetchFinished: MutableLiveData<List<Sound>> = MutableLiveData()
    private var _soundsLoadedToSoundPool = MutableLiveData(0)
    private var _timerRunning = MutableLiveData<Boolean>()

    var currentlyClickedProSound: Sound? = savedStateHandle.get(CURRENTLY_CLICKED_PRO_SOUND)
    var shouldLoadToSoundPool = false

    //--------- Public live data ----------//
    val soundsStorage: LiveData<List<Sound>> = _soundsStorage
    val initialFetchFinished: LiveData<List<Sound>> = _initialFetchFinished
    val soundsLoadedToSoundPool: LiveData<Int> = _soundsLoadedToSoundPool

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        soundRepository = SoundRepository()
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    fun fetchSounds() {
        Log.d(TAG, "fetchSounds: called")
        soundRepository.getSounds().observeOnceOnListNotEmpty(Observer {
            Log.d(TAG, "fetchSounds: called with $it")
            // fetch is finished -> start loading them to the soundpool
            shouldLoadToSoundPool = true
            _initialFetchFinished.value = it
        })
    }

    fun fetchSoundsOffline() {
        Log.d(TAG, "fetchSoundsOffline: called")
        soundRepository.getSoundsOffline().observeOnceOnListNotEmpty(Observer {
            Log.d(TAG, "fetchSounds: called with $it")
            // fetch is finished -> start loading them to the soundpool
            shouldLoadToSoundPool = true
            _initialFetchFinished.value = it
        })
    }

    fun playingSounds(): LiveData<ArrayList<Sound>> {
        return Transformations.map(_soundsStorage) { soundsList ->
            soundsList.filter { sound -> sound.playing } as ArrayList<Sound>
        }
    }

    fun updateNameOnSound(sound: Sound, newName: String) {
        val newList = _soundsStorage.value?.map {
            if (it.id == sound.id) {
                it.copy(name = newName)
            } else {
                it
            }
        }

        triggerLiveDataEmit(newList!!)
    }

    // just used to hide splash after 3 sounds loaded
    fun loadedToSoundPool() {
        _soundsLoadedToSoundPool.value = _soundsLoadedToSoundPool.value!! + 1
    }

    fun updateVolume(sound: Sound, volume: Float) {
        val newList = _soundsStorage.value?.map {
            if (it.id == sound.id) {
                it.copy(volume = volume)
            } else {
                it
            }
        }

        triggerLiveDataEmit(newList!!)
    }

    fun setClickedProSound(sound: Sound) {
        Log.d(TAG, "setClickedProSound:  " + savedStateHandle.get(CURRENTLY_CLICKED_PRO_SOUND))
        Log.d(TAG, "init: savedStateHandle.getTestValue:  " + savedStateHandle.get(TEST_VALUE))
        savedStateHandle.set(CURRENTLY_CLICKED_PRO_SOUND, sound)
        savedStateHandle.set(TEST_VALUE, 5)

        currentlyClickedProSound = sound
    }

    /*This is the place where the service sends back the updated list with updates parameters(loaded, playing etc)*/
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun serviceCallbackAllSounds(eventBusPlayingSounds: EventBusAllSounds) {
        eventBusPlayingSounds.allSoundsFromService.observeForever { allSounds: List<Sound> ->
            Log.d(TAG, "serviceCallbackAllSounds: with allSounds: $allSounds")
            _soundsStorage.value = allSounds
        }
    }

    // this emit on the livedata is used to update the Recyclerview adapter with the new data
    private fun triggerLiveDataEmit(newList: List<Sound>) {
        Log.d(TAG, "triggerLiveDataEmit: called")
        _soundsStorage.value = newList
    }
}
