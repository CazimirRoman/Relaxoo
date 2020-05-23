package com.cazimir.relaxoo.ui.create_sound

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cazimir.relaxoo.eventbus.EventBusUpdateRecordingName
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.repository.IRecordingRepository
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*

class CreateSoundViewModel : ViewModel() {

    companion object {
        private const val TAG = "CreateSoundViewModel"
    }


    private val _recordingsLive = MutableLiveData<ArrayList<Recording>>()

    val recordingsLive: LiveData<ArrayList<Recording>>
        get() = _recordingsLive

    lateinit var repository: IRecordingRepository

    fun refreshList() {

        val recordings: Array<File> = repository.getRecordings() ?: mutableListOf<File>() as Array<File>

        val recordingsList = mutableListOf<Recording>()

        // after changing to viewpager2 the recordings returned null so I needed a safe call here
        recordings.let {
            Arrays.sort(recordings) { f1, f2 -> f1.lastModified().compareTo(f2.lastModified()) }
            recordings.reverse()
            for (file in recordings) {
                recordingsList.add(Recording.Builder().withId(file.name).withFile(file).withFileName(file.name).build())
            }
        }

        _recordingsLive.value = recordingsList as ArrayList<Recording>
    }

    fun deleteRecording(recording: Recording) {
        val deleteRecording = repository.deleteRecording(recording)
        if (deleteRecording) refreshList()
    }

    fun editRecording(recording: Recording, newName: String) {
        val result = repository.editRecording(recording, newName)
        if (result) {
            refreshList()
            EventBus.getDefault().post(EventBusUpdateRecordingName(recording.id, newName))
        }
    }
}
