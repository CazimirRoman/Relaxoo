package com.cazimir.relaxoo.ui.sound_grid;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.Sound;

import java.util.ArrayList;
import java.util.List;

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
    fetchSounds();
  }

  public MutableLiveData<Boolean> mutedLiveData() {
    return mutedLiveData;
  }

  public MutableLiveData<Boolean> isAtLeastOneSoundPlaying() {
    return isAtLeastOneSoundPlaying;
  }

  void fetchSounds() {
    // this call will be done to Firebase

    Sound sound1 = Sound.newSound("Sound1", R.drawable.ic_windy, R.raw.sound1, false, 0.5f);
    Sound sound2 = Sound.newSound("Sound2", R.drawable.ic_windy, R.raw.sound3, false, 0.5f);
    Sound sound3 = Sound.newSound("Sound3", R.drawable.ic_windy, R.raw.sound3, false, 0.5f);

    if (!sounds.contains(sound1)) {
      sounds.add(sound1);
    }

    if (!sounds.contains(sound2)) {
      sounds.add(sound2);
    }

    if (!sounds.contains(sound3)) {
      sounds.add(sound3);
    }

    refreshSoundLiveData();
  }

  MutableLiveData<List<Sound>> sounds() {
    return soundsLiveData;
  }

  MutableLiveData<List<Sound>> playingSounds() {
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
                .build());

        break;
      }
    }

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

  public void updateMuteLiveData(Boolean muted) {
    mutedLiveData.setValue(muted);
  }
}
