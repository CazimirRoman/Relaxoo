package com.cazimir.relaxoo.dialog.recording

import com.cazimir.relaxoo.model.Recording

interface RecordingBottomCallback {
    fun edited(recording: Recording, newName: String)
}
