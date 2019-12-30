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
import com.cazimir.relaxoo.ui.sound_grid.OnFavoriteSaved;

public class SaveToFavoritesDialog extends DialogFragment implements DialogInterface.OnClickListener {

  private static final String TAG = "SaveToFavoritesDialog";
  private OnFavoriteSaved onFavoriteSavedCallback;

  private View form = null;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    onFavoriteSavedCallback = (OnFavoriteSaved) getContext();

    form = getActivity().getLayoutInflater().inflate(R.layout.dialog, null);


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
    String template = getActivity().getString(R.string.toast);
    EditText value = form.findViewById(R.id.value);
    String msg = String.format(template, value.getText().toString());
    onFavoriteSavedCallback.onSaved(value.getText().toString());
    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
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
