package com.cazimir.relaxoo.ui.sound_grid;

import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.model.Sound;

import java.util.HashMap;
import java.util.List;

public interface OnActivityCallback {
  void showNotification();

  void hideNotification();

  void showAddToFavoritesDialog(HashMap<Integer, Integer> playingSounds);

  void showToast(String message);

  void showTimerDialog();

  void triggerCombo(SavedCombo savedCombo);

  void showDeleteConfirmationDialog(int position);

  void showBottomDialog();

  void showIfFileStillThere(List<Sound> sounds);

  void soundsFetchedAndSaved();

  void fragmentStarted();
}
