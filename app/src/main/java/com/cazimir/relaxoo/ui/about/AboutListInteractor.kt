package com.cazimir.relaxoo.ui.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AboutListInteractor : IAboutListInteractor {

    private val removedAds: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun removeAds(): LiveData<Boolean> {

        // so some work to remove ads
        // then set removedAds to either true or false
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun settings() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun share() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun privacyPolicy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun rateApp() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun moreApps() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
