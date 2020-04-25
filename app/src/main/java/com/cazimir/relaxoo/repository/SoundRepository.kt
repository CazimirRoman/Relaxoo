package com.cazimir.relaxoo.repository

import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.cazimir.relaxoo.EspressoIdlingResource
import com.cazimir.relaxoo.model.ListOfSavedCustom
import com.cazimir.relaxoo.model.Sound
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
    }

    private val database = FirebaseDatabase.getInstance()
    private val soundsRef = database.getReference("sounds")
    private val _soundsStorageRepo = MutableLiveData<List<Sound>>()

    override fun getSounds(): MutableLiveData<List<Sound>> {
        Log.d(TAG, "fetchSounds: called")
        val soundsInFirebase = mutableListOf<Sound>()
        // 1. check the Firebase Realtime Database for sounds

        // check database for sounds
        // Read from the database
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
                            getAssetsStorage(soundsInFirebase)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) { // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException())
                    }
                })



        return _soundsStorageRepo
    }

    private fun getAssetsStorage(sounds: List<Sound>) {
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


                                        // add custom sounds as well
                                        val customSounds = ModelPreferencesManager.get<ListOfSavedCustom>("PINNED_RECORDINGS")

                                        customSounds?.savedCustomList?.let { newList.addAll(it) }

                                        // TODO: 14-Mar-20 Add custom sounds here
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

            val customSounds = ModelPreferencesManager.get<ListOfSavedCustom>("PINNED_RECORDINGS")
            customSounds?.savedCustomList?.let { newList.addAll(it) }
            _soundsStorageRepo.value = newList
        }
    }
}