package com.cazimir.relaxoo.shared

import androidx.lifecycle.ViewModel
import java.util.Timer

class MainActivityViewModel : ViewModel() {

    companion object {
        private const val TAG = "MainActivityViewModel"
    }

    var previousColor: Int? = 0
        set(value) {
            field = value
        }


    var nextColor: Int? = 0

    var splashShown = false;

    val timer: Timer = Timer()

    fun splashShown() {
        splashShown = true
    }
}
