package com.cazimir.relaxoo.ui.sound_grid;

public interface OnActivityNeededCallback {
    void showNotification();
    void hideNotification();
    void showDialogFragment();
    void showToast(String message);
}
