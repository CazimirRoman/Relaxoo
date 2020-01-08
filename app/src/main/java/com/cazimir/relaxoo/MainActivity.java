package com.cazimir.relaxoo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.cazimir.relaxoo.adapter.PagerAdapter;
import com.cazimir.relaxoo.dialog.OnTimerDialogCallback;
import com.cazimir.relaxoo.dialog.SaveToFavoritesDialog;
import com.cazimir.relaxoo.dialog.TimerDialog;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.model.Sound;
import com.cazimir.relaxoo.ui.favorites.FavoritesFragment;
import com.cazimir.relaxoo.ui.sound_grid.OnActivityNeededCallback;
import com.cazimir.relaxoo.ui.sound_grid.OnFavoriteSaved;
import com.cazimir.relaxoo.ui.sound_grid.SoundGridFragment;

import java.util.List;

public class MainActivity extends FragmentActivity
    implements OnActivityNeededCallback, OnFavoriteSaved, OnTimerDialogCallback {

  private static final String TAG = "MainActivity";

  private static final String CHANNEL_WHATEVER = "" + "";
  private static int NOTIFY_ID = 1337;
  private NotificationManager notificationManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);
    ViewPager pager = findViewById(R.id.pager);
    pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
    setupNotifications();
  }

  private void setupNotifications() {
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(CHANNEL_WHATEVER) == null) {

      NotificationChannel notificationChannel =
          new NotificationChannel(CHANNEL_WHATEVER, "Whatever", NotificationManager.IMPORTANCE_LOW);
      notificationChannel.setSound(null, null);
      notificationManager.createNotificationChannel(notificationChannel);
    }
  }

  @Override
  public void showNotification() {

    NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_WHATEVER);

    b.setAutoCancel(true);

    b.setContentTitle("Relaxoo")
        .setContentText("1 sound selected")
        .setSmallIcon(android.R.drawable.stat_sys_download_done);

    notificationManager.notify(NOTIFY_ID, b.build());
  }

  @Override
  public void hideNotification() {
    notificationManager.cancel(NOTIFY_ID);
  }

  @Override
  public void showAddToFavoritesDialog(List<Sound> playingSounds) {
    new SaveToFavoritesDialog(playingSounds).show(getSupportFragmentManager(), "save");
  }

  @Override
  public void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void showTimerDialog() {
    new TimerDialog(this).show(getSupportFragmentManager(), "timer");
  }

  @Override
  public void onSaved(SavedCombo savedCombo) {
    Log.d(TAG, "onSaved: called");

    FavoritesFragment favoritesFragment =
        (FavoritesFragment)
            getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":1");

    favoritesFragment.updateList(savedCombo);

  }

  @Override
  public void startCountDownTimer(Integer minutes) {
    SoundGridFragment soundGridFragment =
        (SoundGridFragment)
            getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
    soundGridFragment.startCountDownTimer(minutes);
  }
}
