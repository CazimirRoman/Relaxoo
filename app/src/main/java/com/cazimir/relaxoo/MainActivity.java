package com.cazimir.relaxoo;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.viewpager.widget.ViewPager;

import com.cazimir.relaxoo.adapter.PagerAdapter;
import com.cazimir.relaxoo.dialog.DeleteConfirmationDialog;
import com.cazimir.relaxoo.dialog.OnTimerDialogCallback;
import com.cazimir.relaxoo.dialog.SaveToFavoritesDialog;
import com.cazimir.relaxoo.dialog.TimerDialog;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.model.Sound;
import com.cazimir.relaxoo.ui.create_sound.CreateSoundFragment;
import com.cazimir.relaxoo.ui.create_sound.OnRecordingStarted;
import com.cazimir.relaxoo.ui.favorites.FavoritesFragment;
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback;
import com.cazimir.relaxoo.ui.sound_grid.OnFavoriteSaved;
import com.cazimir.relaxoo.ui.sound_grid.SoundGridFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

public class MainActivity extends FragmentActivity
        implements OnActivityCallback,
        OnFavoriteSaved,
        OnTimerDialogCallback,
        OnFavoriteDeleted,
        OnRecordingStarted {

  private static final String TAG = "MainActivity";
  private static final String FAVORITE_FRAGMENT = ":1";
  private static final String SOUND_GRID_FRAGMENT = ":0";
  private static final String CREATE_SOUND_FRAGMENT = ":2";
  private static final String CHANNEL_WHATEVER = "" + "";
  private static final int RECORDING_REQ_CODE = 0;
  private static int NOTIFY_ID = 1337;
  private NotificationManager notificationManager;
  private int previousColor = R.color.colorPrimary;
  private int nextColor = 0;
  private MutableLiveData<Boolean> areWritePermissionsGranted = new MutableLiveData<>();
  private MutableLiveData<Boolean> isSoundGridFragmentStarted = new MutableLiveData<>();
  private MergePermissionFragmentStarted mergePermissionFragmentStarted;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    areWritePermissionsGranted.setValue(false);
    isSoundGridFragmentStarted.setValue(false);

    mergePermissionFragmentStarted = new MergePermissionFragmentStarted.Builder().build();

    setContentView(R.layout.main_activity);
    ViewPager pager = findViewById(R.id.pager);
    pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
    setupNotifications();
    startColorChangeAnimation();
    checkPermissions();

    final MediatorLiveData<MergePermissionFragmentStarted> result = new MediatorLiveData<>();

    result.addSource(
            areWritePermissionsGranted,
            permissionsGranted -> {
              Log.d(TAG, "permissions granted: " + permissionsGranted);
              mergePermissionFragmentStarted =
                      MergePermissionFragmentStarted.withPermissionGranted(
                              mergePermissionFragmentStarted, permissionsGranted);
              result.setValue(mergePermissionFragmentStarted);
            });

    result.addSource(
            isSoundGridFragmentStarted,
            fragmentStarted -> {
              Log.d(TAG, "fragment recordingStarted: " + fragmentStarted);
              mergePermissionFragmentStarted =
                      MergePermissionFragmentStarted.withFragmentInstantiated(
                              mergePermissionFragmentStarted, fragmentStarted);
              result.setValue(mergePermissionFragmentStarted);
            });

    result.observe(
            this,
            mergePermissionFragmentStarted -> {
              Log.d(
                      TAG,
                      "onChanged() called with: mergePermissionFragmentStarted: "
                              + mergePermissionFragmentStarted.toString());

              if (mergePermissionFragmentStarted.isFragmentStarted()
                      && mergePermissionFragmentStarted.isPermissionsGranted()) {

                if (!getSoundGridFragment().soundsAlreadyFetched()) {
                  Log.d(
                          TAG, "sounds already fetched: " + getSoundGridFragment().soundsAlreadyFetched());
                  getSoundGridFragment().fetchSounds();
                }
              }
            });
  }

  private void checkPermissions() {
    Dexter.withActivity(this)
            .withPermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(
                    new MultiplePermissionsListener() {
                      @Override
                      public void onPermissionsChecked(MultiplePermissionsReport report) {
                        Log.d(
                                TAG,
                                "onPermissionsChecked: " + report.getGrantedPermissionResponses().toString());
                        if (report.areAllPermissionsGranted()) {
                          areWritePermissionsGranted.setValue(true);
                        }
                      }

                      @Override
                      public void onPermissionRationaleShouldBeShown(
                              List<PermissionRequest> permissions, PermissionToken token) {
                        Log.d(TAG, "onPermissionRationaleShouldBeShown: called");

                        /* ... */
                      }
                    })
            .check();
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

  private CreateSoundFragment getCreateSoundFragment() {
    return (CreateSoundFragment)
            getSupportFragmentManager()
                    .findFragmentByTag("android:switcher:" + R.id.pager + CREATE_SOUND_FRAGMENT);
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

    if (!sounds.isEmpty()) {
      Log.d(TAG, "stored File: " + sounds.get(0).getFilePath());
    }
  }

  @Override
  public void soundsFetchedAndSaved() {
    Log.d(TAG, "soundsFetchedAndSaved() called");
    checkIfFileIsThere();
  }

  @Override
  public void soundGridFragmentStarted() {
    isSoundGridFragmentStarted.setValue(true);
  }

  private void checkIfFileIsThere() {

    Log.d(TAG, "checkIfFileIsThere() called with: ");
  }

  @Override
  public void recordingStarted() {
    checkRecordingPermission();
  }

  private void checkRecordingPermission() {
    Dexter.withActivity(this)
            .withPermission(Manifest.permission.RECORD_AUDIO)
            .withListener(
                    new PermissionListener() {
                      @Override
                      public void onPermissionGranted(PermissionGrantedResponse response) {
                        startRecordingActivity();
                      }

                      @Override
                      public void onPermissionDenied(PermissionDeniedResponse response) {
                        showToast("You need to grant recording permissions to record your own sound");
                      }

                      @Override
                      public void onPermissionRationaleShouldBeShown(
                              PermissionRequest permission, PermissionToken token) {
                        /* ... */
                      }
                    })
            .check();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 0) {
      if (resultCode == RESULT_OK) {
        showToast("Sound saved to file");

        getCreateSoundFragment().updateList();

        // Great! User has recorded and saved the audio file
      } else if (resultCode == RESULT_CANCELED) {
        showToast("User canceled!");
      }
    }
  }

  private void startRecordingActivity() {

    File ownSoundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/own_sounds");
    if (!ownSoundsFolder.exists()) {
      ownSoundsFolder.mkdir();
    }

    String fileName = new SimpleDateFormat("yyyyMMddHHmm'.wav'").format(new Date());
    String filePath = Environment.getExternalStorageDirectory() + "/Relaxoo/own_sounds/" + fileName;

    int color = getResources().getColor(R.color.colorPrimaryDark);
    int requestCode = RECORDING_REQ_CODE;
    AndroidAudioRecorder.with(this)
            // Required
            .setFilePath(filePath)
            .setColor(color)
            .setRequestCode(requestCode)
            // Optional
            .setSource(AudioSource.MIC)
            .setChannel(AudioChannel.STEREO)
            .setSampleRate(AudioSampleRate.HZ_48000)
            .setAutoStart(false)
            .setKeepDisplayOn(true)
            // Start recording
            .record();
  }
}
