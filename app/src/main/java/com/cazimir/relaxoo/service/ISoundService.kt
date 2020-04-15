package com.cazimir.relaxoo.service

import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.service.commands.PlayCommand
import com.cazimir.relaxoo.service.commands.StopCommand

interface ISoundService {
    fun loadToSoundPool(sounds: ArrayList<Sound>?)
    fun unload(soundPoolId1: String, sound: Int)
    fun play(playCommand: PlayCommand)
    fun stop(stopCommand: StopCommand)
    fun stopAllSounds()
    fun setVolume(id: String, streamId: Int, leftVolume: Float, rightVolume: Float)
}
