package com.cazimir.relaxoo.ui.about;

import android.content.Context;
import android.content.Intent;
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

import com.cazimir.relaxoo.MainActivity;
import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.adapter.AboutListAdapter;
import com.cazimir.relaxoo.model.AboutItem;
import com.cazimir.relaxoo.ui.settings.SettingsActivity;
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutFragment extends Fragment {

  private static final String TAG = "AboutFragment";

  @BindView(R.id.about_recycler_view)
  RecyclerView aboutRecyclerView;
  private AboutViewModel aboutViewModel;
  private OnActivityCallback activityCallback;

  public static AboutFragment newInstance() {
    return new AboutFragment();
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    activityCallback = (MainActivity) context;
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.about_fragment, container, false);
    ButterKnife.bind(this, view);

    aboutRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    aboutViewModel = ViewModelProviders.of(this).get(AboutViewModel.class);
    aboutViewModel.populateAboutItems();

    aboutViewModel.aboutItems().observe(getViewLifecycleOwner(), new Observer<List<AboutItem>>() {
      @Override
      public void onChanged(List<AboutItem> aboutItems) {
        Log.d(TAG, "aboutItems fetched!");
        aboutRecyclerView.setAdapter(new AboutListAdapter(getContext(), aboutItems, new AboutListAdapter.Interactor() {
          @Override
          public void onItemClick(AboutItem item) {
            switch (item.getName()) {
              case "Settings":
                aboutViewModel.settings();

            }
            activityCallback.showToast(String.format("Clicked on %s", item.getName()));
          }
        }));
      }


    });

    aboutViewModel.get_removeAds().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
      @Override
      public void onChanged(Boolean aBoolean) {
        // hide ads on view
      }
    });

    aboutViewModel.get_settings().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
      @Override
      public void onChanged(Boolean settingsClicked) {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
      }
    });

    aboutViewModel.get_share().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
      @Override
      public void onChanged(Boolean shareClicked) {
        // open share dialog
      }
    });
  }
}
