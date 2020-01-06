package com.cazimir.relaxoo.ui.sound_grid;

public interface OnActivityNeededCallback {
  void showNotification();

  void hideNotification();

  void showAddToFavoritesDialog();

  void showToast(String message);

  void showTimerDialog();
}
