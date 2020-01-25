package com.cazimir.relaxoo.ui.create_sound;

import android.content.Context;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.Collections;

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
                      file -> viewModel.deleteRecording(file),
                      getContext(),
                      Collections.emptyList()));

      return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
      viewModel = ViewModelProviders.of(this).get(CreateSoundViewModel.class);

      viewModel
              .getRecordingsLive()
              .observe(
                      getViewLifecycleOwner(),
                      files -> {
                          Log.d(TAG, "onChanged() called with: files[]" + files.toString());
                          recordingList.setAdapter(
                                  new RecordingAdapter(
                                          file -> viewModel.deleteRecording(file),
                                          getContext(),
                                          Arrays.asList(files)));
                          noRecordingsText.setVisibility(files.length != 0 ? View.GONE : View.VISIBLE);
                      });
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
        viewModel.listChanged();
  }
}
