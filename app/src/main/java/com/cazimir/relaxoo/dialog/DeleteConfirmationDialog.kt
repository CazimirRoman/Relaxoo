package com.cazimir.relaxoo.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.dialog.favorite.FavoriteDeleted
import com.cazimir.relaxoo.dialog.recording.RecordingDeleted
import kotlinx.android.synthetic.main.dialog_delete_confirmation.view.*

class DeleteConfirmationDialog(private val callback: OnDeleted) : RetainableDialogFragment(), DialogInterface.OnClickListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val form = activity!!.layoutInflater.inflate(R.layout.dialog_delete_confirmation, null)

        if (callback is RecordingDeleted) form.delete_confirmation_text.text = getString(R.string.delete_confirmation_recording)

        val builder = AlertDialog.Builder(activity!!)
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.setPositiveButton(getString(R.string.ok), this)
        return builder.setTitle(getString(R.string.delete_title)).setView(form).create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        // handle which button was clicked here
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (callback is FavoriteDeleted) {
                callback.deleted()
            } else {
                (callback as RecordingDeleted).deleted()
            }
        }
    }

    companion object {
        private val TAG = DeleteConfirmationDialog::class.java.simpleName
    }

}