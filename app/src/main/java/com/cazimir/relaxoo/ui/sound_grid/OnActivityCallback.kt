package com.cazimir.relaxoo.ui.sound_grid

import android.os.Bundle
import com.cazimir.relaxoo.dialog.OnDeleted
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.model.SavedCombo
import com.cazimir.relaxoo.model.Sound

interface OnActivityCallback {
    fun showAddToFavoritesDialog(sounds: List<Sound>)
    fun showTimerDialog()
    fun triggerCombo(savedCombo: SavedCombo)
    fun showDeleteConfirmationDialog(recDeleted: OnDeleted)
    fun showBottomDialogForPro()
    fun soundGridFragmentStarted()
    fun hideSplashScreen()
    fun removeAds()
    fun deleteRecording(recording: Recording)
    fun renameRecording(recording: Recording, toString: String)
    fun pinToDashBoardActionCalled(sound: Sound)
    fun playRewardAd()
    fun hideProgress()
    fun showMessageToUser(string: String, duration: Int)
    fun startBuyingProFlow()
    fun removeAdsViewAndButtonInAbout()
    fun logAnalyticsEvent(eventName: String, bundle: Bundle)
}