package com.cazimir.relaxoo.repository

import com.cazimir.relaxoo.model.Recording
import java.io.File

interface IRecordingRepository {
    fun deleteRecording(recording: Recording): Boolean
    fun editRecording(recording: Recording, newName: String): Boolean
    fun getRecordings(): Array<File>?
}
