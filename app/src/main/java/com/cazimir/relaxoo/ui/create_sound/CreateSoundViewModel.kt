package com.cazimir.relaxoo.ui.create_sound

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.repository.ISoundRepository
import java.io.File
import java.util.*

class CreateSoundViewModel : ViewModel() {

    companion object {
        private const val TAG = "CreateSoundViewModel"
    }

    private var recordings: Array<File>? = null
    val _recordingsLive = MutableLiveData<ArrayList<Recording>>()

    lateinit var repository: ISoundRepository

    fun refreshList() {

        recordings = repository.getRecordings()

        val recordingsList = mutableListOf<Recording>()

        // after changing to viewpager2 the recordings returned null so I needed a safe call here
        recordings?.let {
            for (file in recordings!!) {
                recordingsList.add(Recording.Builder().withId(file.name).withFile(file).withFileName(file.name).build())
            }
        }

        Log.d(TAG, "refreshList called: $recordingsList")

        _recordingsLive.value = recordingsList as ArrayList<Recording>
    }

    fun deleteRecording(recording: Recording) {
        val deleteRecording = repository.deleteRecording(recording)
        if (deleteRecording) refreshList()
    }

    fun editRecording(recording: Recording, newName: String) {
        val result = repository.editRecording(recording, newName)
        if (result) refreshList()
    }
}
