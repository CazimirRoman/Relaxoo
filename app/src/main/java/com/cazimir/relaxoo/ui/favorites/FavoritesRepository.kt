package com.cazimir.relaxoo.ui.favorites

import com.cazimir.relaxoo.model.ListOfSavedCombos
import com.cazimir.utilitieslibrary.SharedPreferencesUtil.loadFromSharedPreferences
import com.cazimir.utilitieslibrary.SharedPreferencesUtil.saveToSharedPreferences

class FavoritesRepository : IFavoritesRepository {

    companion object {
        private const val COMBO_LIST = "COMBO_LIST"
    }

    override fun addUpdatedList(updatedList: ListOfSavedCombos) {
        saveToSharedPreferences(updatedList, COMBO_LIST)
    }

    override fun getFavorites(): ListOfSavedCombos? {
        return loadFromSharedPreferences<ListOfSavedCombos>(COMBO_LIST)
    }
}
