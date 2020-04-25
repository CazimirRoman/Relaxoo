package com.cazimir.relaxoo.ui.create_sound

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.repository.IRecordingRepository
import java.io.File

class CreateSoundViewModel : ViewModel() {

    companion object {
        private const val TAG = "CreateSoundViewModel"
    }

    private var recordings: Array<File>? = null
    private val _recordingsLive = MutableLiveData<ArrayList<Recording>>()

    val recordingsLive: LiveData<ArrayList<Recording>>
        get() = _recordingsLive

    lateinit var repository: IRecordingRepository

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
