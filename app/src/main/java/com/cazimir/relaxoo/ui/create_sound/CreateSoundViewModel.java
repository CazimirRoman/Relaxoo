package com.cazimir.relaxoo.ui.create_sound;

import android.os.Environment;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;

public class CreateSoundViewModel extends ViewModel {

  private static final String TAG = "CreateSoundViewModel";

  private File[] recordings;
  private MutableLiveData<File[]> recordingsLive = new MutableLiveData<>();

  public CreateSoundViewModel() {
    listChanged();
  }

  public MutableLiveData<File[]> getRecordingsLive() {
    return recordingsLive;
  }

  public void listChanged() {
    File ownSoundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/own_sounds");
    recordings = ownSoundsFolder.listFiles();
    recordingsLive.setValue(recordings);
  }

  public void deleteRecording(File file) {
    File ownSoundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/own_sounds");
    File file2 = new File(ownSoundsFolder, file.getName());
    boolean deleted = file.delete();
    Log.d(TAG, "deleteRecording() called with: " + deleted);
    if (deleted) {
      listChanged();
    }

  }
}
