package com.cazimir.relaxoo.ui.create_sound;

import com.cazimir.relaxoo.model.Recording;

public interface OnRecordingStarted {
    void recordingStarted();

    void showBottomDialogForRecording(Recording recording);
}
