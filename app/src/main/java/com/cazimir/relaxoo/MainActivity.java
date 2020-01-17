package com.cazimir.relaxoo;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.cazimir.relaxoo.adapter.PagerAdapter;
import com.cazimir.relaxoo.dialog.DeleteConfirmationDialog;
import com.cazimir.relaxoo.dialog.OnTimerDialogCallback;
import com.cazimir.relaxoo.dialog.SaveToFavoritesDialog;
import com.cazimir.relaxoo.dialog.TimerDialog;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.model.Sound;
import com.cazimir.relaxoo.ui.favorites.FavoritesFragment;
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback;
import com.cazimir.relaxoo.ui.sound_grid.OnFavoriteSaved;
import com.cazimir.relaxoo.ui.sound_grid.SoundGridFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity
    implements OnActivityCallback, OnFavoriteSaved, OnTimerDialogCallback, OnFavoriteDeleted {

  private static final String TAG = "MainActivity";
  private static final String FAVORITE_FRAGMENT = ":1";
  private static final String SOUND_GRID_FRAGMENT = ":0";

  private static final String CHANNEL_WHATEVER = "" + "";
  private static int NOTIFY_ID = 1337;
  private NotificationManager notificationManager;
  private int previousColor = R.color.colorPrimary;
  private int nextColor = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);
    ViewPager pager = findViewById(R.id.pager);
    pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
    setupNotifications();
    startColorChangeAnimation();

      Dexter.withActivity(this)
              .withPermissions(
                      Manifest.permission.WRITE_EXTERNAL_STORAGE,
                      Manifest.permission.READ_EXTERNAL_STORAGE
              ).withListener(new MultiplePermissionsListener() {
          @Override
          public void onPermissionsChecked(MultiplePermissionsReport report) {
              Log.d(TAG, "onPermissionsChecked: " + report);

              File folder = Environment.getExternalStoragePublicDirectory("Relaxoo");

              Log.d("Files", "Path: " + folder);
              File directory = new File(folder.getAbsolutePath());
              File[] files = directory.listFiles();
              Log.d("Files", "Size: " + files.length);
              for (int i = 0; i < files.length; i++) {
                  Log.d("Files", "FileName:" + files[i].getName());
              }

          }

          @Override
          public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
              Log.d(TAG, "onPermissionRationaleShouldBeShown: called");

              /* ... */
          }
      }).check();






  }

  private void startColorChangeAnimation() {

    final LinearLayout parentLayout = findViewById(R.id.parentLayout);

    Timer timer = new Timer();
    // Set the schedule function
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {

            runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {

                    nextColor = getRandomColor();

                    Log.d(
                        TAG,
                        String.format(
                            "run: called. previousColor: %s and nextColor: %s",
                            previousColor, nextColor));

                    int duration = 1500;
                    ObjectAnimator animator =
                        ObjectAnimator.ofObject(
                                parentLayout,
                                "backgroundColor",
                                new ArgbEvaluator(),
                                previousColor,
                                nextColor)
                            .setDuration(duration);

                    animator.addListener(
                        new AnimatorListenerAdapter() {
                          @Override
                          public void onAnimationEnd(Animator animation) {
                            previousColor = nextColor;
                          }
                        });

                    animator.start();
                  }
                });
          }
        },
        2000,
        10000);
  }

  private int getRandomColor() {
    Random random = new Random();
    return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
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
  public void showAddToFavoritesDialog(HashMap<Integer, Integer> playingSounds) {
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
  public void triggerCombo(SavedCombo savedCombo) {
    Log.d(
        TAG,
        "triggerCombo in MainActivity: called with: "
            + savedCombo.getSoundPoolParameters().toString());
    getSoundGridFragment().triggerCombo(savedCombo);
  }

  @Override
  public void showDeleteConfirmationDialog(int position) {
    new DeleteConfirmationDialog(position).show(getSupportFragmentManager(), "delete");
  }

  @Override
  public void saved(SavedCombo savedCombo) {
    Log.d(TAG, "saved: called");
    getFavoriteFragment().updateList(savedCombo);
  }

  private FavoritesFragment getFavoriteFragment() {
    return (FavoritesFragment)
        getSupportFragmentManager()
            .findFragmentByTag("android:switcher:" + R.id.pager + FAVORITE_FRAGMENT);
  }

  private SoundGridFragment getSoundGridFragment() {
    return (SoundGridFragment)
        getSupportFragmentManager()
            .findFragmentByTag("android:switcher:" + R.id.pager + SOUND_GRID_FRAGMENT);
  }

  @Override
  public void startCountDownTimer(Integer minutes) {
    getSoundGridFragment().startCountDownTimer(minutes);
  }

  @Override
  public void deleted(int position) {
    getFavoriteFragment().deleteFavorite(position);
  }

  @Override
  public void showBottomDialog() {
    BottomSheetDialog dialog = new BottomSheetDialog(this);
    final View form = getLayoutInflater().inflate(R.layout.dialog_bottom, null);

    //    TextView textView = form.findViewById(R.id.proIcon);
    //    textView.setOnClickListener(new View.OnClickListener() {
    //      @Override
    //      public void onClick(View v) {
    //        showToast("clicken on Textview in Bottombar");
    //      }
    //    });

    dialog.setContentView(form);
    dialog.show();
  }

  @Override
  public void showIfFileStillThere(List<Sound> sounds) {

    if(!sounds.isEmpty()){
        Log.d(TAG, "stored File: " + sounds.get(0).filePath());

    }









  }
}
