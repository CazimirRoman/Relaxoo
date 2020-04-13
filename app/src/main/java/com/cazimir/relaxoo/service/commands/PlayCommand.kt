package com.cazimir.relaxoo.service.commands

class PlayCommand(
        val id: String,
        val soundPoolID: Int,
        val streamId: Int,
        val leftVolume: Float,
        val rightVolume: Float,
        val priority: Int,
        val loop: Int,
        val rate: Float
) : ISoundServiceCommand