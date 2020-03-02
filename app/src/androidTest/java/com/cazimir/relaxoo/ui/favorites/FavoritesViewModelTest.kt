package com.cazimir.relaxoo.ui.favorites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.cazimir.relaxoo.model.ListOfSavedCombos
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FavoritesViewModelTest {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val favoritesObserver: Observer<ListOfSavedCombos> = mock()
    private val mockFavoritesRepository: IFavoritesRepository = mock()
    private val viewModel = FavoritesViewModel()

    @Before
    fun setUp() {
        viewModel.savedCombosLive().observeForever(favoritesObserver)
        viewModel.repository = mockFavoritesRepository
    }

    @Test
    fun fetchFavoritesCallsGetFavoritesOnRepository() {
        viewModel.fetchFavorites()
        verify(mockFavoritesRepository).getFavorites()
    }

    @Test
    fun fetchFavoritesTriggersObserver() {
        // stubFavoritesRepositoryGetAllFavorites();
    }

    // private fun stubFavoritesRepositoryGetAllFavorites() {
    //     whenever(mockFavoritesRepository.getFavorites()).thenReturn(ListOfSavedCombos())
    // }
}