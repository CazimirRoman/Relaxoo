package com.cazimir.relaxoo.ui.sound_grid;

import androidx.lifecycle.ViewModelProviders;

import android.media.SoundPool;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.adapter.GridBaseAdapter;
import com.cazimir.relaxoo.model.Sound;

import java.util.ArrayList;
import java.util.function.ToDoubleBiFunction;

import static android.media.AudioManager.STREAM_MUSIC;

public class SoundGridFragment extends Fragment implements ISoundGridFragment {

  private static final String TAG = "SoundGridFragment";
  private static final int MAX_SOUNDS = 5;
  private GridView gridView;
  private GridBaseAdapter gridBaseAdapter;
  private int sound1;
  //TODO needs to be replaced with arraylist with playing sounds
  int streamId;
  private SoundPool soundPool;
  private SoundGridViewModel viewModel;

  public static SoundGridFragment newInstance() {
    return new SoundGridFragment();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.sound_list_fragment, container, false);
    gridView = view.findViewById(R.id.gridView);

    // will be fetched from Firebase servers via viewmodel
    ArrayList<Sound> sounds = new ArrayList<>();
    sounds.add(new Sound("Sound1", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound2", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound3", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound4", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));
    sounds.add(new Sound("Sound5", R.drawable.ic_home_black_24dp));

    gridBaseAdapter =
        new GridBaseAdapter(
            getContext(),
            sounds,
            new OnSoundClickListener() {
              @Override
              public void clicked(boolean playing) {
                handleClick(playing);
              }

              @Override
              public void volumeChange(int progress) {
                // TODO: 18.12.2019 refactor this float to string to double transformation
                Log.d(TAG, "volumeChange: called with volume: " + (double) progress / 100);
                soundPool.setVolume(
                    streamId,
                    Float.valueOf(String.valueOf((double) progress / 100)),
                    Float.valueOf(String.valueOf((double) progress / 100)));
              }
            });

    gridView.setAdapter(gridBaseAdapter);

    return view;
  }

  private void handleClick(boolean playing) {
    if (playing) {
      stopSound();
    } else {
      playSound();
    }

    //start playing sound in loop

    //show seekbar with volume and option to increase or decrease the volume

    //change icon to complete white instead of gray
    //show notification with option to stop and mute and if clicked redirect to application
    //change icon to 'stop' icon in upper action bar

  }

  private void stopSound() {
    Log.d(TAG, "stopSound: called");
    // just gets the first sound. need to implement logic to search array for sound to be stopped
    soundPool.stop(streamId);
  }

  private void playSound() {
    Log.d(TAG, "playSound: called");
    // save streamId to streaming sound array

    streamId = soundPool.play(sound1, 0.5f, 0.5f, 0, -1, 1);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    viewModel = ViewModelProviders.of(this).get(SoundGridViewModel.class);
    soundPool = new SoundPool(MAX_SOUNDS, STREAM_MUSIC, 0);
    sound1 = soundPool.load(getContext(), R.raw.sound1, 1);
  }
}
