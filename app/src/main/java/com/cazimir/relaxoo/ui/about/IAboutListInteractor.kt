package com.cazimir.relaxoo.ui.about

import androidx.lifecycle.LiveData

interface IAboutListInteractor {
    fun removeAds(): LiveData<Boolean>
    fun settings()
    fun share()
    fun privacyPolicy()
    fun rateApp()
    fun moreApps()
}
