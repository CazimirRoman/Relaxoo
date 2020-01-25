package com.cazimir.relaxoo.ui.create_sound;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.cazimir.relaxoo.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.OnClick;
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

public class CreateSoundFragment extends Fragment {

  @BindView(R.id.add_recording)
  FloatingActionButton addRecordingButton;
  private CreateSoundViewModel mViewModel;

  public static CreateSoundFragment newInstance() {
    return new CreateSoundFragment();
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.create_sound_fragment, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mViewModel = ViewModelProviders.of(this).get(CreateSoundViewModel.class);
    // TODO: Use the ViewModel
  }

  @OnClick(R.id.add_recording)
  public void onViewClicked() {

    String filePath = Environment.getExternalStorageDirectory() + "/recorded_audio.wav";
    int color = getResources().getColor(R.color.colorPrimaryDark);
    int requestCode = 0;
    AndroidAudioRecorder.with(this)
            // Required
            .setFilePath(filePath)
            .setColor(color)
            .setRequestCode(requestCode)

            // Optional
            .setSource(AudioSource.MIC)
            .setChannel(AudioChannel.STEREO)
            .setSampleRate(AudioSampleRate.HZ_48000)
            .setAutoStart(true)
            .setKeepDisplayOn(true)

            // Start recording
            .record();
  }
}
