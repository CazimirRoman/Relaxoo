package com.cazimir.relaxoo.shared

import androidx.lifecycle.ViewModel

class SplashViewModel : ViewModel() {

    var splashShown = false;

    fun splashShown() {
        splashShown = true
    }

}