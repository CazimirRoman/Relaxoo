package com.cazimir.relaxoo.dialog

import androidx.fragment.app.DialogFragment

open class RetainableDialogFragment() : DialogFragment() {

    init {
        retainInstance = true
    }

    override fun onDestroyView() {

        val dialog = dialog
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }
        super.onDestroyView()
    }
}
