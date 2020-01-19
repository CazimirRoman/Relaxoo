package com.cazimir.relaxoo.ui.sound_grid;

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

public class SoundGridViewModel extends ViewModel {

  private static final String TAG = "SoundGridViewModel";
  private List<Sound> allSounds = new ArrayList<>();
  private MutableLiveData<List<Sound>> soundsLiveData = new MutableLiveData<>();
  private List<Sound> playingSounds = new ArrayList<>();
  private MutableLiveData<List<Sound>> playingSoundsLiveData = new MutableLiveData<>();
  /** used to show notification in MainActivity to let user know that a sound is playing */
  private MutableLiveData<Boolean> isAtLeastOneSoundPlaying = new MutableLiveData<>();

  private MutableLiveData<Boolean> mutedLiveData = new MutableLiveData<>();

  MutableLiveData<Boolean> mutedLiveData() {
    return mutedLiveData;
  }

  MutableLiveData<Boolean> isAtLeastOneSoundPlaying() {
    return isAtLeastOneSoundPlaying;
  }

  void fetchSounds() {

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
                  getAssetsFromStorage(soundsInFirebase);
                }
              }

              @Override
              public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
              }
            });

    // check if number of sound files on local storage matches the number of children in the
    // database
    File folder = Environment.getExternalStoragePublicDirectory("Relaxoo");

    Log.d("Files", "Path: " + folder);
    File directory = new File(folder.getAbsolutePath());
    File[] files = directory.listFiles();
    Log.d("Files", "Size: " + files.length);

    for (File file : files) {
      Log.d("Files", "FileName:" + file.getName());
    }

    // if less sounds are in the local storage and more on the server then download the missing one

    // get reference to storage
    // download sound file to ext storage (rain.ogg)
    // download logo file to ext storage

  }

  private void getAssetsFromStorage(ArrayList<Sound> sounds) {

    File folder = Environment.getExternalStoragePublicDirectory("Relaxoo");
    if (!folder.exists()) {
      folder.mkdir();
    }
    // get sounds

    for (Sound sound : sounds) {

      ArrayList<Sound> tempSounds = new ArrayList<>();

      StorageReference reference =
              FirebaseStorage.getInstance().getReference().child("sounds").child(sound.getFilePath());

      final File myFile = new File(folder, sound.getFilePath());

      reference
              .getFile(myFile)
              .addOnSuccessListener(
                      taskSnapshot -> {
                        Log.d(TAG, "onSuccess: called");

                        Sound ring =
                                Sound.SoundBuilder.aSound()
                                        .withName(sound.getName())
                                        .withLogo("logo.url")
                                        .withFilePath(myFile.getPath())
                                        .build();

                        allSounds.addAll(Arrays.asList(ring));
                        refreshSoundLiveData();
                      })
              .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.getMessage()));
    }
  }

  MutableLiveData<List<Sound>> sounds() {

    if (soundsLiveData.getValue() == null) {
      soundsLiveData.setValue(Collections.emptyList());
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

  void addToSounds(List<Sound> sounds) {
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
}
