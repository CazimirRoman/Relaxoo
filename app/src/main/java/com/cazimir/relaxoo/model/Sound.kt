package com.cazimir.relaxoo.model

import com.google.firebase.database.Exclude
import java.io.Serializable

data class Sound(val id: String = "", @Exclude val soundPoolId: Int = -1, @Exclude val streamId: Int = -1, val name: String = "", val logoPath: String = "", val filePath: String = "", @Exclude val playing: Boolean = false, @Exclude val volume: Float = 0.5f, val pro: Boolean = false, val custom: Boolean = false) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val sound = other as Sound
        return soundPoolId == sound.soundPoolId
    }

    override fun hashCode(): Int {
        return soundPoolId
    }
}