package com.cazimir.relaxoo.ui.sound_grid;

import com.cazimir.relaxoo.model.Sound;

public interface OnSoundClickListener {
  void clicked(Sound sound);

  void volumeChange(Sound sound, int progress);

  void volumeChangeStopped(Sound sound, int progress);

  void moreOptionsClicked(Sound sound);
}
