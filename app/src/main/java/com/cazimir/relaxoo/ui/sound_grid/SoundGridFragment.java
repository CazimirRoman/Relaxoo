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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.adapter.GridAdapter;
import com.cazimir.relaxoo.dialog.TimerDialog;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.model.Sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SoundGridFragment extends Fragment {

  private static final String TAG = "SoundGridFragment";

  @BindView(R.id.upper_buttons)
  LinearLayout upperButtons;

  @BindView(R.id.gridView)
  GridView gridView;

  @BindView(R.id.mute_button)
  ImageButton muteButton;

  @BindView(R.id.random_button)
  ImageButton randomButton;

  @BindView(R.id.play_button)
  ImageButton playStopButton;

  @BindView(R.id.save_fav_button)
  ImageButton saveFavorites;

  @BindView(R.id.set_timer_button)
  ImageButton setTimer;

  @BindView(R.id.timerText)
  TextView timerText;

  private GridAdapter gridArrayAdapter;
  private SoundPool soundPool;
  private SoundGridViewModel viewModel;

  private boolean muted;
  private OnActivityCallback activityCallback;

  private MutableLiveData<Boolean> timerEnabled = new MutableLiveData<>();
  private CountDownTimer countDownTimer;

  private TimePickerDialog.OnTimeSetListener timerPickerListener =
          (view, hours, minutes) -> toggleCountdownTimer((hours * 60) + minutes);

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

    ButterKnife.bind(this, view);

    return view;
  }

  private void playStopSound(int soundPoolId, boolean playing, int streamId) {
    Log.d(TAG, "playStopSound: called");
    if (playing) {
      Log.d(TAG, "stopping sound");
      soundPool.stop(streamId);
      // update viewmodel for favorites fragment somehow - perhaps through activity?

      viewModel.updateSoundList(soundPoolId, 0);
    } else {
      int newStreamId = soundPool.play(soundPoolId, 0.5f, 0.5f, 0, -1, 1);
      Log.d(TAG, "playing sound");
      viewModel.updateSoundList(soundPoolId, newStreamId);
    }
  }

  @Override
  @NonNull
  public void onAttach(Context context) {
    super.onAttach(context);
    Log.d(TAG, "SoundGridFragment onAttach() called with: ");
    activityCallback = (OnActivityCallback) context;
  }

  private boolean stopAllSounds() {

    Log.d(TAG, "stopAllSounds: called");

    List<Sound> list = new CopyOnWriteArrayList<>(viewModel.playingSounds().getValue());

    // TODO: 22.12.2019 try to remove some of the for loops, use guava or something
    for (Sound sound : list) {
      soundPool.stop(sound.streamId());
      viewModel.updateSoundList(sound.soundPoolId(), -1);
    }

    return false;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    viewModel = ViewModelProviders.of(this).get(SoundGridViewModel.class);

    soundPool = viewModel.createOrGetSoundPool();

    setListenersForButtons();

    soundPool.setOnLoadCompleteListener(
            (soundPool, sampleId, status) -> {
              Log.d(TAG, "onLoadComplete: " + sampleId);
              viewModel.addedSound();
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
                  muteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_mute_off));

                } else {
                  muteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_mute_on));
                }
              }
            });

    // listen if at least one sound is playing to show notification in actionbar to user
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
                sounds -> {
                  Log.d(TAG, "Sound list changed: " + sounds);

                  if (!sounds.isEmpty()) {
                    activityCallback.soundsFetchedAndSaved();
                  }
                  gridArrayAdapter =
                          new GridAdapter(
                                  getContext(),
                                  sounds,
                                  new OnSoundClickListener() {
                                    @Override
                                    public void clicked(
                                            int soundId, boolean playing, int streamId, boolean pro) {

                                      if (pro) {
                                        activityCallback.showBottomDialogForPro();
                                      } else {
                                        playStopSound(soundId, playing, streamId);
                                      }
                                    }

                                    @Override
                                    public void volumeChange(int streamId, int progress) {
                                      // TODO: 18.12.2019 refactor this float to string to double
                                      // transformation
                                      Log.d(
                                              TAG, "volumeChange: called with volume: " + (double) progress / 100);
                                      soundPool.setVolume(
                                              streamId,
                                              Float.valueOf(String.valueOf((double) progress / 100)),
                                              Float.valueOf(String.valueOf((double) progress / 100)));
                                    }
                                  });
                  //              }

                  gridView.setAdapter(gridArrayAdapter);

                  // if sound not loaded yet and sounds list not yet populated
                  if (!sounds.isEmpty() && atLeastOneSoundWithoutSoundPoolId(sounds)) {
                    loadToSoundPool(sounds);
                  }
                });

    // listen to the playing sounds live data object to change the play stop getLogoPath icon on top
    viewModel
        .playingSounds()
        .observe(
            getViewLifecycleOwner(),
            new Observer<List<Sound>>() {
              @Override
              public void onChanged(List<Sound> playingList) {
                if (playingList.isEmpty()) {
                  playStopButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
                  setTimer.setImageDrawable(getResources().getDrawable(R.drawable.ic_timer_on));
                  if (countDownTimer != null) {
                    timerEnabled.setValue(false);
                  }
                } else {
                  playStopButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
                }
              }
            });

    // TODO: 06-Jan-20 move this to viewmodel
    timerEnabled.observe(
        getViewLifecycleOwner(),
        new Observer<Boolean>() {
          @Override
          public void onChanged(Boolean isTimerEnabled) {

            Log.d(TAG, "timerEnabled: " + isTimerEnabled);

            if (isTimerEnabled) {
              showTimerText();
              setTimer.setImageDrawable(getResources().getDrawable(R.drawable.ic_timer_off));
            } else {
              hideTimerText();
              setTimer.setImageDrawable(getResources().getDrawable(R.drawable.ic_timer_on));
              countDownTimer.cancel();
              Log.d(TAG, "countDownTimer canceled!");
            }
          }
        });

    viewModel
            .getSoundsLoadedToSoundPool()
            .observe(
                    getViewLifecycleOwner(),
                    new Observer<Integer>() {
                      @Override
                      public void onChanged(Integer soundsAdded) {

                        if (viewModel.sounds().getValue().size() != 0) {
                  if (soundsAdded == viewModel.sounds().getValue().size()) {
                    activityCallback.hideSplash();
                  }
                        }
                      }
                    });

    // endregion
  }

  private boolean atLeastOneSoundWithoutSoundPoolId(List<Sound> sounds) {
    for (Sound sound : sounds) {
      if (sound.soundPoolId() == 0) {
        return true;
      }
    }

    return false;
  }

  // region Listeners for buttons on top
  private void setListenersForButtons() {

    Log.d(TAG, "setListenersForButtons: called");

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

            final Random totalNumberOfSounds = new Random();

            // total number of available sounds can be found in viewmodel in sounds variable
            List<Sound> value = viewModel.sounds().getValue();

            for (int i = 0; i < value.size(); i++) {
              Log.d(TAG, "randomClick for loop called: " + i);
              Sound sound = value.get(totalNumberOfSounds.nextInt(value.size()));
              if (!sound.isPro()) {
                playStopSound(sound.soundPoolId(), sound.isPlaying(), sound.streamId());
              }
            }
          }
        });

    playStopButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (viewModel.playingSounds().getValue() != null
                && viewModel.playingSounds().getValue().size() != 0) {
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
              activityCallback.showAddToFavoritesDialog(
                  getSoundParameters(viewModel.playingSounds().getValue()));
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

              Boolean timerIsRunning = timerEnabled.getValue();

              if (timerIsRunning != null && timerIsRunning) {
                timerEnabled.setValue(false);
              } else {
                // show TimerDialog fragment created with getFilePath template
                activityCallback.showTimerDialog();
              }

            } else {
              activityCallback.showToast("You must play at least one sound");
            }
          }
        });
  }

  private HashMap<Integer, Integer> getSoundParameters(List<Sound> sounds) {

    HashMap<Integer, Integer> hashMap = new HashMap<>();

    for (Sound sound : sounds) {
      hashMap.put(sound.soundPoolId(), sound.streamId());
    }

    return hashMap;
  }

  // endregion

  private void loadToSoundPool(List<Sound> sounds) {

    Log.d(TAG, "loadToSoundPool: called");

    ArrayList<Sound> sounds1 = new ArrayList<>();

    for (Sound sound : sounds) {

      if (sound.soundPoolId() == 0) {
        // add to arraylist with soundId from soundpool
        int soundId = soundPool.load(sound.getFilePath(), 1);
        sounds1.add(Sound.withSoundPoolId(sound, soundId));
      } else {
        sounds1.add(sound);
      }
    }

    Log.d(TAG, "loadToSoundPool: sounds: " + sounds1);

    viewModel.addToSounds(sounds1);
  }

  private void showTimerText() {
    timerText.setVisibility(View.VISIBLE);
  }

  private void hideTimerText() {
    Log.d(TAG, "hideTimerText: called");
    timerText.setVisibility(View.INVISIBLE);
  }

  private void setTimerText(String text) {
    timerText.setText(text);
  }

  public void startCountDownTimer(Integer minutes) {

    Log.d(TAG, "startCountDownTimer: minutes: " + minutes);

    if (minutes == 999) {
      new TimePickerDialog(getContext(), timerPickerListener, 0, 0, true).show();
    } else {
      toggleCountdownTimer(minutes);
    }
  }

  private void toggleCountdownTimer(int minutes) {

    if (timerEnabled.getValue() != null && timerEnabled.getValue() && countDownTimer != null) {
      timerEnabled.setValue(false);
    } else {
      countDownTimer =
          new CountDownTimer(TimeUnit.MINUTES.toMillis(minutes), 1000) {

            public void onTick(long millisUntilFinished) {

              setTimerText(
                  String.format(
                      "Sound%s will stop in " + TimerDialog.getCountTimeByLong(millisUntilFinished),
                      viewModel.playingSounds().getValue().size() > 1 ? "s" : ""));
            }

            public void onFinish() {
              setTimer.setImageDrawable(getResources().getDrawable(R.drawable.ic_timer_on));
              timerEnabled.setValue(false);
              stopAllSounds();
              hideTimerText();
            }
          }.start();

      timerEnabled.setValue(true);
    }
  }

  public void triggerCombo(SavedCombo savedCombo) {

    boolean areSoundsStillPlaying = stopAllSounds();

    for (Map.Entry<Integer, Integer> entry : savedCombo.getSoundPoolParameters().entrySet()) {
      playStopSound(entry.getKey(), areSoundsStillPlaying, entry.getValue());
    }
  }

  public void fetchSounds() {
    viewModel.fetchSounds();
  }

  @Override
  public void onStart() {
    super.onStart();
    activityCallback.soundGridFragmentStarted();
  }

  public boolean soundsAlreadyFetched() {
    return viewModel.getSoundsAlreadyFetched();
  }
}
