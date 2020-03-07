package com.cazimir.relaxoo.dialog

import com.cazimir.relaxoo.model.Recording

interface BottomCallback {
    fun edited(recording: Recording, newName: String)
}
