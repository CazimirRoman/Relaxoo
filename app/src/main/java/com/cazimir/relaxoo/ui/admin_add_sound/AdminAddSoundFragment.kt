package com.cazimir.relaxoo.ui.admin_add_sound

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.model.Sound
import kotlinx.android.synthetic.main.admin_add_sound_fragment.*
import kotlinx.android.synthetic.main.admin_add_sound_fragment.view.*

class AdminAddSoundFragment : Fragment() {
    private lateinit var viewModel: AdminAddSoundViewModel
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.admin_add_sound_fragment, container, false)

        view.save_button.setOnClickListener {
            onSaveButtonClicked()
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AdminAddSoundViewModel::class.java)
    }

    fun onCheckBoxProClicked() {
    }

    fun onSaveButtonClicked() {
        if (!sound_name.getText().toString().isEmpty() &&
                !sound_logo_url.getText().toString().isEmpty() &&
                !sound_name_storage.getText().toString().isEmpty()) {
            val sound = Sound.SoundBuilder.aSound()
                    .withName(sound_name.getText().toString())
                    .withLogo(sound_logo_url.getText().toString())
                    .withFilePath(sound_name_storage.getText().toString())
                    .withPro(checkbox_pro.isChecked())
                    .build()
            viewModel.saveToFirebase(sound)
            Log.d(TAG, "onSaveButtonClicked() called with: Saving to Firebase database")
        } else {
        }
    }

    companion object {
        private const val TAG = "AdminAddSoundFragment"
        fun newInstance(): AdminAddSoundFragment {
            return AdminAddSoundFragment()
        }
    }
}
