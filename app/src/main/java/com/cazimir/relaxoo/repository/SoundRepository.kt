package com.cazimir.relaxoo.repository

import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cazimir.relaxoo.EspressoIdlingResource
import com.cazimir.relaxoo.model.ListOfSavedCustom
import com.cazimir.relaxoo.model.ListOfSounds
import com.cazimir.relaxoo.model.Sound
import com.cazimir.utilitieslibrary.SharedPreferencesUtil
import com.cazimir.utilitieslibrary.SharedPreferencesUtil.loadFromSharedPreferences
import com.cazimir.utilitieslibrary.observeOnceWithTrueNoLifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class SoundRepository : ISoundRepository {

    companion object {
        private const val TAG = "SoundRepository"
        const val PREF_ALL_SOUNDS = "PREF_ALL_SOUNDS"
    }

    private val database = FirebaseDatabase.getInstance()
    private val soundsRef = database.getReference("sounds")
    private val _soundsStorageRepo = MutableLiveData<List<Sound>>()
    private var mAuth = FirebaseAuth.getInstance()
    private val authenticationComplete: MutableLiveData<Boolean> = MutableLiveData()

    private val soundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/sounds")
    private val logosFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/logos")

    private val soundsDirectory = File(soundsFolder.absolutePath)
    private val logosDirectory = File(logosFolder.absolutePath)

    init {
        val user: FirebaseUser? = mAuth.currentUser
        if (user == null) {
            signInAnonymously()
        } else {
            authenticationComplete.value = true
        }
    }

    private fun signInAnonymously() {
        mAuth.signInAnonymously().addOnCompleteListener {
            if (it.isSuccessful) {
                authenticationComplete.value = true
            }
        }
    }

    override fun getSounds(): MutableLiveData<List<Sound>> {
        authenticationComplete.observeOnceWithTrueNoLifecycleOwner(Observer {
            val soundsInFirebase = mutableListOf<Sound>()
            EspressoIdlingResource.increment()
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
                                //we need to see the regular sounds on top and the pro sounds on the bottom

                                val newList = mutableListOf<Sound>()

                                val reversed = soundsInFirebase.reversed()

                                reversed.forEach {
                                    if (it.new) {
                                        newList.add(0, it)
                                    } else {
                                        newList.add(it)
                                    }
                                }

                                getSoundAndLogo(newList)
                                //save to shared preferences in order to load offline if no internet available
                                SharedPreferencesUtil.saveToSharedPreferences(ListOfSounds(newList as List<Sound>), PREF_ALL_SOUNDS)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) { // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException())
                        }
                    })
        })

        return _soundsStorageRepo
    }

    override fun getSoundsOffline(): LiveData<List<Sound>> {

        val newList = mutableListOf<Sound>()

        val localFiles = soundsFolder.listFiles()

        //get list of sounds from shared preferences
        val allSounds = loadFromSharedPreferences<ListOfSounds>(PREF_ALL_SOUNDS)?.sounds

        //check if local files are there
        localFiles?.let { file ->
            //check if sounds fetched from local storage
            allSounds?.let { sounds ->
                for (sound in sounds) {
                    // missing information about pro. need to save this to shared preferences on first download
//                    val localSound = Sound(id = allSounds.find { it.name == file.name }?.id, streamId = -1, soundPoolId = -1, name = file.name, pro = allSounds.find { it.name == file.name }?.pro)

                    val localSound = sound.copy(logoPath = logosDirectory.toString() + "/" + sound.logoPath, filePath = soundsDirectory.toString() + "/" + sound.filePath)
                    newList.add(localSound)
                }
            }
        }

        //adding custom sounds as well
        val customSounds = loadFromSharedPreferences<ListOfSavedCustom>("PINNED_RECORDINGS")
        customSounds?.savedCustomList?.let { newList.addAll(it) }
        _soundsStorageRepo.value = newList
        return _soundsStorageRepo
    }

    private fun getSoundAndLogo(soundsInDatabase: List<Sound>) {

        if (!soundsFolder.exists()) {
            soundsFolder.mkdirs()
        }

        val soundsDirectory = File(soundsFolder.absolutePath)
        val files = soundsDirectory.listFiles()

        when {
            // no files downloaded locally
            files == null -> {
                getAllSoundsFromStorage(soundsInDatabase)
            }

            files.size < soundsInDatabase.size -> {
                //find the new sound (transform to set and then filter based on set contains criteria)
                val fileNames = files.map { it.name }.toSet()
                val newSoundList = soundsInDatabase.filter { !fileNames.contains(it.filePath) }
                getNewSoundsFromStorage(newSoundList, soundsInDatabase)
                //fetch and on callback and after loop finishes call loadLocalSounds
            }
            files.size == soundsInDatabase.size -> {
                // no new sounds in firebase, load local sounds
                loadLocalSounds(soundsInDatabase)
            }
        }
    }

    private fun loadLocalSounds(soundsInFirebase: List<Sound>) {
        val newList = mutableListOf<Sound>()
        for (sound in soundsInFirebase) {
            val localSound = sound.copy(logoPath = logosDirectory.toString() + "/" + sound.logoPath, filePath = soundsDirectory.toString() + "/" + sound.filePath)
            newList.add(localSound)
        }

        //adding custom sounds as well
        val customSounds = loadFromSharedPreferences<ListOfSavedCustom>("PINNED_RECORDINGS")
        customSounds?.savedCustomList?.let { newList.addAll(it) }
        _soundsStorageRepo.value = newList
    }

    private fun getAllSoundsFromStorage(list: List<Sound>) {

        for (sound in list) {
            val soundReference = FirebaseStorage.getInstance().reference.child("sounds").child(sound.filePath)
            val imageReference = FirebaseStorage.getInstance().reference.child("logos").child(sound.logoPath)

            if (!logosFolder.exists()) {
                logosFolder.mkdirs()
            }

            // create the files
            val soundFile = File(soundsFolder, sound.filePath)
            val logoFile = File(logosFolder, sound.logoPath)

            // download sound from Firebase
            soundReference
                    .getFile(soundFile)
                    .addOnSuccessListener { soundSnapshot: FileDownloadTask.TaskSnapshot? ->
                        // now download the image
                        imageReference
                                .getFile(logoFile)
                                .addOnSuccessListener { imageSnapshot: FileDownloadTask.TaskSnapshot? ->

                                    val fetchedSound = sound.copy(logoPath = logoFile.path, filePath = soundFile.path)
                                    val newList = mutableListOf(fetchedSound)

                                    // add custom sounds from SP as well
                                    val customSounds = loadFromSharedPreferences<ListOfSavedCustom>("PINNED_RECORDINGS")
                                    customSounds?.savedCustomList?.let { newList.addAll(it) }

                                    //all sounds fetched? update livedata
                                    if (_soundsStorageRepo.value?.size == list.size) {
                                        _soundsStorageRepo.value = newList
                                    }
                                }
                                .addOnFailureListener { e: Exception ->
                                    throw RuntimeException("Logo fetching failed")
                                }
                    }
                    .addOnFailureListener { e: Exception ->
                        throw RuntimeException("Sound fetching failed")
                    }
        }


    }

    private fun getNewSoundsFromStorage(list: List<Sound>, soundsInFirebase: List<Sound>) {

        val sounds = soundsInFirebase as ArrayList

        list.forEachIndexed { index, sound ->
            val soundReference = FirebaseStorage.getInstance().reference.child("sounds").child(sound.filePath)
            val imageReference = FirebaseStorage.getInstance().reference.child("logos").child(sound.logoPath)

            if (!logosFolder.exists()) {
                logosFolder.mkdirs()
            }

            // create the files
            val soundFile = File(soundsFolder, sound.filePath)
            val logoFile = File(logosFolder, sound.logoPath)

            // download sound from Firebase
            soundReference
                    .getFile(soundFile)
                    .addOnSuccessListener { soundSnapshot: FileDownloadTask.TaskSnapshot? ->
                        // now download the image
                        imageReference
                                .getFile(logoFile)
                                .addOnSuccessListener { imageSnapshot: FileDownloadTask.TaskSnapshot? ->

//                                    val fetchedSound = sound.copy(logoPath = logoFile.path, filePath = soundFile.path)
//                                    newList.add(0, fetchedSound)

                                    sounds.remove(sound)
                                    sounds.add(0, sound)

                                    if (index == list.size - 1) {
                                        loadLocalSounds(sounds)
                                    }


                                    //move fetched sounds to top
                                }
                                .addOnFailureListener { e: Exception ->
                                    throw RuntimeException("Logo fetching failed")
                                }
                    }
                    .addOnFailureListener { e: Exception ->
                        throw RuntimeException("Sound fetching failed")
                    }
        }
    }
}