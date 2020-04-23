package com.cazimir.relaxoo.dialog.recording

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.dialog.RetainableDialogFragment
import com.cazimir.relaxoo.model.Recording
import kotlinx.android.synthetic.main.edit_recording.view.*
import org.apache.commons.io.FilenameUtils

class EditRecordingDialog(val recording: Recording, val callback: RecordingBottomCallback) :
        RetainableDialogFragment() {

    private lateinit var layout: View
    private lateinit var positiveButton: Button
    val editTextString: MutableLiveData<String> = MutableLiveData()

    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog?
        if (d != null) {
            this.positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout.new_recording_name.requestFocus()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        layout = activity!!.layoutInflater.inflate(R.layout.edit_recording, null)

        layout.new_recording_name.onChange {
            Log.d(TAG, "new_recording_name: changed: $it")
            editTextString.value = it
        }

        editTextString.observe(this, Observer {
            if (it.isEmpty()) {
                layout.new_recording_name.error = getString(R.string.no_recording_text)
                positiveButton.isEnabled = false
            } else {
                positiveButton.isEnabled = true
            }

        })

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity!!)

        layout?.let {

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(layout)

            layout.new_recording_name.setText(FilenameUtils.removeExtension(recording.file.name))
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
                                    layout.new_recording_name?.getText().toString()
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

    private fun EditText.onChange(cb: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                cb(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    companion object {
        private const val TAG = "EditRecordingDialog"
    }
}
