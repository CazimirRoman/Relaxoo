package com.cazimir.relaxoo.ui.sound_grid;

public interface OnSoundClickListener {
  void clicked(int soundId, boolean playing, int i, boolean pro);

  void volumeChange(int streamId, int progress);
}
