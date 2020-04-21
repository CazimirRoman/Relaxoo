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
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val sound = other as Sound
        return id == sound.id
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
