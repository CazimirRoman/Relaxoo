package com.cazimir.relaxoo.dialog.timer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.dialog.RetainableDialogFragment;

import java.util.Arrays;
import java.util.List;

public class TimerDialog extends RetainableDialogFragment {

  private static final String TAG = TimerDialog.class.getSimpleName();
  private List<Integer> timers = Arrays.asList(5, 10, 15, 30, 45, 60, 120, 999);
  private OnTimerDialogCallback callback;

  public TimerDialog(OnTimerDialogCallback callback) {
    this.callback = callback;
  }

  public TimerDialog() {
  }

  public static String getCountTimeByLong(long finishTime) {
    int totalTime = (int) (finishTime / 1000);
    int hour = 0, minute = 0, second = 0;

    if (3600 <= totalTime) {
      hour = totalTime / 3600;
      totalTime = totalTime - 3600 * hour;
    }
    if (60 <= totalTime) {
      minute = totalTime / 60;
      totalTime = totalTime - 60 * minute;
    }
    if (0 <= totalTime) {
      second = totalTime;
    }
    StringBuilder sb = new StringBuilder();

    if (hour < 10) {
      sb.append("0").append(hour).append(":");
    } else {
      sb.append(hour).append(":");
    }
    if (minute < 10) {
      sb.append("0").append(minute).append(":");
    } else {
      sb.append(minute).append(":");
    }
    if (second < 10) {
      sb.append("0").append(second);
    } else {
      sb.append(second);
    }
    return sb.toString();
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    final View form = getActivity().getLayoutInflater().inflate(R.layout.dialog_timer, null);

    ListView listView = form.findViewById(R.id.timer_list);

    Context context;
    final ArrayAdapter adapter =
        new ArrayAdapter<Integer>(
            getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, timers) {
          @Override
          public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView text1 = view.findViewById(android.R.id.text1);

            Integer minutes = timers.get(position);

            if (minutes == 999) {
              text1.setText(getString(R.string.timer_custom_time));
            } else {
              text1.setText(
                      (minutes >= 60)
                              ? (minutes >= 120)
                              ? getString(R.string.timer_dialog_hours, minutes / 60)
                              : getString(R.string.timer_dialog_hour, minutes / 60)
                              : getString(R.string.timer_dialog_minutes, minutes));
            }

            return view;
          }
        };

    listView.setAdapter(adapter);

    listView.setOnItemClickListener(
            (parent, view, position, id) -> {
              callback.startCountDownTimer(timers.get(position));
              dismiss();
            });

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    return (builder.setTitle(getString(R.string.set_timer_duration)).setView(form).create());
  }
}
