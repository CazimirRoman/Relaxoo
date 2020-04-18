package com.cazimir.relaxoo.service.commands

class VolumeCommand(val id: String, val streamId: Int, val leftVolume: Float, val rightVolume: Float) :
        ISoundServiceCommand
