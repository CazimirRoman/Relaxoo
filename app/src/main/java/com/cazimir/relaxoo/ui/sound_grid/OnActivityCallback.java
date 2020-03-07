package com.cazimir.relaxoo.ui.sound_grid;

import com.cazimir.relaxoo.dialog.OnDeleted;
import com.cazimir.relaxoo.model.Recording;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.model.Sound;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public interface OnActivityCallback {
  void showNotification();

  void hideNotification();

  void showAddToFavoritesDialog(HashMap<Integer, Integer> playingSounds);

  void showToast(String message);

  void showTimerDialog();

  void triggerCombo(SavedCombo savedCombo);

  void showDeleteConfirmationDialog(OnDeleted recDeleted);

  void showBottomDialogForPro();

  void soundsFetchedAndSaved();

  void soundGridFragmentStarted();

  void hideSplash();

  void removeAds();

  void deleteRecording(@NotNull Recording recording);

  void renameRecording(@NotNull Recording recording, @NotNull String toString);

  void pinToDashBoardActionCalled(@NotNull Sound sound);
}
