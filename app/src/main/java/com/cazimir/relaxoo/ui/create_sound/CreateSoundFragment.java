package com.cazimir.relaxoo.ui.create_sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cazimir.relaxoo.MainActivity;
import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.adapter.RecordingAdapter;
import com.cazimir.relaxoo.model.Recording;
import com.cazimir.relaxoo.repository.SoundRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateSoundFragment extends Fragment {

    private static final String TAG = "CreateSoundFragment";

  @BindView(R.id.add_recording)
  FloatingActionButton addRecordingButton;

    @BindView(R.id.recording_list)
    RecyclerView recordingList;

    @BindView(R.id.no_recordings_text)
    TextView noRecordingsText;

    private CreateSoundViewModel viewModel;

    private OnRecordingStarted activityCallback;

    private MediaPlayer mediaPlayer;

  public static CreateSoundFragment newInstance() {
    return new CreateSoundFragment();
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.create_sound_fragment, container, false);
      ButterKnife.bind(this, view);

      recordingList.setLayoutManager(new LinearLayoutManager(getContext()));
      recordingList.setAdapter(
              new RecordingAdapter(
                      new RecordingAdapter.OnItemClickListener() {
                          @Override
                          public void onPlayClicked(Recording item) {
                              playRecordedSound(item);
                          }

                          @Override
                          public void onStopClicked() {
                              stopRecordedSound();
                          }

                          @Override
                          public void onOptionsClicked(Recording recording) {
                              activityCallback.showBottomDialogForRecording(recording);
                          }
                      },
                      getContext(),
                      new ArrayList<>()));

      return view;
  }

    private void stopRecordedSound() {
        Log.d(TAG, "stopRecordedSound() called");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    private void playRecordedSound(Recording recordedSound) {
        Uri uri = Uri.fromFile(recordedSound.getFile());

        try {
            mediaPlayer.setDataSource(getContext(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d(TAG, "playRecordedSound() called with: finished");
                        RecordingAdapter adapter = (RecordingAdapter) recordingList.getAdapter();
                        adapter.finishedPlayingRecording(recordedSound);
                    }
                });

        mediaPlayer.start();

        Log.d(TAG, "playRecordedSound() called with: " + mediaPlayer.isPlaying());
    }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
      viewModel = ViewModelProviders.of(this).get(CreateSoundViewModel.class);

      viewModel.repository = new SoundRepository();

      viewModel.refreshList();

      viewModel
              .getRecordingsLive()
              .observe(
                      getViewLifecycleOwner(),
                      files -> {
                          Log.d(TAG, "onChanged() called with: files[]" + files.toString());

                          RecordingAdapter adapter = (RecordingAdapter) recordingList.getAdapter();
                          adapter.setList(files);
                          noRecordingsText.setVisibility(files.size() != 0 ? View.GONE : View.VISIBLE);
                      });

      mediaPlayer = new MediaPlayer();
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
  }

  @OnClick(R.id.add_recording)
  public void onViewClicked() {
      Log.d(TAG, "onViewClicked() called");
      activityCallback.recordingStarted();
  }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activityCallback = (MainActivity) context;
    }

    public void updateList() {
        viewModel.refreshList();
    }

    public void deleteRecording(Recording recording) {
        viewModel.deleteRecording(recording);
    }

    public void renameRecording(Recording recording, String newName) {
        viewModel.editRecording(recording, newName);
    }
}
