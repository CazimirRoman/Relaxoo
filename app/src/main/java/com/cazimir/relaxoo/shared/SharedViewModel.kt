package com.cazimir.relaxoo.shared

import TimerTaskExtended
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class SharedViewModel : ViewModel() {

    val timer: Timer = Timer()

    var timerTaskExtended: TimerTaskExtended? = null

    private var _adsBought: MutableLiveData<Boolean> = MutableLiveData(false)
    val adsBought: LiveData<Boolean> = _adsBought
    private var _proBought: MutableLiveData<UnlockProEvent> = MutableLiveData(UnlockProEvent(eventProcessed = false, proBought = false))
    val proBought: LiveData<UnlockProEvent> = _proBought

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    var previousColor: Int? = 0
        set(value) {
            field = value
        }

    var nextColor: MutableLiveData<Int> = MutableLiveData(0)

    var splashScreenShown = false

    fun updateBoughtAds() {
        _adsBought.value = true
    }

    fun updateBoughtPro() {
        _proBought.value = UnlockProEvent(eventProcessed = false, proBought = true)
    }

    fun setTimerTaskExtended(context: Context, timerTask: TimerTask) {
        timerTaskExtended = TimerTaskExtended(context, timerTask)
    }

    fun startOrStop() {
        timer
                .scheduleAtFixedRate(
                        timerTaskExtended?.timerTask, 2000, 5000
                )
    }

    fun updateProcessedUnlockProEvent() {
        _proBought.value = UnlockProEvent(eventProcessed = true, proBought = true)
    }
}
