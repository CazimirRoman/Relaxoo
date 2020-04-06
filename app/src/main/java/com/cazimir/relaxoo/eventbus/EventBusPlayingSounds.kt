package com.cazimir.relaxoo.eventbus

import androidx.lifecycle.MutableLiveData
import com.cazimir.relaxoo.model.PlayingSound

class EventBusPlayingSounds(val playingSounds: MutableLiveData<ArrayList<PlayingSound>>)