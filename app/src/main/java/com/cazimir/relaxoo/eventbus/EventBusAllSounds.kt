package com.cazimir.relaxoo.eventbus

import androidx.lifecycle.MutableLiveData
import com.cazimir.relaxoo.model.Sound

class EventBusAllSounds(val allSoundsFromService: MutableLiveData<List<Sound>>)
