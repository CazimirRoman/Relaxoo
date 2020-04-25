package com.cazimir.relaxoo.repository

import android.os.Environment
import android.util.Log
import com.cazimir.relaxoo.model.Recording
import java.io.File

class RecordingRepository : IRecordingRepository {

    companion object {
        private val TAG = RecordingRepository::class.simpleName
    }

    override fun deleteRecording(recording: Recording): Boolean {

        val ownSoundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/own_sounds")
        val file2 = File(ownSoundsFolder, recording.file.name)
        val deleted = recording.file.delete()
        Log.d(TAG, "deleteRecording() called with: $deleted")
        return deleted
    }

    override fun editRecording(recording: Recording, newName: String): Boolean {

        val ownSoundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/own_sounds")
        val from = File(ownSoundsFolder, recording.file.name)
        val to = File(ownSoundsFolder, "$newName.wav")

        return from.renameTo(to)
    }

    override fun getRecordings(): Array<File>? {

        val ownSoundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/own_sounds")

        if (!ownSoundsFolder.exists()) {
            ownSoundsFolder.mkdir()
        }

        return ownSoundsFolder.listFiles()
    }
}
