package com.cazimir.relaxoo.ui.favorites

import com.cazimir.relaxoo.model.ListOfSavedCombos

interface IFavoritesRepository {
    fun getFavorites(): ListOfSavedCombos?
    fun addUpdatedList(savedComboObject: ListOfSavedCombos)
}
