package com.cazimir.relaxoo.ui.create_sound;

import android.os.Environment;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cazimir.relaxoo.model.Recording;

import java.io.File;
import java.util.ArrayList;

public class CreateSoundViewModel extends ViewModel {

  private static final String TAG = "CreateSoundViewModel";

  private File[] recordings;
  private MutableLiveData<ArrayList<Recording>> recordingsLive = new MutableLiveData<>();

  public CreateSoundViewModel() {
    listChanged();
  }

  public MutableLiveData<ArrayList<Recording>> getRecordingsLive() {
    return recordingsLive;
  }

  public void listChanged() {
    File ownSoundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/own_sounds");

    if (!ownSoundsFolder.exists()) {
      ownSoundsFolder.mkdir();
    }

    recordings = ownSoundsFolder.listFiles();

    ArrayList<Recording> recordingsList = new ArrayList<>();

    for (File file : recordings) {
      recordingsList.add(new Recording.Builder().withFile(file).withFileName(file.getName()).build());
    }

    recordingsLive.setValue(recordingsList);
  }

  void deleteRecording(Recording recording) {
    File ownSoundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/own_sounds");
    File file2 = new File(ownSoundsFolder, recording.getFile().getName());
    boolean deleted = recording.getFile().delete();
    Log.d(TAG, "deleteRecording() called with: " + deleted);
    if (deleted) {
      listChanged();
    }

  }

  void editRecording(Recording recording, String newName) {
    File ownSoundsFolder = Environment.getExternalStoragePublicDirectory("Relaxoo/own_sounds");
    File from = new File(ownSoundsFolder, recording.getFile().getName());
    File to = new File(ownSoundsFolder, newName + ".wav");

    boolean renamed = from.renameTo(to);

    if (renamed) {
      listChanged();
    }


  }
}
