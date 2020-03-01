package com.cazimir.relaxoo.ui.admin_add_sound;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.Sound;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AdminAddSoundFragment extends Fragment {

  private static final String TAG = "AdminAddSoundFragment";

  @BindView(R.id.soundName)
  EditText soundName;

  @BindView(R.id.soundLogoUrl)
  EditText soundLogoUrl;

  @BindView(R.id.soundNameStorage)
  EditText soundNameStorage;

  @BindView(R.id.checkBoxPro)
  CheckBox checkBoxPro;

  @BindView(R.id.saveButton)
  Button saveButton;

  private AdminAddSoundViewModel viewModel;

  public static AdminAddSoundFragment newInstance() {
    return new AdminAddSoundFragment();
  }

  @Override
  public View onCreateView(
          @NonNull LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState) {
    View inflate = inflater.inflate(R.layout.admin_add_sound_fragment, container, false);

    ButterKnife.bind(this, inflate);

    return inflate;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    viewModel = new ViewModelProvider(this).get(AdminAddSoundViewModel.class);
  }

  @OnClick(R.id.checkBoxPro)
  public void onCheckBoxProClicked() {
  }

  @OnClick(R.id.saveButton)
  public void onSaveButtonClicked() {

    if (!soundName.getText().toString().isEmpty()
            && !soundLogoUrl.getText().toString().isEmpty()
            && !soundNameStorage.getText().toString().isEmpty()) {

      Sound sound =
              Sound.SoundBuilder.aSound()
                      .withName(soundName.getText().toString())
                      .withLogo(soundLogoUrl.getText().toString())
                      .withFilePath(soundNameStorage.getText().toString())
                      .withPro(checkBoxPro.isChecked())
                      .build();

      viewModel.saveToFirebase(sound);
      Log.d(TAG, "onSaveButtonClicked() called with: Saving to Firebase database");
    } else {

    }
  }
}
