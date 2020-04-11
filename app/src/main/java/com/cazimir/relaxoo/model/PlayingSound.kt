package com.cazimir.relaxoo.model

data class PlayingSound(val id: String, val streamId: Int, val volume: Float = 0.5f) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val playingSound = other as PlayingSound
        return id == playingSound.id
    }
}