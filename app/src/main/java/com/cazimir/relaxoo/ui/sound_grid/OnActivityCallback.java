package com.cazimir.relaxoo.ui.sound_grid;

import com.cazimir.relaxoo.model.SavedCombo;

import java.util.HashMap;

public interface OnActivityCallback {
  void showNotification();

  void hideNotification();

  void showAddToFavoritesDialog(HashMap<Integer, Integer> playingSounds);

  void showToast(String message);

  void showTimerDialog();

  void triggerCombo(SavedCombo savedCombo);

  void showDeleteConfirmationDialog(int position);

  void showBottomDialogForPro();

  void soundsFetchedAndSaved();

  void soundGridFragmentStarted();

  void hideSplash();

  void removeAds();
}
