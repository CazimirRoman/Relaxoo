package com.cazimir.relaxoo.ui.favorites;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.model.Sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoritesViewModel extends ViewModel {

  private static final String TAG = "FavoritesViewModel";

  private MutableLiveData<List<SavedCombo>> savedCombos = new MutableLiveData<>();

  public FavoritesViewModel() {
    fetchFavorites();
  }

  public MutableLiveData<List<SavedCombo>> savedCombosLive() {
    return savedCombos;
  }

  private void fetchFavorites() {

    Log.d(TAG, "fetchFavorites: called");

    SavedCombo savedCombo1 =
        new SavedCombo.Builder()
            .withName("Test combo 1")
            .withSounds(
                Arrays.asList(
                    Sound.newSound("Sound1", R.drawable.ic_windy, R.raw.sound1, false, 0.5f),
                    Sound.newSound("Sound2", R.drawable.ic_windy, R.raw.sound3, false, 0.5f)))
            .build();

    SavedCombo savedCombo2 =
            new SavedCombo.Builder()
                    .withName("Test combo 2")
                    .withSounds(
                            Arrays.asList(
                                    Sound.newSound("Sound1", R.drawable.ic_windy, R.raw.sound1, false, 0.5f),
                                    Sound.newSound("Sound2", R.drawable.ic_windy, R.raw.sound3, false, 0.5f)))
                    .build();


    ArrayList<SavedCombo> list = new ArrayList<>(Arrays.asList(savedCombo1, savedCombo2));

    savedCombos.setValue(list);
  }
}
