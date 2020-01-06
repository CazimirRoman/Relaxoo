package com.cazimir.relaxoo.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cazimir.relaxoo.R;

public class TimerDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String TAG = TimerDialog.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //instatiate callback

        final View form = getActivity().getLayoutInflater().inflate(R.layout.dialog_timer, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setNegativeButton(android.R.string.cancel, null);

        builder.setPositiveButton(android.R.string.ok, this);

        return (builder
                .setTitle("Set timer duration")
                .setView(form)
                .create());
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // handle which button was clicked here
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
