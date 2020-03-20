package com.cazimir.relaxoo.application

import android.app.Application
import com.cazimir.relaxoo.repository.ModelPreferencesManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ModelPreferencesManager.with(this)

        //val eventBus = EventBus.builder().addIndex(EventBusIndex()).build()
    }
}