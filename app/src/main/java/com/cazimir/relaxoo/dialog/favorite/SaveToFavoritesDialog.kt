package com.cazimir.relaxoo.dialog.favorite

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.dialog.RetainableDialogFragment
import com.cazimir.relaxoo.model.SavedCombo
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.ui.sound_grid.OnFavoriteSaved
import com.cazimir.utilitieslibrary.onChange
import kotlinx.android.synthetic.main.favorites_dialog.view.*

class SaveToFavoritesDialog(private val playingSoundIds: List<Sound>) : RetainableDialogFragment(), DialogInterface.OnClickListener {

    private lateinit var positiveButton: Button
    private var touched = false
    private var addToListInFragmentCallback: OnFavoriteSaved? = null
    private val comboNameString: MutableLiveData<String> = MutableLiveData("")
    private lateinit var form: View
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        addToListInFragmentCallback = context as OnFavoriteSaved?

        form = activity!!.layoutInflater.inflate(R.layout.favorites_dialog, null)

        form.combo_name.onChange {
            comboNameString.value = it
        }

        comboNameString.observe(this, Observer {
            if (it.isEmpty()) {
                if (touched) {
                    form.combo_name.error = getString(R.string.no_combo_text)
                }

                positiveButton.isEnabled = false
            } else {
                touched = true
                positiveButton.isEnabled = true
            }

        })

        val builder = AlertDialog.Builder(activity!!)

        return builder
                .setTitle("Save Favorites Combos")
                .setView(form)
                .setPositiveButton(getString(R.string.ok), this)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        form.combo_name.requestFocus()
        dialog?.window?.setSoftInputMode(SOFT_INPUT_STATE_VISIBLE)
        return super.onCreateView(inflater, container, savedInstanceState)

    }

    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog?
        if (d != null) {
            this.positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val comboName = form!!.findViewById<EditText>(R.id.combo_name)
        // saving a new combo
        val savedCombo = SavedCombo.Builder()
                .withName(comboName.text.toString())
                .withSoundPoolParameters(playingSoundIds)
                .withPlaying(true)
                .build()
        addToListInFragmentCallback!!.saved(savedCombo)
    }

    override fun onDismiss(unused: DialogInterface) {
        super.onDismiss(unused)
        Log.d(javaClass.simpleName, "Goodbye!")
    }

    companion object {
        private const val TAG = "SaveToFavoritesDialog"
    }

}