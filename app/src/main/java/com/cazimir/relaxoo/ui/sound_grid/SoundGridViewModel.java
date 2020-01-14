package com.cazimir.relaxoo.ui.sound_grid;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.os.EnvironmentCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.Sound;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class SoundGridViewModel extends ViewModel {

  private static final String TAG = "SoundGridViewModel";
  private List<Sound> sounds = new ArrayList<>();
  private MutableLiveData<List<Sound>> soundsLiveData = new MutableLiveData<>();
  private List<Sound> playingSounds = new ArrayList<>();
  private MutableLiveData<List<Sound>> playingSoundsLiveData = new MutableLiveData<>();
  /** used to show notification in MainActivity to let user know that a sound is playing */
  private MutableLiveData<Boolean> isAtLeastOneSoundPlaying = new MutableLiveData<>();

  private MutableLiveData<Boolean> mutedLiveData = new MutableLiveData<>();

  public SoundGridViewModel() {
    try {
      fetchSounds();
    } catch (IOException e) {
      Log.d(TAG, "fetchSounds failed with: " + e.getMessage());
      e.printStackTrace();
    }
  }

  MutableLiveData<Boolean> mutedLiveData() {
    return mutedLiveData;
  }

  MutableLiveData<Boolean> isAtLeastOneSoundPlaying() {
    return isAtLeastOneSoundPlaying;
  }

  private void fetchSounds() throws IOException {




    // Create a storage reference for your app
    StorageReference rainFile = FirebaseStorage.getInstance().getReference().child("rain.ogg");



    final File file = File.createTempFile("rain", ".ogg");


    rainFile.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
      @Override
      public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
        Log.d(TAG, "onSuccess: called");
        Sound sound1 = Sound.newSound("Ring", R.drawable.ic_windy, file, false, 0.5f, false);

        sounds = new ArrayList<>();
        sounds.addAll(Arrays.asList(sound1));
        refreshSoundLiveData();

      }
    }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        Log.d(TAG, "onFailure: " + e.getMessage());
      }
    });


    //    // this call will be done to Firebase through Repository pattern.
    //
    //    Sound sound1 = Sound.newSound("Ring", R.drawable.ic_windy, R.raw.sound3, false, 0.5f,
    // true);
    //    Sound sound2 = Sound.newSound("Bird", R.drawable.ic_windy, R.raw.birds, false, 0.5f,
    // false);
    //    Sound sound3 = Sound.newSound("Wind", R.drawable.ic_windy, R.raw.graveyard_ambiance,
    // false, 0.5f, false);
    //    Sound sound4 = Sound.newSound("Other", R.drawable.ic_windy, R.raw.graveyard_ambiance,
    // false, 0.5f, false);
    //    Sound sound5 = Sound.newSound("Other2", R.drawable.ic_windy, R.raw.graveyard_ambiance,
    // false, 0.5f, true);
    //
    //    sounds = new ArrayList<>();
    //    sounds.addAll(Arrays.asList(sound1, sound2, sound3, sound4, sound5));
  }

  MutableLiveData<List<Sound>> sounds() {

    if (soundsLiveData.getValue() == null) {
      soundsLiveData.setValue(Collections.<Sound>emptyList());
    }

    return soundsLiveData;
  }

  MutableLiveData<List<Sound>> playingSounds() {

    if (playingSoundsLiveData.getValue() == null) {
      playingSoundsLiveData.setValue(Collections.<Sound>emptyList());
    }

    return playingSoundsLiveData;
  }

  private void refreshSoundLiveData() {
    Log.d(TAG, "refreshSoundLiveData: called: " + sounds.toString());
    soundsLiveData.setValue(sounds);
  }

  private void refreshPlayingSoundLiveData() {
    Log.d(TAG, "refreshPlayingSoundLiveData: called: size: " + playingSounds.size());
    playingSoundsLiveData.setValue(playingSounds);
  }

  void addToSounds(List<Sound> sounds) {
    this.sounds = sounds;
    refreshSoundLiveData();
  }

  void updateSoundList(int soundPoolId, int streamId) {

    boolean atLeastOneIsPlaying = false;

    for (Sound sound : sounds) {

      if (sound.soundPoolId() == soundPoolId) {
        sounds.set(
            sounds.indexOf(sound),
            Sound.SoundBuilder.aSound()
                .withSoundPoolId(soundPoolId)
                .withStreamId(streamId)
                .withName(sound.name())
                .withDrawable(sound.drawable())
                .withFile(sound.file())
                .withPlaying(!sound.isPlaying())
                .withVolume(sound.volume())
                .withPro(sound.pro())
                .build());

        break;
      }
    }

    // TODO: 08-Jan-20 update playing sounds reactively as a result of sounds beeing updated
    playingSounds.clear();

    for (Sound sound2 : sounds) {

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
