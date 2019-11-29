package com.cazimir.relaxoo.ui.soundlist;

import androidx.lifecycle.ViewModelProviders;

import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.adapter.GridBaseAdapter;
import com.cazimir.relaxoo.model.Sound;

import java.util.ArrayList;

import static android.media.AudioManager.STREAM_MUSIC;

public class SoundListFragment extends Fragment implements ISoundListFragment {

  private static final String TAG = "SoundListFragment";
  private static final int MAX_SOUNDS = 5;
  private GridView gridView;
  private GridBaseAdapter gridBaseAdapter;
  private int sound1;
  private SoundPool soundPool;
  private SoundListViewModel viewModel;

  public static SoundListFragment newInstance() {
    return new SoundListFragment();
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

    gridBaseAdapter = new GridBaseAdapter(getContext(), sounds);

    gridView.setAdapter(gridBaseAdapter);

    gridView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            soundPool.play(sound1, 1.0f, 1.0f, 0, -1, 1);
          }
        });

    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    viewModel = ViewModelProviders.of(this).get(SoundListViewModel.class);

    soundPool = new SoundPool(MAX_SOUNDS, STREAM_MUSIC, 0);

    sound1 = soundPool.load(getContext(), R.raw.sound1, 1);
  }
}
