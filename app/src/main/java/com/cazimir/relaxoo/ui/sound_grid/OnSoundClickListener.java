package com.cazimir.relaxoo.ui.sound_grid;

import com.cazimir.relaxoo.model.Sound;

public interface OnSoundClickListener {
  void clicked(int soundId, boolean playing, int i, boolean pro);

  void volumeChange(Sound sound, int progress);

  void volumeChangeStopped(Sound sound, int progress);
}
