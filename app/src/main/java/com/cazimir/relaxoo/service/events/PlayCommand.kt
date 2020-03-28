package com.cazimir.relaxoo.service.events

class PlayCommand(
    val soundPoolID: Int,
    val leftVolume: Float,
    val rightVolume: Float,
    val priority: Int,
    val loop: Int,
    val rate: Float
) : ISoundPoolCommand