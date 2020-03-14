package com.cazimir.relaxoo.ui.favorites

import com.cazimir.relaxoo.model.ListOfSavedCombos
import com.cazimir.relaxoo.repository.ModelPreferencesManager

class FavoritesRepository : IFavoritesRepository {

    companion object {
        private const val COMBO_LIST = "COMBO_LIST"
    }

    override fun addUpdatedList(updatedList: ListOfSavedCombos) {
        ModelPreferencesManager.save(updatedList, COMBO_LIST)
    }

    override fun getFavorites(): ListOfSavedCombos? {
        return ModelPreferencesManager.get<ListOfSavedCombos>(COMBO_LIST)
    }
}