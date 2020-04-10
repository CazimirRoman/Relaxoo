package com.cazimir.relaxoo.ui.sound_grid

import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.cazimir.relaxoo.dialog.timer.TimerDialog
import com.cazimir.relaxoo.eventbus.EventBusPlayingSounds
import com.cazimir.relaxoo.model.ListOfSavedCustom
import com.cazimir.relaxoo.model.PlayingSound
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.repository.ModelPreferencesManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.shopify.livedataktx.LiveDataKtx
import com.shopify.livedataktx.MutableLiveDataKtx
import com.shopify.livedataktx.map
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class SoundGridViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val TAG = "SoundGridViewModel"
        private const val CURRENTLY_CLICKED_PRO_SOUND = "CurrentlyClickedProSound"
        private const val TEST_VALUE = "TestValue"
    }

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }


    var currentlyClickedProSound: Sound? = savedStateHandle.get(CURRENTLY_CLICKED_PRO_SOUND)
    var soundsLoadedToSoundPool = MutableLiveData(0)

    var _timerText = MutableLiveData<String>()
    var _timerFinished = MutableLiveData<Boolean>()
    private var allSounds = ArrayList<Sound>()
    private var _allSounds: MutableLiveDataKtx<ArrayList<Sound>> = MutableLiveDataKtx()

    val _fetchFinished: MutableLiveData<Boolean> = MutableLiveData(false)


    /**
     * used to show notification in MainActivity to let user know that a sound is playing
     */
    private val _mutedLiveData = MutableLiveData<Boolean>()
    var soundsAlreadyFetched = false
        private set
    private val _timerEnabled = MutableLiveData<Boolean>()
    private var countDownTimer: CountDownTimer? = null

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    fun timerFinished(): MutableLiveData<Boolean> {
        return _timerFinished
    }

    fun timerText(): MutableLiveData<String> {
        return _timerText
    }

    fun timerEnabled(): MutableLiveData<Boolean> {
        return _timerEnabled
    }

    fun mutedLiveData(): MutableLiveData<Boolean> {
        return _mutedLiveData
    }

    fun fetchSounds() { // TODO: 01-Mar-20 Move to repository class so you can test
        Log.d(TAG, "fetchSounds: called")
        val soundsInFirebase = ArrayList<Sound>()
        // 1. check the Firebase DB for sounds
        val database = FirebaseDatabase.getInstance()
        val soundsRef = database.getReference("sounds")
        // check database for sounds
        // Read from the database
        soundsRef.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (soundsSnapshot in dataSnapshot.children) {
                            val sound = soundsSnapshot.getValue(Sound::class.java)
                            if (sound != null) {
                                soundsInFirebase.add(0, sound)
                            }
                        }
                        if (soundsInFirebase.size > 0) {
                            getAssetsFromFirebaseStorage(soundsInFirebase)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) { // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException())
                    }
                })
    }

    private fun getAssetsFromFirebaseStorage(sounds: ArrayList<Sound>) {
        allSounds.clear()
        // check if files already downloaded locally
        val soundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/sounds")
        val logosFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/logos")
        if (!soundsFolder.exists()) {
            soundsFolder.mkdirs()
        }
        // check locally to see how many files there are
        val soundsDirectory = File(soundsFolder.absolutePath)
        val files = soundsDirectory.listFiles()
        // there are some sounds missing locally
        if (files == null || files.size < sounds.size) {
            Log.d(TAG, "getAssetsFromFirebaseStorage: loading assets from firebase")
            // get sounds
            for (sound in sounds) {
                val soundReference = FirebaseStorage.getInstance().reference.child("sounds").child(sound.filePath)
                val imageReference = FirebaseStorage.getInstance().reference.child("logos").child(sound.logoPath)
                if (!logosFolder.exists()) {
                    logosFolder.mkdirs()
                }
                val soundFile = File(soundsFolder, sound.filePath)
                val logoFile = File(logosFolder, sound.logoPath)
                // download sound from Firebase
                soundReference
                        .getFile(soundFile)
                        .addOnSuccessListener { soundSnapshot: FileDownloadTask.TaskSnapshot? ->
                            Log.d(TAG, "onSuccess: called")
                            // now download the image
                            imageReference
                                    .getFile(logoFile)
                                    .addOnSuccessListener { imageSnapshot: FileDownloadTask.TaskSnapshot? ->
                                        Log.d(TAG, "onSuccess: called")
                                        val fetchedSound = Sound.SoundBuilder.aSound()
                                                .withId(sound.id)
                                                .withName(sound.name)
                                                .withLogo(logoFile.path)
                                                .withPro(sound.isPro)
                                                .withFilePath(soundFile.path)
                                                .build()
                                        allSounds.addAll(Arrays.asList(fetchedSound))
                                        // addCustomSoundsAsWell();

                                        val customSounds = ModelPreferencesManager.get<ListOfSavedCustom>("PINNED_RECORDINGS")

                                        customSounds?.savedCustomList?.let { this.allSounds.addAll(it) }

                                        // TODO: 14-Mar-20 Add custom sounds here
                                        if (allSounds.size == sounds.size) {
                                            nextSoundLiveData()
                                            soundsAlreadyFetched = true
                                        }
                                    }
                                    .addOnFailureListener { e: Exception -> Log.d(TAG, "onFailure: " + e.message) }
                        }
                        .addOnFailureListener { e: Exception -> Log.d(TAG, "onFailure: " + e.message) }
            }
        } else {
            Log.d(TAG, "getAssetsFromFirebaseStorage: loading assets from local storage")
            val logosDirectory = File(logosFolder.absolutePath)
            for (i in sounds.indices) {
                val localSound = Sound.SoundBuilder.aSound()
                        .withId(sounds[i].id)
                        .withName(sounds[i].name)
                        .withLogo(logosDirectory.toString() + "/" + sounds[i].logoPath)
                        .withPro(sounds[i].isPro)
                        .withFilePath(soundsDirectory.toString() + "/" + sounds[i].filePath)
                        .build()
                allSounds.add(localSound)
            }

            val customSounds = ModelPreferencesManager.get<ListOfSavedCustom>("PINNED_RECORDINGS")
            customSounds?.savedCustomList?.let { this.allSounds.addAll(it) }
            nextSoundLiveData()
            soundsAlreadyFetched = true
        }


        // send a request to service to get playing sounds
        _fetchFinished.postValue(true)
    }

    private fun updatePlayingFromServiceIfRunning() {

    }

    fun allSounds(): LiveDataKtx<ArrayList<Sound>> {
        return _allSounds
    }

    fun playingSounds(): LiveDataKtx<ArrayList<Sound>> {
        return _allSounds.map { soundsList -> soundsList.filter { sound -> sound.isPlaying } as ArrayList<Sound> }
    }

    private fun nextSoundLiveData() {
        Log.d(TAG, "refreshSoundLiveData: called: $allSounds")
        _allSounds.value = allSounds
    }

    fun addToSounds(sounds: ArrayList<Sound>) {
        allSounds = sounds
        nextSoundLiveData()
    }

    fun updateSingleSoundInViewModel(soundPoolId: Int, streamId: Int) {

        for (sound in allSounds) {
            if (sound.soundPoolId() == soundPoolId) {
                allSounds[allSounds.indexOf(sound)] = Sound.SoundBuilder.aSound()
                        .withId(sound.id)
                        .withSoundPoolId(soundPoolId)
                        .withStreamId(streamId)
                        .withName(sound.name)
                        .withLogo(sound.logoPath)
                        .withFilePath(sound.filePath)
                        .withPlaying(!sound.isPlaying)
                        .withVolume(sound.volume())
                        .withPro(sound.isPro)
                        .withCustom(sound.isCustom)
                        .build()
                break
            }
        }

        nextSoundLiveData()
    }

    fun updateViewModelWithPlayingSoundsFalse() {
        val newList = mutableListOf<Sound>()
        allSounds.forEach { sound ->
            run {
                if (sound.isPlaying) {
                    newList.add(Sound.withPlaying(sound))
                } else {
                    newList.add(sound)
                }
            }
        }

        allSounds = newList as ArrayList<Sound>

        nextSoundLiveData()
    }

    fun updateMuteLiveData(muted: Boolean) {
        _mutedLiveData.value = muted
    }

    fun loadedToSoundPool() {
        soundsLoadedToSoundPool.value = soundsLoadedToSoundPool.value!! + 1
    }

    fun updateVolume(sound: Sound?, volume: Float?) {
        allSounds[allSounds.indexOf(sound)] = Sound.withVolume(sound, volume!!)
        _allSounds.value = allSounds
    }

    fun countDownTimer(): CountDownTimer? {
        return countDownTimer
    }

    fun setCountDownTimer(minutes: Int) {
        countDownTimer = object : CountDownTimer(TimeUnit.MINUTES.toMillis(minutes.toLong()), 1000) {
            override fun onTick(millisUntilFinished: Long) { // updateLiveDataHere() observe from Fragment
                timerText().value = String.format("Sound%s will stop in " +
                        TimerDialog.getCountTimeByLong(millisUntilFinished),
                        if (playingSounds().value.size > 1) "s" else "")
            }

            override fun onFinish() { // live data observe timer finished
                timerFinished().value = true
            }
        }.start()
        timerFinished().value = false
    }

    fun setClickedProSound(sound: Sound) {
        Log.d(TAG, "setClickedProSound:  " + savedStateHandle.get(CURRENTLY_CLICKED_PRO_SOUND))
        Log.d(TAG, "init: savedStateHandle.getTestValue:  " + savedStateHandle.get(TEST_VALUE))
        savedStateHandle.set(CURRENTLY_CLICKED_PRO_SOUND, sound)
        savedStateHandle.set(TEST_VALUE, 5)

        currentlyClickedProSound = sound
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun updatePlayingSoundsInViewModel(eventBusPlayingSounds: EventBusPlayingSounds) {

        Log.d(TAG, "updatePlayingSoundsInViewModel: called")

        eventBusPlayingSounds.playingSounds.observeForever { playingSounds ->
            if (playingSounds.isNotEmpty()) {
                val filteredList = allSounds.filterBasedOnId(playingSounds)

                //update allSounds with playing status and streamId so you can control

                for (sound: Sound in filteredList) {

                    val index = playingSounds.indexOfFirst { it.id == sound.id }

                    val fetchedSound = Sound.SoundBuilder.aSound()
                            .withId(sound.id)
                            .withSoundPoolId(sound.soundPoolId())
                            .withName(sound.name)
                            .withLogo(sound.logoPath)
                            .withPro(sound.isPro)
                            .withCustom(sound.isCustom)
                            .withVolume(playingSounds[index].volume!!)
                            .withFilePath(sound.filePath)
                            .withPlaying(true)
                            .withStreamId(playingSounds[index].streamId)
                            .build()


                    allSounds = allSounds.replace(sound, fetchedSound) as ArrayList<Sound>
                }

                nextSoundLiveData()
            }

        }
    }

    private fun List<Sound>.filterBasedOnId(soundPoolListFromService: List<PlayingSound>) = filter { m -> soundPoolListFromService.any { it.id == m.id } }

    // replace element in an array (any iterable for that matter)
    fun <E> Iterable<E>.replace(old: E, new: E) = map { if (it == old) new else it }
}
