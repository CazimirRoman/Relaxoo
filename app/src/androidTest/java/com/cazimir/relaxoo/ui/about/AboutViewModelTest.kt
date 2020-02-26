package com.cazimir.relaxoo.ui.about

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before

import org.junit.Rule
import org.junit.Test

class AboutViewModelTest {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val removeAdsObserver: Observer<Boolean> = mock()
    private val settingsClickedObserver: Observer<Boolean> = mock()
    private val shareClickedObserver: Observer<Boolean> = mock()
    private val mockAboutListInteractor: IAboutListInteractor = mock()
    val viewModel = AboutViewModel()

    @Before
    fun setUp() {
        viewModel._removeAds.observeForever(removeAdsObserver)
        viewModel._settings.observeForever(settingsClickedObserver)
        viewModel._share.observeForever(shareClickedObserver)
        viewModel.aboutListInteractor = mockAboutListInteractor
    }

    @Test
    fun removeAdsCallsRemoveAdsOnAboutListInteractor() {
        stubAboutListInteractorRemoveAds()
        viewModel.removeAds()
        verify(mockAboutListInteractor).removeAds()
    }

    @Test
    fun removeAdsTriggersObserver() {
        stubAboutListInteractorRemoveAds()
        viewModel.removeAds()
        verify(removeAdsObserver).onChanged(true)
    }

    @Test
    fun settingsTriggersObserverOnAboutFragment() {
        viewModel.settings()
        verify(settingsClickedObserver).onChanged(true)
    }

    @Test
    fun shareTriggersObserverOnAboutFragment() {
        viewModel.share()
        verify(shareClickedObserver).onChanged(true)
    }

    private fun stubAboutListInteractorRemoveAds() {
        whenever(mockAboutListInteractor.removeAds()).thenReturn(MutableLiveData<Boolean>(true))
    }
}