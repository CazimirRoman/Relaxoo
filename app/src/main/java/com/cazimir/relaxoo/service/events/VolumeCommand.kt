package com.cazimir.relaxoo.service.events

class VolumeCommand(val streamId: Int, val leftVolume: Float, val rightVolume: Float) :
    ISoundPoolCommand