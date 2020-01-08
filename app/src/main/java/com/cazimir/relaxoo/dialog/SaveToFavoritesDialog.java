package com.cazimir.relaxoo.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.ui.sound_grid.OnFavoriteSaved;

import java.util.HashMap;

public class SaveToFavoritesDialog extends DialogFragment
    implements DialogInterface.OnClickListener {

  private static final String TAG = "SaveToFavoritesDialog";
  private OnFavoriteSaved addToListInFragmentCallback;

  private View form = null;
  private final HashMap<Integer, Integer> playingSoundsParameters;

  public SaveToFavoritesDialog(HashMap<Integer, Integer> playingSounds) {
    this.playingSoundsParameters = playingSounds;
  }

    @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    addToListInFragmentCallback = (OnFavoriteSaved) getContext();

    form = getActivity().getLayoutInflater().inflate(R.layout.favorites_dialog, null);

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    return (builder
        .setTitle("Save Favorites Combos")
        .setView(form)
        .setPositiveButton(android.R.string.ok, this)
        .setNegativeButton(android.R.string.cancel, null)
        .create());
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    EditText comboName = form.findViewById(R.id.comboName);
    //saving a new combo
    SavedCombo savedCombo = new SavedCombo.Builder().withName(comboName.getText().toString()).withSoundPoolParameters(playingSoundsParameters).withPlaying(true).build();
    addToListInFragmentCallback.onSavedToList(savedCombo);
    Toast.makeText(getActivity(), "Saved combo!", Toast.LENGTH_LONG).show();
  }

  @Override
  public void onDismiss(DialogInterface unused) {
    super.onDismiss(unused);

    Log.d(getClass().getSimpleName(), "Goodbye!");
  }

  @Override
  public void onCancel(DialogInterface unused) {
    super.onCancel(unused);

    Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_LONG).show();
  }
}
