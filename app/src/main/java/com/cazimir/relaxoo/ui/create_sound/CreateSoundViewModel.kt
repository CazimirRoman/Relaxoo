package com.cazimir.relaxoo.ui.create_sound

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.repository.ISoundRepository
import java.io.File
import java.util.ArrayList

class CreateSoundViewModel : ViewModel() {

    companion object {
        private const val TAG = "CreateSoundViewModel"
    }

    private var recordings: Array<File>? = null
    val _recordingsLive = MutableLiveData<ArrayList<Recording>>()

    lateinit var repository: ISoundRepository

    fun refreshList() {

        recordings = repository.getRecordings()

        val recordingsList = ArrayList<Recording>()

        for (file in recordings!!) {
            recordingsList.add(Recording.Builder().withFile(file).withFileName(file.name).build())
        }

        _recordingsLive.value = recordingsList
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
