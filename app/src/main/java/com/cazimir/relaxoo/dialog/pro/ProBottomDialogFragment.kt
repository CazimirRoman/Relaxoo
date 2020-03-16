package com.cazimir.relaxoo.dialog.pro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_unlock_pro.view.*

class ProBottomDialogFragment(val callback: OnActivityCallback) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = layoutInflater.inflate(R.layout.dialog_bottom_unlock_pro, null)

        view.viewAds.setOnClickListener {
            dismiss()
            callback.playRewardAd()
        }

        return view
    }
}