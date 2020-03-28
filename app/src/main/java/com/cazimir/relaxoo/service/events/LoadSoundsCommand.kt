package com.cazimir.relaxoo.service.events

import com.cazimir.relaxoo.model.Sound

class LoadSoundsCommand(val sounds: ArrayList<Sound>) : ISoundPoolCommand