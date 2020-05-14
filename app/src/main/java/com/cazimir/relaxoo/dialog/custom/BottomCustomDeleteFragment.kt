package com.cazimir.relaxoo.dialog.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.model.Sound
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_custom.view.*

class BottomCustomDeleteFragment(val sound: Sound, val callback: CustomBottomCallback) :
        BottomSheetDialogFragment() {

    init {
        retainInstance = true
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val view = layoutInflater.inflate(R.layout.dialog_bottom_custom, null)

        view.delete_from_dashboard.setOnClickListener(View.OnClickListener {
            dismiss()
            context?.let { _ ->
                callback.deletedClicked(sound)
            }
        })

        return view
    }
}
