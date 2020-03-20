package com.cazimir.relaxoo.service

import com.cazimir.relaxoo.model.Sound

interface ISoundPoolService {
    fun addRecordingToSoundPool(sound: Sound)
    fun removeRecordingFromSoundPool(sound: Sound)
    fun playStopSound(soundPoolId: Int, playing: Boolean, streamId: Int)
    fun stopAllSounds()
}
