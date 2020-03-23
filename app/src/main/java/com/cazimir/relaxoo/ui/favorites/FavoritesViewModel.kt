package com.cazimir.relaxoo.ui.favorites

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cazimir.relaxoo.model.ListOfSavedCombos
import com.cazimir.relaxoo.model.SavedCombo

class FavoritesViewModel : ViewModel() {

    companion object {
        private const val TAG = "FavoritesViewModel"
    }

    private val _savedCombos = MutableLiveData<ListOfSavedCombos>()

    lateinit var repository: IFavoritesRepository

    fun savedCombosLive(): MutableLiveData<ListOfSavedCombos> {
        return _savedCombos
    }

    fun fetchFavorites() {

        val favorites = repository.getFavorites()

        if (favorites == null) {
            val emptyListOfSavedCombos = ListOfSavedCombos(mutableListOf())
            _savedCombos.value = emptyListOfSavedCombos
            return
        }

        _savedCombos.value = favorites
    }

    fun deleteCombo(position: Int) {
        val currentSavedCombos = _savedCombos.value
        val currentList = currentSavedCombos?.savedComboList?.toMutableList()
        currentList?.removeAt(position)
        _savedCombos.value = ListOfSavedCombos(currentList)
        repository.addUpdatedList(ListOfSavedCombos(currentList))
    }

    fun addFavorite(comboToBeAdded: SavedCombo) {
        // get current object with list and return another one with savedCombo added
        val currentSavedCombos = _savedCombos.value
        val currentList = currentSavedCombos?.savedComboList?.toMutableList()
        currentList?.add(comboToBeAdded)
        _savedCombos.value = ListOfSavedCombos(currentList)
        repository.addUpdatedList(ListOfSavedCombos(currentList))
    }
}
