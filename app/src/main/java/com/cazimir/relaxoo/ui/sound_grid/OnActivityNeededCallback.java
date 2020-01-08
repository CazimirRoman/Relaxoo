package com.cazimir.relaxoo.ui.sound_grid;

import com.cazimir.relaxoo.model.Sound;

import java.util.List;

public interface OnActivityNeededCallback {
  void showNotification();

  void hideNotification();

  void showAddToFavoritesDialog(List<Sound> playingSounds);

  void showToast(String message);

  void showTimerDialog();
}
