package com.cazimir.relaxoo.ui.more_apps

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class MoreAppsViewModel : ViewModel() {

    val remoteReady: MutableLiveData<Boolean> = MutableLiveData()

    init {
        remoteReady.value = false
    }

    fun remoteConfigReady() {
        remoteReady.value = true
    }

    val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

}
