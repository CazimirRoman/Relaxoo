package com.cazimir.relaxoo.dialog.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_recording.view.*

class BottomRecordingDialogFragment(val recording: Recording, val callback: OnActivityCallback) :
    BottomSheetDialogFragment() {

    init {
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = layoutInflater.inflate(R.layout.dialog_bottom_recording, null)
        view.delete_recording.setOnClickListener(View.OnClickListener {
            dismiss()
            context?.let { context ->
                callback.showDeleteConfirmationDialog(object : RecordingDeleted {
                    override fun deleted() {
                        callback.deleteRecording(recording)
                    }
                })
            }
        })

        view.edit_recording_name.setOnClickListener {
            dismiss()

            EditRecordingDialog(recording, object : RecordingBottomCallback {
                override fun edited(recording: Recording, newName: String) {
                    callback.renameRecording(
                            recording,
                            newName
                    )
                }
            }).show(parentFragmentManager, "editRecording")
        }

        view.pin_to_dashboard.setOnClickListener {
            dismiss()
            // add to sounds array on soundgridfragment
            // add to sound pool
            val sound: Sound = Sound.SoundBuilder.aSound()
                .withCustom(true)
                .withFilePath(recording.file.path)
                .withLogo("/storage/emulated/0/Relaxoo/logos/thunder.png")
                .withName("Custom")
                .build()

            callback.pinToDashBoardActionCalled(sound)
        }

        return view
    }
}