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
        const val ALL_SOUNDS = "ALL_SOUNDS"
    }

    private val database = FirebaseDatabase.getInstance()
    private val soundsRef = database.getReference("sounds")
    private val _soundsStorageRepo = MutableLiveData<List<Sound>>()
    private var mAuth = FirebaseAuth.getInstance()
    private val authenticationComplete: MutableLiveData<Boolean> = MutableLiveData()

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
            Log.d(TAG, "EspressoIdlingResource.increment called")
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
                                getSoundAndLogo(soundsInFirebase.reversed())
                                //save to shared preferences in order to load offline if no internet available
                                SharedPreferencesUtil.saveToSharedPreferences(ListOfSounds(soundsInFirebase as List<Sound>), ALL_SOUNDS)
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
        Log.d(TAG, "getAssetsStorage: loading assets from local storage")
        val soundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/sounds")
        val logosFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/logos")

        val soundsDirectory = File(soundsFolder.absolutePath)
        val logosDirectory = File(logosFolder.absolutePath)

        val newList = mutableListOf<Sound>()

        val localFiles = soundsFolder.listFiles()

        //get list of sounds from shared preferences
        val allSounds = loadFromSharedPreferences<ListOfSounds>(ALL_SOUNDS)?.sounds

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
        _soundsStorageRepo.value = newList.reversed()
        return _soundsStorageRepo
    }

    private fun getSoundAndLogo(sounds: List<Sound>) {
        // check if files already downloaded locally
        val soundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/sounds")
        val logosFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/logos")
        if (!soundsFolder.exists()) {
            soundsFolder.mkdirs()
        }
        // check locally to see how many files there are
        val soundsDirectory = File(soundsFolder.absolutePath)
        val files = soundsDirectory.listFiles()
        // no files downloaded locally
        if (files == null || files.size < sounds.size) {
            Log.d(TAG, "getAssetsStorage: loading assets from firebase")
            // get sounds
            for (sound in sounds) {
                val soundReference = FirebaseStorage.getInstance().reference.child("sounds").child(sound.filePath)
                val imageReference = FirebaseStorage.getInstance().reference.child("logos").child(sound.logoPath)

                if (!logosFolder.exists()) {
                    logosFolder.mkdirs()
                }

                val soundFile = File(soundsFolder, sound.filePath)

                Log.d(TAG, "getAssetsFromFirebaseStorage: soundFile: $soundFile")
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
                                        Log.d(TAG, "onSuccess for imageReference: called")

                                        val fetchedSound = sound.copy(logoPath = logoFile.path, filePath = soundFile.path)

                                        val newList = mutableListOf(fetchedSound)

                                        // add custom sounds from SP as well
                                        val customSounds = loadFromSharedPreferences<ListOfSavedCustom>("PINNED_RECORDINGS")

                                        customSounds?.savedCustomList?.let { newList.addAll(it) }

                                        if (_soundsStorageRepo.value?.size == sounds.size) {
                                            _soundsStorageRepo.value = newList
                                        }
                                    }
                                    .addOnFailureListener { e: Exception ->
                                        Log.d(TAG, "onFailure logo: " + e.message)
                                    }
                        }
                        .addOnFailureListener { e: Exception ->
                            Log.d(TAG, "onFailure sound: " + e.message)
                        }
            }
        } else {
            Log.d(TAG, "getAssetsStorage: loading assets from local storage")
            val logosDirectory = File(logosFolder.absolutePath)

            val newList = mutableListOf<Sound>()

            for (sound in sounds) {
                val localSound = sound.copy(logoPath = logosDirectory.toString() + "/" + sound.logoPath, filePath = soundsDirectory.toString() + "/" + sound.filePath)
                newList.add(localSound)
            }

            //adding custom sounds as well
            val customSounds = loadFromSharedPreferences<ListOfSavedCustom>("PINNED_RECORDINGS")
            customSounds?.savedCustomList?.let { newList.addAll(it) }
            _soundsStorageRepo.value = newList
        }
    }
}