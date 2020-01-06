package com.cazimir.relaxoo.ui.sound_grid;

import android.app.TimePickerDialog;
import android.content.Context;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.adapter.GridAdapter;
import com.cazimir.relaxoo.model.Sound;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static android.media.AudioManager.STREAM_MUSIC;

public class SoundGridFragment extends Fragment implements ISoundGridFragment {

  private static final String TAG = "SoundGridFragment";
  private static final int MAX_SOUNDS = 5;
  private GridAdapter gridArrayAdapter;
  private SoundPool soundPool;
  private SoundGridViewModel viewModel;
  private boolean once;

  private GridView gridView;
  private ImageButton muteButton;
  private ImageButton randomButton;
  private ImageButton playStopButton;
  private ImageButton saveFavorites;
  private ImageButton setTimer;
  private TextView timerText;

  private boolean timerEnabled;
  private CountDownTimer countDownTimer;
  private boolean muted;
  private OnActivityNeededCallback activityCallback;

  TimePickerDialog.OnTimeSetListener timerPickerListener =
      new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hours, int minutes) {

          toggleCountdownTimer(hours, minutes);

          activityCallback.showToast("Timer set to: " + hours + " " + minutes);
        }
      };

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
    playStopButton = view.findViewById(R.id.play_button);
    muteButton = view.findViewById(R.id.mute_button);
    randomButton = view.findViewById(R.id.random_button);
    saveFavorites = view.findViewById(R.id.save_fav_button);
    setTimer = view.findViewById(R.id.set_timer_button);
    timerText = view.findViewById(R.id.timerText);

    return view;
  }

  private void playStopSound(int soundPoolId, boolean playing, int streamId) {
    Log.d(TAG, "playStopSound: called");
    if (playing) {
      soundPool.stop(streamId);
      viewModel.updateSoundList(soundPoolId, -1);
    } else {
      int newStreamId = soundPool.play(soundPoolId, 0.5f, 0.5f, 0, -1, 1);
      viewModel.updateSoundList(soundPoolId, newStreamId);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    activityCallback = (OnActivityNeededCallback) context;
  }

  @Override
  public void stopAllSounds() {

    List<Sound> list = new CopyOnWriteArrayList<>(viewModel.playingSounds().getValue());

    // TODO: 22.12.2019 try to remove some of the for loops, use guava or something
    for (Sound sound : list) {
      soundPool.stop(sound.streamId());
      viewModel.updateSoundList(sound.soundPoolId(), -1);
    }
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    viewModel = ViewModelProviders.of(this).get(SoundGridViewModel.class);
    soundPool = new SoundPool(MAX_SOUNDS, STREAM_MUSIC, 0);

    soundPool.setOnLoadCompleteListener(
        new SoundPool.OnLoadCompleteListener() {
          @Override
          public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            Log.d(TAG, "onLoadComplete: " + sampleId);
            configureListeners();
          }
        });

    // region Observers

    // change icon to unmute
    viewModel
        .mutedLiveData()
        .observe(
            getViewLifecycleOwner(),
            new Observer<Boolean>() {
              @Override
              public void onChanged(Boolean muted) {
                if (muted) {
                  muteButton.setImageDrawable(
                      getActivity().getResources().getDrawable(R.drawable.ic_mute_off));

                } else {
                  muteButton.setImageDrawable(
                      getActivity().getResources().getDrawable(R.drawable.ic_mute_on));
                }
              }
            });

    // listen if at least one sound is playing to show notification to user
    viewModel
        .isAtLeastOneSoundPlaying()
        .observe(
            getViewLifecycleOwner(),
            new Observer<Boolean>() {
              @Override
              public void onChanged(Boolean playing) {

                if (playing) {
                  activityCallback.showNotification();
                } else {
                  activityCallback.hideNotification();
                }
              }
            });

    // listen for changes to the sound lists live data object to set the adapter for the gridview
    // along with the callback methods (clicked & volume changed)
    viewModel
        .sounds()
        .observe(
            // TODO: 19.12.2019 move in a separate file or inner class
            getViewLifecycleOwner(),
            new Observer<List<Sound>>() {
              @Override
              public void onChanged(List<Sound> sounds) {

                Log.d(TAG, "Data observer changed: " + sounds);

                gridArrayAdapter =
                    new GridAdapter(
                        getContext(),
                        sounds,
                        new OnSoundClickListener() {
                          @Override
                          public void clicked(int soundId, boolean playing, int streamId) {
                            playStopSound(soundId, playing, streamId);
                          }

                          @Override
                          public void volumeChange(int streamId, int progress) {
                            // TODO: 18.12.2019 refactor this float to string to double
                            // transformation
                            Log.d(
                                TAG,
                                "volumeChange: called with volume: " + (double) progress / 100);
                            soundPool.setVolume(
                                streamId,
                                Float.valueOf(String.valueOf((double) progress / 100)),
                                Float.valueOf(String.valueOf((double) progress / 100)));
                          }
                        });

                gridView.setAdapter(gridArrayAdapter);
                // TODO: 19.12.2019 get rid of this!!!
                if (!once) {
                  loadToSoundPool(sounds);
                }
                once = true;
              }
            });

    // listen to the playing sounds live data object to change the play stop drawable icon on top
    viewModel
        .playingSounds()
        .observe(
            getViewLifecycleOwner(),
            new Observer<List<Sound>>() {
              @Override
              public void onChanged(List<Sound> playingList) {
                if (playingList.isEmpty()) {
                  playStopButton.setImageDrawable(
                      getActivity().getResources().getDrawable(R.drawable.ic_play));
                  timerText.setVisibility(View.INVISIBLE);
                  if(countDownTimer != null){
                      countDownTimer.cancel();
                  }
                } else {
                  playStopButton.setImageDrawable(
                      getActivity().getResources().getDrawable(R.drawable.ic_stop));
                }
              }
            });

    // endregion
  }

  private void configureListeners() {

    muteButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            muted = !muted;

            if (muted) {
              for (Sound sound : viewModel.playingSounds().getValue()) {
                soundPool.setVolume(sound.streamId(), 0f, 0f);
              }
            } else {
              for (Sound sound : viewModel.playingSounds().getValue()) {
                soundPool.setVolume(sound.streamId(), sound.volume(), sound.volume());
              }
            }

            viewModel.updateMuteLiveData(muted);
          }
        });

    randomButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            // total number of available sounds can be found in viewmodel in sounds variable

            final Random totalNumberOfSounds = new Random();

            List<Sound> value = viewModel.sounds().getValue();

            for (int i = 0; i < 3; i++) {
              Sound sound = value.get(totalNumberOfSounds.nextInt(value.size()));
              playStopSound(sound.soundPoolId(), sound.isPlaying(), sound.streamId());
            }
          }
        });

    playStopButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (viewModel.playingSounds().getValue().size() != 0) {
              // stop all sounds and show play button again
              stopAllSounds();
            }
          }
        });

    saveFavorites.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            Boolean atLeastOneIsPlaying = viewModel.isAtLeastOneSoundPlaying().getValue();

            if (atLeastOneIsPlaying != null && atLeastOneIsPlaying) {
              activityCallback.showDialogFragment();
            } else {
              activityCallback.showToast("You must play at least one sound");
            }
          }
        });

    setTimer.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            Boolean atLeastOneIsPlaying = viewModel.isAtLeastOneSoundPlaying().getValue();

            if (atLeastOneIsPlaying != null && atLeastOneIsPlaying) {

                //show TimerDialog fragment created with file template

              Calendar calendar = Calendar.getInstance();

              new TimePickerDialog(
                      getContext(),
                      timerPickerListener,
                      0,
                      0,
                      true)
                  .show();

            } else {
              activityCallback.showToast("You must play at least one sound");
            }
          }
        });
  }

  private void toggleCountdownTimer(int hours, int minutes) {
    if (timerEnabled && countDownTimer != null) {
      countDownTimer.cancel();
      timerEnabled = false;
      timerText.setVisibility(View.INVISIBLE);
    } else {

      timerText.setVisibility(View.VISIBLE);

      countDownTimer =
          new CountDownTimer(TimeUnit.MINUTES.toMillis((hours * 60) + minutes), 1000) {

            public void onTick(long millisUntilFinished) {

              timerEnabled = !timerEnabled;

              timerText.setText(
                  "Sounds will stop in "
                      + String.format(
                          "%02d:%02d:%02d",
                          TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                          TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                          TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                              - TimeUnit.MINUTES.toSeconds(
                                  TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
              timerEnabled = false;
              stopAllSounds();
              timerText.setVisibility(View.INVISIBLE);
            }
          }.start();
    }
  }

  private void loadToSoundPool(List<Sound> sounds) {

    Log.d(TAG, "loadToSoundPool: called");

    List<Sound> sounds1 = new ArrayList<>();

    for (Sound sound : sounds) {
      // add to arraylist with soundId from soundpool
      int soundId = soundPool.load(getContext(), sound.file(), 1);
      sounds1.add(Sound.withSoundPoolId(sound, soundId));
    }

    Log.d(TAG, "loadToSoundPool: sounds: " + sounds1);

    viewModel.addToSounds(sounds1);
  }
}
