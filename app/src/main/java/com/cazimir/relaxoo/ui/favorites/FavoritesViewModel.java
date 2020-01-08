package com.cazimir.relaxoo.ui.favorites;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.model.Sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    HashMap<Integer, Integer> hashMap = new HashMap<>();

    hashMap.put(55,99);

    SavedCombo savedCombo1 =
        new SavedCombo.Builder()
            .withName("Phone")
            .withSoundPoolParameters(hashMap)
            .build();

    ArrayList<SavedCombo> list = new ArrayList<>(Arrays.asList(savedCombo1));

    savedCombos.setValue(list);
  }
}
