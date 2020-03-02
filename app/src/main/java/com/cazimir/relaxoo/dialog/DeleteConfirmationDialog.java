package com.cazimir.relaxoo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.cazimir.relaxoo.OnFavoriteDeleted;
import com.cazimir.relaxoo.R;

public class DeleteConfirmationDialog extends RetainableDialogFragment
        implements DialogInterface.OnClickListener {

  private static final String TAG = DeleteConfirmationDialog.class.getSimpleName();
  private int position;
  private OnFavoriteDeleted deleteFromListCallback;

  public DeleteConfirmationDialog(int position) {
    this.position = position;
    setRetainInstance(true);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    // instatiate callback

    deleteFromListCallback = (OnFavoriteDeleted) getContext();

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
      deleteFromListCallback.deleted(position);
    }
  }

  @Override
  public void onDismiss(DialogInterface unused) {
    // handle dismiss actions here
    super.onDismiss(unused);
  }

  @Override
  public void onCancel(DialogInterface unused) {
    // handle cancel actions here
    super.onCancel(unused);
  }
}
