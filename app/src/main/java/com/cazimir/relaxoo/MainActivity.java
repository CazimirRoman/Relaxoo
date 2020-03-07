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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.cazimir.relaxoo.adapter.PagerAdapter;
import com.cazimir.relaxoo.dialog.BottomRecordingDialogFragment;
import com.cazimir.relaxoo.dialog.DeleteConfirmationDialog;
import com.cazimir.relaxoo.dialog.FavoriteDeleted;
import com.cazimir.relaxoo.dialog.OnDeleted;
import com.cazimir.relaxoo.dialog.OnTimerDialogCallback;
import com.cazimir.relaxoo.dialog.ProBottomDialogFragment;
import com.cazimir.relaxoo.dialog.SaveToFavoritesDialog;
import com.cazimir.relaxoo.dialog.TimerDialog;
import com.cazimir.relaxoo.model.Recording;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.model.Sound;
import com.cazimir.relaxoo.shared.MainActivityViewModel;
import com.cazimir.relaxoo.ui.create_sound.CreateSoundFragment;
import com.cazimir.relaxoo.ui.create_sound.OnRecordingStarted;
import com.cazimir.relaxoo.ui.favorites.FavoritesFragment;
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback;
import com.cazimir.relaxoo.ui.sound_grid.OnFavoriteSaved;
import com.cazimir.relaxoo.ui.sound_grid.SoundGridFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

import static com.android.billingclient.api.BillingClient.newBuilder;

public class MainActivity extends FragmentActivity
        implements OnActivityCallback,
        OnFavoriteSaved,
        OnTimerDialogCallback,
        OnDeleted,
        OnRecordingStarted,
        PurchasesUpdatedListener {

  private static final String TAG = "MainActivity";
  private static final String FAVORITE_FRAGMENT = ":1";
  private static final String SOUND_GRID_FRAGMENT = ":0";
  private static final String CREATE_SOUND_FRAGMENT = ":2";
  private static final String CHANNEL_WHATEVER = "" + "";
  private static final int RECORDING_REQ_CODE = 0;
  private static int NOTIFY_ID = 1337;

  @BindView(R.id.splash)
  AppCompatImageView splash;

  @BindView(R.id.main_layout)
  LinearLayout mainLayout;

  private NotificationManager notificationManager;

  private MutableLiveData<Boolean> areWritePermissionsGranted = new MutableLiveData<>();
  private MutableLiveData<Boolean> isSoundGridFragmentStarted = new MutableLiveData<>();
  private MergePermissionFragmentStarted mergePermissionFragmentStarted;
  private AdView adView;

  private BillingClient billingClient;
  private List<String> skuList = Arrays.asList("remove_ads");
  private MainActivityViewModel mainActivityViewModel;
  private TimerDialog timerdialog;
  private SoundGridFragment soundGridFragment;
  private ViewPager viewPager;
  private Runnable runnable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

    areWritePermissionsGranted.setValue(false);
    isSoundGridFragmentStarted.setValue(false);

    mergePermissionFragmentStarted = new MergePermissionFragmentStarted.Builder().build();

    setContentView(R.layout.main_activity);
    ButterKnife.bind(this);


    shouldShowSplash();
    this.viewPager = findViewById(R.id.pager);
    viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
    TabLayout tabLayout = findViewById(R.id.tabDots);
    tabLayout.setupWithViewPager(viewPager, true);
    setupNotifications();
    startColorChangeAnimation();
    checkPermissions();
    launchAds();
    setupBillingClient();

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

  private void shouldShowSplash() {
    if (mainActivityViewModel.getSplashShown()) {
      hideSplash();
    }
  }

  private void setupBillingClient() {
    billingClient = newBuilder(this).enablePendingPurchases().setListener(this).build();

    billingClient.startConnection(
            new BillingClientStateListener() {
              @Override
              public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                  Log.d(TAG, "onBillingSetupFinished() called with: success!");
                  loadAllSKUs();
                }
              }

              @Override
              public void onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected() called with: failed");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
              }
            });
  }

  private void loadAllSKUs() {
    if (billingClient.isReady()) {
      SkuDetailsParams skuDetailsParams =
              SkuDetailsParams.newBuilder()
                      .setSkusList(skuList)
                      .setType(BillingClient.SkuType.INAPP)
                      .build();

      billingClient.querySkuDetailsAsync(
              skuDetailsParams,
              (billingResult, skuDetailsList) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && !skuDetailsList.isEmpty()) {
                  for (SkuDetails skuDetail : skuDetailsList) {
                    Toast.makeText(this, skuDetail.getDescription(), Toast.LENGTH_SHORT).show();
                  }
                } else {
                  Log.d(TAG, "loadAllSKUs() called with: skuDetailsList is empty");
                }
              });
    } else {
      Log.d(TAG, "loadAllSKUs() called with: billingClient is not ready");
    }
  }

  private void launchAds() {

    this.adView = findViewById(R.id.ad_view);
//    if (BuildConfig.DEBUG) {
//      adView.setAdUnitId(getResources().getString(R.string.ad_test));
//    } else {
//      adView.setAdUnitId(getResources().getString(R.string.ad_prod));
//    }

    AdRequest adRequest1 = mainActivityViewModel.getAdRequest();

    if (adRequest1 == null) {
      mainActivityViewModel.setAdRequest(new AdRequest.Builder().build());
      adView.loadAd(adRequest1);
    }
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

    final FrameLayout parentLayout = findViewById(R.id.parentLayout);

    parentLayout.setBackgroundColor(mainActivityViewModel.getNextColor());

    mainActivityViewModel
            .getTimer()
            .scheduleAtFixedRate(
                    new TimerTask() {
                      @Override
                      public void run() {

                        runOnUiThread(
                                new Runnable() {
                                  @Override
                                  public void run() {

                                    mainActivityViewModel.setNextColor(getRandomColor());

                                    Log.d(
                                            TAG,
                                            String.format(
                                                    "run: called. previousColor: %s and nextColor: %s",
                                                    mainActivityViewModel.getPreviousColor(),
                                                    mainActivityViewModel.getNextColor()));

                                    int duration = 1500;
                                    ObjectAnimator animator =
                                            ObjectAnimator.ofObject(
                                                    parentLayout,
                                                    "backgroundColor",
                                                    new ArgbEvaluator(),
                                                    mainActivityViewModel.getPreviousColor(),
                                                    mainActivityViewModel.getNextColor())
                                                    .setDuration(duration);

                                    animator.addListener(
                                            new AnimatorListenerAdapter() {
                                              @Override
                                              public void onAnimationEnd(Animator animation) {
                                                mainActivityViewModel.setPreviousColor(
                                                        mainActivityViewModel.getNextColor());
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
    if (timerdialog == null) {
      timerdialog = new TimerDialog(this);
    }

    timerdialog.show(getSupportFragmentManager(), "timer");
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
  public void showDeleteConfirmationDialog(OnDeleted deleted) {
    new DeleteConfirmationDialog(deleted)
            .show(
                    getSupportFragmentManager(),
                    deleted instanceof FavoriteDeleted ? "favDeleted" : "recDeleted");
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
    soundGridFragment.startCountDownTimer(minutes);
  }

  //  @Override
  //  public void favoriteDeleted(int position) {
  //    getFavoriteFragment().deleteFavorite(position);
  //  }

  @Override
  public void showBottomDialogForPro() {
    //    BottomSheetDialogFragment dialog = new BottomSheetDialogFragment();
    //
    //      final View form = getLayoutInflater().inflate(R.layout.dialog_bottom_unlock_pro, null);
    //
    //      //    TextView textView = form.findViewById(R.id.proIcon);
    //      //    textView.setOnClickListener(new View.OnClickListener() {
    //      //      @Override
    //      //      public void onClick(View v) {
    //      //        showToast("clicken on Textview in Bottombar");
    //      //      }
    //      //    });
    //
    //      dialog.setContentView(form);
    //      dialog.show();

    new ProBottomDialogFragment().show(getSupportFragmentManager(), "pro");
  }

  @Override
  public void soundsFetchedAndSaved() {
    Log.d(TAG, "soundsFetchedAndSaved() called");
    checkIfFileIsThere();
  }

  @Override
  public void soundGridFragmentStarted() {
    Log.d(TAG, "soundGridFragmentStarted() called");
    isSoundGridFragmentStarted.setValue(true);
    this.soundGridFragment = getSoundGridFragment();
  }

  @Override
  public void hideSplash() {
    splash.setVisibility(View.GONE);
    mainLayout.setVisibility(View.VISIBLE);
    this.mainActivityViewModel.splashShown();
  }

  @Override
  public void removeAds() {

    //      billingClient

    //    adBannerLayout.removeView(adView);
  }

  @Override
  public void deleteRecording(@NotNull Recording recording) {
    getCreateSoundFragment().deleteRecording(recording);
  }

  @Override
  public void renameRecording(@NotNull Recording recording, @NotNull String newName) {
    getCreateSoundFragment()
            .renameRecording(recording, newName);
  }

  @Override
  public void pinToDashBoardActionCalled(@NotNull Sound sound) {
    getSoundGridFragment().addRecordingToSoundPool(sound);
    scrollViewPager();
  }

  private void checkIfFileIsThere() {

    Log.d(TAG, "checkIfFileIsThere() called with: ");
  }

  @Override
  public void recordingStarted() {
    checkRecordingPermission();
  }

  @Override
  public void showBottomDialogForRecording(Recording recording) {
    new BottomRecordingDialogFragment(recording, this)
            .show(getSupportFragmentManager(), "rec_bottom");
  }

  private void scrollViewPager() {
    viewPager.setCurrentItem(0);
    getSoundGridFragment().scrollToBottom();
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

    String fileName = new SimpleDateFormat("yyyyMMddHHmmss'.wav'").format(new Date());
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

  @Override
  public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
  }
}
