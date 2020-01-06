package com.cazimir.relaxoo.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.cazimir.relaxoo.R;

public class FavoritesFragment extends Fragment implements IFavoritesFragment {

  private FavoritesViewModel mViewModel;
  private TextView text;

  public static FavoritesFragment newInstance() {
    return new FavoritesFragment();
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.favorites_fragment, container, false);
    text = view.findViewById(R.id.text);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mViewModel = ViewModelProviders.of(this).get(FavoritesViewModel.class);
    // TODO: Use the ViewModel
  }

  @Override
  public void updateList(String soundName) {
    text.setText(soundName);
  }
}
