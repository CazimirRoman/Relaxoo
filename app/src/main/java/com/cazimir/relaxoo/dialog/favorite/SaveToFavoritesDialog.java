package com.cazimir.relaxoo.dialog.favorite;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.dialog.RetainableDialogFragment;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.ui.sound_grid.OnFavoriteSaved;

import java.util.HashMap;

public class SaveToFavoritesDialog extends RetainableDialogFragment
        implements DialogInterface.OnClickListener {

  private static final String TAG = "SaveToFavoritesDialog";
  private final HashMap<Integer, Integer> playingSoundsParameters;
  private OnFavoriteSaved addToListInFragmentCallback;
  private View form = null;

  public SaveToFavoritesDialog(HashMap<Integer, Integer> playingSounds) {
    this.playingSoundsParameters = playingSounds;
  }

  @NonNull
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
    // saving a new combo
    SavedCombo savedCombo =
            new SavedCombo.Builder()
                    .withName(comboName.getText().toString())
                    .withSoundPoolParameters(playingSoundsParameters)
                    .withPlaying(true)
                    .build();
    addToListInFragmentCallback.saved(savedCombo);
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