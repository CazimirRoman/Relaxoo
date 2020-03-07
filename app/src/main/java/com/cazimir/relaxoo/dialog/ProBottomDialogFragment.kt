package com.cazimir.relaxoo.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cazimir.relaxoo.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProBottomDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = layoutInflater.inflate(R.layout.dialog_bottom_unlock_pro, null)

        return view
    }
}