package com.cazimir.relaxoo.service.commands

import com.cazimir.relaxoo.model.Sound

class TriggerComboCommand(val soundList: List<Sound>, val boughtPro: Boolean) : ISoundServiceCommand
