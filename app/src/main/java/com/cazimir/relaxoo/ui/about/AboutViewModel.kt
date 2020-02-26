package com.cazimir.relaxoo.ui.about

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.model.AboutItem

class AboutViewModel : ViewModel() {

    companion object {
        private const val TAG = "AboutViewModel"
    }

    lateinit var aboutListInteractor: IAboutListInteractor

    private val _aboutItems = MutableLiveData<List<AboutItem>>()
    val _removeAds = MutableLiveData<Boolean>()
    val _settings = MutableLiveData<Boolean>()
    val _share = MutableLiveData<Boolean>()

    private val aboutItems: MutableList<AboutItem> = ArrayList()

    private val observer: Observer<Boolean> = Observer { removed -> _removeAds.value = removed }


    fun aboutItems(): MutableLiveData<List<AboutItem>> {
        return _aboutItems
    }

    fun populateAboutItems() {

        Log.d(TAG, "populateAboutItems: called")
        if (aboutItems.isEmpty()) {

            aboutListInteractor = AboutListInteractor()

            aboutItems.add(AboutItem(name = "Remove Ads", icon = R.drawable.ic_message))
            aboutItems.add(AboutItem(name = "Settings", icon = R.drawable.ic_message))
            aboutItems.add(AboutItem(name = "Share", icon = R.drawable.ic_message))
            aboutItems.add(AboutItem(name = "Privacy Policy", icon = R.drawable.ic_message))
            aboutItems.add(AboutItem(name = "Rate App", icon = R.drawable.ic_message))
            aboutItems.add(AboutItem(name = "More App", icon = R.drawable.ic_message))
        }
        _aboutItems.value = aboutItems
    }

    override fun onCleared() {
        aboutListInteractor.removeAds().removeObserver(observer)
        super.onCleared()
    }

    fun removeAds() {
        aboutListInteractor.removeAds().observeForever(observer)
    }

    fun settings() {
        _settings.value = true
    }

    fun share() {
        _share.value = true
    }
}