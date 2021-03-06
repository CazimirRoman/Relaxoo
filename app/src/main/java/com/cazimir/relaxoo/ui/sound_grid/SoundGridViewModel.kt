package com.cazimir.relaxoo.ui.sound_grid

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
        private const val CURRENTLY_CLICKED_PRO_SOUND = "CurrentlyClickedProSound"
        private const val TEST_VALUE = "TestValue"
    }

    // TODO: 26-Apr-20 get rid of redundandt gettter methods for livedata - see example for soundsStorage
    // ---- Internal live data ------//
    private var _soundsStorage: MutableLiveData<List<Sound>> = MutableLiveData()
    private val _initialFetchFinished: MutableLiveData<List<Sound>> = MutableLiveData()
    private var _soundsLoadedToSoundPool = MutableLiveData(0)

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
        soundRepository.getSounds().observeOnceOnListNotEmpty(Observer {
            // fetch is finished -> start loading them to the soundpool
            shouldLoadToSoundPool = true
            _initialFetchFinished.value = it
        })
    }

    fun fetchSoundsOffline() {
        soundRepository.getSoundsOffline().observeOnceOnListNotEmpty(Observer {
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
                it.copy(name = newName, id = "$newName.wav", filePath = "/storage/emulated/0/Relaxoo/own_sounds/$newName.wav")
            } else {
                it
            }
        }

        triggerLiveDataEmit(newList!!)
    }

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
        savedStateHandle.set(CURRENTLY_CLICKED_PRO_SOUND, sound)
        savedStateHandle.set(TEST_VALUE, 5)

        currentlyClickedProSound = sound
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun serviceCallbackAllSounds(eventBusPlayingSounds: EventBusAllSounds) {
        eventBusPlayingSounds.allSoundsFromService.observeForever { allSounds: List<Sound> ->
            _soundsStorage.value = allSounds
        }
    }

    private fun triggerLiveDataEmit(newList: List<Sound>) {
        _soundsStorage.value = newList
    }
}
