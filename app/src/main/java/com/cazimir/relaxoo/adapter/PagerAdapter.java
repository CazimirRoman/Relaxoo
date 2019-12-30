package com.cazimir.relaxoo.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cazimir.relaxoo.ui.about.AboutFragment;
import com.cazimir.relaxoo.ui.create_sound.CreateSoundFragment;
import com.cazimir.relaxoo.ui.favorites.FavoritesFragment;
import com.cazimir.relaxoo.ui.sound_grid.SoundGridFragment;

public class PagerAdapter extends FragmentPagerAdapter {

  public static final int NUMBER_OF_FRAGMENTS = 4;

  public PagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public Fragment getItem(int position) {

    switch (position) {
      case 0:
        return SoundGridFragment.newInstance();
      case 1:
        return FavoritesFragment.newInstance();
      case 2:
        return CreateSoundFragment.newInstance();
      case 3:
        return AboutFragment.newInstance();
    }

    return null;
  }

  @Override
  public int getCount() {
    return NUMBER_OF_FRAGMENTS;
  }
}
