package com.cazimir.relaxoo.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.cazimir.relaxoo.R;

import java.util.Calendar;

public class TimerDialog extends DialogFragment {

    private static final String TAG = TimerDialog.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //instatiate callback

        final View form = getActivity().getLayoutInflater().inflate(R.layout.dialog_timer, null);

        ListView listView = form.findViewById(R.id.timer_list);

//        listView.setAdapter(new SimpleAdapter(getActivity(), ))

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return (builder
                .setTitle("Set timer duration")
                .setView(form)
                .create());
    }

//    private void setCustomTimer(){
//
//        Calendar calendar = Calendar.getInstance();
//
//        new TimePickerDialog(
//                getContext(),
//                timerPickerListener,
//                0,
//                0,
//                true)
//                .show();
//    }

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
