package com.cazimir.relaxoo.eventbus

import androidx.lifecycle.MutableLiveData

class EventBusTimer(val _timerRunning: MutableLiveData<Boolean>, val _timerText: MutableLiveData<String>)