package com.cazimir.relaxoo.model

import com.google.firebase.database.Exclude
import java.io.Serializable

data class Sound(
        val id: String = "",
        @get:Exclude
        val soundPoolId: Int = -1,
        @get:Exclude
        val streamId: Int = -1,
        val name: String = "",
        val logoPath: String = "",
        val filePath: String = "",
        @get:Exclude
        val playing: Boolean = false,
        @get:Exclude val volume: Float = 0.5f,
        val pro: Boolean = false,
        @get:Exclude
        val custom: Boolean = false,
        @get:Exclude
        val loaded: Boolean = false
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as Sound

        if (id != other.id) {
            return false
        }

        if (soundPoolId != other.soundPoolId) {
            return false
        }

        if (streamId != other.streamId) {
            return false
        }

        if (name != other.name) {
            return false
        }
        if (logoPath != other.logoPath) {
            return false
        }
        if (filePath != other.filePath) {
            return false
        }

        if (playing != other.playing) {
            return false
        }

        if (pro != other.pro) {
            return false
        }
        if (custom != other.custom) {
            return false
        }

        if (loaded != other.loaded) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        return soundPoolId
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("\n")
        stringBuilder.append("id = $id")
        stringBuilder.append(", ")
        stringBuilder.append("soundPoolId = $soundPoolId")
        stringBuilder.append(", ")
        stringBuilder.append("streamId = $streamId")
        stringBuilder.append(", ")
        stringBuilder.append("name = $name")
        stringBuilder.append(", ")
        stringBuilder.append("logoPath = $logoPath")
        stringBuilder.append(", ")
        stringBuilder.append("filePath = $filePath")
        stringBuilder.append(", ")
        stringBuilder.append("playing = $playing")
        stringBuilder.append(", ")
        stringBuilder.append("volume = $volume")
        stringBuilder.append(", ")
        stringBuilder.append("pro = $pro")
        stringBuilder.append(", ")
        stringBuilder.append("custom = $custom")
        stringBuilder.append(", ")
        stringBuilder.append("loaded = $loaded")
        return stringBuilder.toString()
    }
}
