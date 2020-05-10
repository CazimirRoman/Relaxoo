package com.cazimir.relaxoo.dialog.permission

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.dialog.RetainableDialogFragment

class PermissionNeededDialog(private val callback: OnStoragePermissionCallback, private val message: String, private val title: String) : RetainableDialogFragment(), DialogInterface.OnClickListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        builder.setPositiveButton(getString(R.string.ok), this)
        builder.setMessage(message)
        builder.setIcon(R.mipmap.ic_launcher)
        return builder.setTitle(title).create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        callback.okClicked()
        dialog.dismiss()
    }
}