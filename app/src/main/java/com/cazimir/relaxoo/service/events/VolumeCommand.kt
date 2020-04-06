package com.cazimir.relaxoo.service.events

class VolumeCommand(val id: String, val streamId: Int, val leftVolume: Float, val rightVolume: Float) :
        ISoundPoolCommand