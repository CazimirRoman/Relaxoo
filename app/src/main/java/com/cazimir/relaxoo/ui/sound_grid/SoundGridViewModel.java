package com.cazimir.relaxoo.ui.sound_grid;

import android.media.SoundPool;
import android.os.Environment;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

import static android.media.AudioManager.STREAM_MUSIC;

public class SoundGridViewModel extends ViewModel {

  private static final String TAG = "SoundGridViewModel";
  private static final int MAX_SOUNDS = 5;
  private ArrayList<Sound> allSounds = new ArrayList<>();
  private MutableLiveData<ArrayList<Sound>> soundsLiveData = new MutableLiveData<>();
  private List<Sound> playingSounds = new ArrayList<>();
  private MutableLiveData<List<Sound>> playingSoundsLiveData = new MutableLiveData<>();
  /** used to show notification in MainActivity to let user know that a sound is playing */
  private MutableLiveData<Boolean> isAtLeastOneSoundPlaying = new MutableLiveData<>();

  private MutableLiveData<Boolean> mutedLiveData = new MutableLiveData<>();
  private boolean soundsFetched = false;
  private SoundPool soundPool;

  MutableLiveData<Boolean> mutedLiveData() {
    return mutedLiveData;
  }

  MutableLiveData<Boolean> isAtLeastOneSoundPlaying() {
    return isAtLeastOneSoundPlaying;
  }

  void fetchSounds() {

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

    if (soundsLiveData.getValue() == null) {
      soundsLiveData.setValue(new ArrayList<>());
    }

    return soundsLiveData;
  }

  MutableLiveData<List<Sound>> playingSounds() {

    if (playingSoundsLiveData.getValue() == null) {
      playingSoundsLiveData.setValue(Collections.emptyList());
    }

    return playingSoundsLiveData;
  }

  private void refreshSoundLiveData() {
    Log.d(TAG, "refreshSoundLiveData: called: " + allSounds.toString());
    soundsLiveData.setValue(allSounds);
  }

  private void refreshPlayingSoundLiveData() {
    Log.d(TAG, "refreshPlayingSoundLiveData: called: size: " + playingSounds.size());
    playingSoundsLiveData.setValue(playingSounds);
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

    isAtLeastOneSoundPlaying.setValue(atLeastOneIsPlaying);

    refreshSoundLiveData();
    refreshPlayingSoundLiveData();

    Log.d(TAG, "updateSoundList: atLeastOneIsPlaying: " + atLeastOneIsPlaying);
  }

  void updateMuteLiveData(Boolean muted) {
    mutedLiveData.setValue(muted);
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
}
