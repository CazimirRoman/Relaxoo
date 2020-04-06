package com.cazimir.relaxoo.ui.sound_grid

import com.cazimir.relaxoo.dialog.OnDeleted
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.model.SavedCombo
import com.cazimir.relaxoo.model.Sound
import java.util.*

// used for initialiting lateinit var in SoundGridFragment in the onAttach callback method
class OnActivityCallbackTest : OnActivityCallback {
    override fun showNotification() {
        TODO("Not yet implemented")
    }

    override fun hideNotification() {
        TODO("Not yet implemented")
    }

    override fun showAddToFavoritesDialog(playingSounds: HashMap<Int, Int>?) {
        TODO("Not yet implemented")
    }

    override fun showToast(message: String?) {
        TODO("Not yet implemented")
    }

    override fun showTimerDialog() {
        TODO("Not yet implemented")
    }

    override fun triggerCombo(savedCombo: SavedCombo?) {
        TODO("Not yet implemented")
    }

    override fun showDeleteConfirmationDialog(recDeleted: OnDeleted?) {
        TODO("Not yet implemented")
    }

    override fun showBottomDialogForPro() {
        TODO("Not yet implemented")
    }

    override fun soundGridFragmentStarted() {

    }

    override fun hideSplash() {
        TODO("Not yet implemented")
    }

    override fun removeAds() {
        TODO("Not yet implemented")
    }

    override fun deleteRecording(recording: Recording) {
        TODO("Not yet implemented")
    }

    override fun renameRecording(recording: Recording, toString: String) {
        TODO("Not yet implemented")
    }

    override fun pinToDashBoardActionCalled(sound: Sound) {
        TODO("Not yet implemented")
    }

    override fun playRewardAd() {
        TODO("Not yet implemented")
    }
}
