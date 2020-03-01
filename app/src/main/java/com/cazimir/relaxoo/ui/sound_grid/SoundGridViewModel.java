package com.cazimir.relaxoo.ui.sound_grid;

import android.media.SoundPool;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cazimir.relaxoo.dialog.TimerDialog;
import com.cazimir.relaxoo.model.Sound;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.media.AudioManager.STREAM_MUSIC;

public class SoundGridViewModel extends ViewModel {

  private static final String TAG = "SoundGridViewModel";
  private static final int MAX_SOUNDS = 5;
  MutableLiveData<Integer> soundsLoadedToSoundPool = new MutableLiveData<>();
  MutableLiveData<String> _timerText = new MutableLiveData<>();
  MutableLiveData<Boolean> _timerFinished = new MutableLiveData<>();
  private ArrayList<Sound> allSounds = new ArrayList<>();
  private MutableLiveData<ArrayList<Sound>> _soundsLiveData = new MutableLiveData<>();
  private List<Sound> playingSounds = new ArrayList<>();
  private MutableLiveData<List<Sound>> _playingSoundsLiveData = new MutableLiveData<>();
  /**
   * used to show notification in MainActivity to let user know that a sound is playing
   */
  private MutableLiveData<Boolean> _isAtLeastOneSoundPlaying = new MutableLiveData<>();

  private MutableLiveData<Boolean> _mutedLiveData = new MutableLiveData<>();
  private boolean soundsFetched = false;
  private SoundPool soundPool;
  private MutableLiveData<Boolean> _timerEnabled = new MutableLiveData<>();
  private CountDownTimer countDownTimer;

  public SoundGridViewModel() {
    soundsLoadedToSoundPool.setValue(0);
  }

  public MutableLiveData<Boolean> timerFinished() {
    return _timerFinished;
  }

  public MutableLiveData<String> timerText() {
    return _timerText;
  }

  public MutableLiveData<Boolean> timerEnabled() {
    return _timerEnabled;
  }

  MutableLiveData<Boolean> mutedLiveData() {
    return _mutedLiveData;
  }

  MutableLiveData<Boolean> isAtLeastOneSoundPlaying() {
    return _isAtLeastOneSoundPlaying;
  }

  void fetchSounds() {

    // TODO: 01-Mar-20 Move to repository class so you can test

    Log.d(TAG, "fetchSounds: called");

    final ArrayList<Sound> soundsInFirebase = new ArrayList<>();

    // 1. check the Firebase DB for sounds

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference soundsRef = database.getReference("sounds");

    // check database for sounds

    // Read from the database
    soundsRef.addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot soundsSnapshot : dataSnapshot.getChildren()) {
                  Sound sound = soundsSnapshot.getValue(Sound.class);
                  if (sound != null) {
                    soundsInFirebase.add(0, sound);
                  }
                }

                if (soundsInFirebase.size() > 0) {
                  getAssetsFromFirebaseStorage(soundsInFirebase);
                }
              }

              @Override
              public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
              }
            });
  }

  public MutableLiveData<Integer> getSoundsLoadedToSoundPool() {
    return soundsLoadedToSoundPool;
  }

  private void getAssetsFromFirebaseStorage(ArrayList<Sound> sounds) {

    allSounds.clear();

    // check if files already downloaded locally

    File soundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/sounds");
    File logosFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/logos");

    if (!soundsFolder.exists()) {
      soundsFolder.mkdirs();
    }

    // check locally to see how many files there are
    File soundsDirectory = new File(soundsFolder.getAbsolutePath());
    File[] files = soundsDirectory.listFiles();

    // there are some sounds missing locally
    if (files == null || files.length < sounds.size()) {
      Log.d(TAG, "getAssetsFromFirebaseStorage: loading assets from firebase");
      // get sounds
      for (Sound sound : sounds) {

        StorageReference soundReference =
                FirebaseStorage.getInstance().getReference().child("sounds").child(sound.getFilePath());

        StorageReference imageReference =
                FirebaseStorage.getInstance().getReference().child("logos").child(sound.getLogoPath());

        if (!logosFolder.exists()) {
          logosFolder.mkdirs();
        }

        final File soundFile = new File(soundsFolder, sound.getFilePath());
        final File logoFile = new File(logosFolder, sound.getLogoPath());

        // download sound from Firebase
        soundReference
                .getFile(soundFile)
                .addOnSuccessListener(
                        soundSnapshot -> {
                          Log.d(TAG, "onSuccess: called");

                          // now download the image
                          imageReference
                                  .getFile(logoFile)
                                  .addOnSuccessListener(
                                          imageSnapshot -> {
                                            Log.d(TAG, "onSuccess: called");

                                            Sound fetchedSound =
                                                    Sound.SoundBuilder.aSound()
                                                            .withName(sound.getName())
                                                            .withLogo(logoFile.getPath())
                                                            .withPro(sound.isPro())
                                                            .withFilePath(soundFile.getPath())
                                                            .build();

                                            allSounds.addAll(Arrays.asList(fetchedSound));

                                            if (allSounds.size() == sounds.size()) {
                                              refreshSoundLiveData();
                                            }
                                          })
                                  .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.getMessage()));
                        })
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.getMessage()));
      }
    }
    // load all files from local storage
    else {

      Log.d(TAG, "getAssetsFromFirebaseStorage: loading assets from local storage");

      File logosDirectory = new File(logosFolder.getAbsolutePath());

      for (int i = 0; i < sounds.size(); i++) {
        Sound localSound =
                Sound.SoundBuilder.aSound()
                        .withName(sounds.get(i).getName())
                        .withLogo(logosDirectory + "/" + sounds.get(i).getLogoPath())
                        .withPro(sounds.get(i).isPro())
                        .withFilePath(soundsDirectory + "/" + sounds.get(i).getFilePath())
                        .build();

        allSounds.add(localSound);
      }

      refreshSoundLiveData();

      soundsFetched = true;
    }
  }

  MutableLiveData<ArrayList<Sound>> sounds() {

    if (_soundsLiveData.getValue() == null) {
      _soundsLiveData.setValue(new ArrayList<>());
    }

    return _soundsLiveData;
  }

  MutableLiveData<List<Sound>> playingSounds() {

    if (_playingSoundsLiveData.getValue() == null) {
      _playingSoundsLiveData.setValue(Collections.emptyList());
    }

    return _playingSoundsLiveData;
  }

  private void refreshSoundLiveData() {
    Log.d(TAG, "refreshSoundLiveData: called: " + allSounds.toString());
    _soundsLiveData.setValue(allSounds);
  }

  private void refreshPlayingSoundLiveData() {
    Log.d(TAG, "refreshPlayingSoundLiveData: called: size: " + playingSounds.size());
    _playingSoundsLiveData.setValue(playingSounds);
  }

  void addToSounds(ArrayList<Sound> sounds) {
    this.allSounds = sounds;
    refreshSoundLiveData();
  }

  void updateSoundList(int soundPoolId, int streamId) {

    boolean atLeastOneIsPlaying = false;

    for (Sound sound : allSounds) {

      if (sound.soundPoolId() == soundPoolId) {
        allSounds.set(
                allSounds.indexOf(sound),
                Sound.SoundBuilder.aSound()
                        .withSoundPoolId(soundPoolId)
                        .withStreamId(streamId)
                        .withName(sound.getName())
                        .withLogo(sound.getLogoPath())
                        .withFilePath(sound.getFilePath())
                        .withPlaying(!sound.isPlaying())
                        .withVolume(sound.volume())
                        .withPro(sound.isPro())
                        .build());

        break;
      }
    }

    // TODO: 08-Jan-20 update playing sounds reactively as a result of sounds beeing updated
    playingSounds.clear();

    for (Sound sound2 : allSounds) {

      if (sound2.isPlaying()) {
        atLeastOneIsPlaying = true;
        playingSounds.add(sound2);
      }
    }

    if (!atLeastOneIsPlaying) {
      playingSounds.clear();
    }

    _isAtLeastOneSoundPlaying.setValue(atLeastOneIsPlaying);

    refreshSoundLiveData();
    refreshPlayingSoundLiveData();

    Log.d(TAG, "updateSoundList: atLeastOneIsPlaying: " + atLeastOneIsPlaying);
  }

  void updateMuteLiveData(Boolean muted) {
    _mutedLiveData.setValue(muted);
  }

  public boolean getSoundsAlreadyFetched() {
    return soundsFetched;
  }

  public SoundPool createOrGetSoundPool() {
    Log.d(TAG, "createOrGetSoundPool: called");
    if (soundPool == null) {
      soundPool = new SoundPool(MAX_SOUNDS, STREAM_MUSIC, 0);
    }
    return soundPool;
  }

  public void addedSound() {
    soundsLoadedToSoundPool.setValue(soundsLoadedToSoundPool.getValue() + 1);
  }

  public void updateVolume(Sound sound, Float volume) {
    allSounds.set(allSounds.indexOf(sound), Sound.withVolume(sound, volume));
    _soundsLiveData.setValue(allSounds);
  }

  public CountDownTimer countDownTimer() {
    return countDownTimer;
  }

  public void setCountDownTimer(int minutes) {
    this.countDownTimer =
            new CountDownTimer(TimeUnit.MINUTES.toMillis(minutes), 1000) {

              public void onTick(long millisUntilFinished) {

                // updateLiveDataHere() observe from Fragment
                timerText()
                        .setValue(
                                String.format(
                                        "Sound%s will stop in "
                                                + TimerDialog.getCountTimeByLong(millisUntilFinished),
                                        playingSounds().getValue().size() > 1 ? "s" : ""));
              }

              public void onFinish() {
                // live data observe timer finished
                timerFinished().setValue(true);
              }
            }.start();

    timerFinished().setValue(false);
  }
}
