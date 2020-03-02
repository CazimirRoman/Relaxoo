package com.cazimir.relaxoo.ui.favorites;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.adapter.SavedComboAdapter;
import com.cazimir.relaxoo.model.SavedCombo;
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoritesFragment extends Fragment {

  private static final String TAG = "FavoritesFragment";
  @BindView(R.id.no_favorites_text)
  TextView noFavoritesText;

  private FavoritesViewModel viewModel;

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
    ButterKnife.bind(this, view);
    favoritesList = view.findViewById(R.id.favoritesList);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    viewModel = ViewModelProviders.of(this).get(FavoritesViewModel.class);
    viewModel.repository = new FavoritesRepository();
    viewModel.fetchFavorites();
    viewModel
            .savedCombosLive()
            .observe(
                    getViewLifecycleOwner(),
                    savedCombos -> {
//                Log.d(TAG, "onChanged: called: savedCombos size is: " + savedCombos.size());
                      // update recyclerview
                      favoritesList.setLayoutManager(new LinearLayoutManager(getContext()));
                      adapter = new SavedComboAdapter(getContext(), savedCombos, new SavedComboAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(SavedCombo savedCombo) {
                          activityCallback.triggerCombo(savedCombo);
                          //adapter.updateComboWithPlayingStatus(savedCombo);
                        }

                        @Override
                        public void onItemDeleted(int position) {
                          activityCallback.showDeleteConfirmationDialog(position);
                        }
                      });
                      favoritesList.setAdapter(adapter);

                      noFavoritesText.setVisibility(savedCombos.getSavedComboList().size() != 0 ? View.GONE : View.VISIBLE);


                    });
  }

  public void updateList(SavedCombo savedCombo) {
    viewModel.addFavorite(savedCombo);
  }

  public void deleteFavorite(int position) {
    viewModel.deleteCombo(position);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    activityCallback = (OnActivityCallback) context;
  }
}
