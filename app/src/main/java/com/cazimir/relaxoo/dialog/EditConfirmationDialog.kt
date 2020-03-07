package com.cazimir.relaxoo.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.model.Recording
import kotlinx.android.synthetic.main.edit_recording.view.*
import org.apache.commons.io.FilenameUtils

class EditConfirmationDialog(val recording: Recording, val callback: BottomCallback) :
    RetainableDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = activity?.layoutInflater?.inflate(R.layout.edit_recording, null)

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity!!)

        view?.let {

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(view)

            view.new_recording_name.setText(FilenameUtils.removeExtension(recording.file.name))
            // set dialog message
            alertDialogBuilder
                .setTitle("Rename created sound")
                .setCancelable(false)
                .setPositiveButton(
                    "OK",
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {

                            callback.edited(
                                recording,
                                view.new_recording_name?.getText().toString()
                            )
                        }
                    })
                .setNegativeButton(
                    "Cancel",
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            dialog?.cancel()
                        }
                    })

        }

        // create alert dialog
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog
            .getWindow()!!
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        return alertDialog
    }
}