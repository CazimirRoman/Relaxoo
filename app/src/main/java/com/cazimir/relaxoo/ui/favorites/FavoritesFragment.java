package com.cazimir.relaxoo.ui.favorites;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.adapter.SavedComboAdapter;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.model.Sound;
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback;

import java.util.List;

public class FavoritesFragment extends Fragment implements IFavoritesFragment {

  private static final String TAG = "FavoritesFragment";

  private FavoritesViewModel mViewModel;

  private RecyclerView favoritesList;

  private SavedComboAdapter adapter;

  private OnActivityCallback activityCallback;

  public static FavoritesFragment newInstance() {
    return new FavoritesFragment();
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.favorites_fragment, container, false);
    favoritesList = view.findViewById(R.id.favoritesList);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mViewModel = ViewModelProviders.of(this).get(FavoritesViewModel.class);
    mViewModel
        .savedCombosLive()
        .observe(
            getViewLifecycleOwner(),
            new Observer<List<SavedCombo>>() {
              @Override
              public void onChanged(List<SavedCombo> savedCombos) {
              Log.d(TAG, "onChanged: called: savedCombos size is: " + savedCombos.size());
                // update recyclerview
                favoritesList.setLayoutManager(new LinearLayoutManager(getContext()));
                adapter = new SavedComboAdapter(getContext(), savedCombos, new SavedComboAdapter.OnItemClickListener() {
                  @Override
                  public void onItemClick(SavedCombo savedCombo) {
                    activityCallback.triggerCombo(savedCombo);
                    adapter.updateComboWithPlayingStatus(savedCombo);
                  }
                });
                favoritesList.setAdapter(adapter);
              }
            });
  }

  @Override
  public void updateList(SavedCombo savedCombo) {
    adapter.addCombo(savedCombo);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    activityCallback = (OnActivityCallback) context;
  }
}
