package com.cazimir.relaxoo.ui.create_sound;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.cazimir.relaxoo.R;

public class CreateSoundFragment extends Fragment {

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
}
