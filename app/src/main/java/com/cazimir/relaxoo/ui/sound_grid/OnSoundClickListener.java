package com.cazimir.relaxoo.ui.sound_grid;

public interface OnSoundClickListener {
    void clicked(boolean playing);
    void volumeChange(int progress);
}
