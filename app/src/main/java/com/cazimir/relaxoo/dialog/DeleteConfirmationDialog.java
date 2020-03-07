package com.cazimir.relaxoo.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.cazimir.relaxoo.R;

public class DeleteConfirmationDialog extends RetainableDialogFragment
        implements DialogInterface.OnClickListener {

  private static final String TAG = DeleteConfirmationDialog.class.getSimpleName();
  private OnDeleted callback;

  public DeleteConfirmationDialog(OnDeleted callback) {
    this.callback = callback;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    // instatiate callback

//    callback = (OnDeleted) getContext();

    final View form =
        getActivity().getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null);

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    builder.setNegativeButton(android.R.string.cancel, null);

    builder.setPositiveButton(android.R.string.ok, this);

    return (builder.setTitle("Delete?").setView(form).create());
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    // handle which button was clicked here
    if (which == DialogInterface.BUTTON_POSITIVE) {

      if (callback instanceof FavoriteDeleted) {
        ((FavoriteDeleted) callback).deleted();
      } else {
        ((RecordingDeleted) callback).deleted();
      }
    }
  }
}
