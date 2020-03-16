package com.cazimir.relaxoo.shared

import TimerTaskExtended
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import java.util.Timer
import java.util.TimerTask

class SharedViewModel : ViewModel() {

    lateinit var adView: AdView
    val timer: Timer = Timer()

    var timerTaskExtended: TimerTaskExtended? = null

    var adsBought: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    var adRequest: AdRequest? = null
        set(value) {
            field = value
        }

    var previousColor: Int? = 0
        set(value) {
            field = value
        }

    var nextColor: MutableLiveData<Int> = MutableLiveData(0)

    var splashShown = false

    fun splashShown() {
        splashShown = true
    }

    fun adsBought(bought: Boolean) {
        adsBought.value = bought
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
}
