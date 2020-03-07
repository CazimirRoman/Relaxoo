package com.cazimir.relaxoo.ui.about;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.adapter.AboutListAdapter;
import com.cazimir.relaxoo.model.AboutItem;
import com.cazimir.relaxoo.model.MenuItemType;
import com.cazimir.relaxoo.ui.MoreAppsActivity;
import com.cazimir.relaxoo.ui.PrivacyPolicyActivity;
import com.cazimir.relaxoo.ui.settings.SettingsActivity;
import com.cazimir.relaxoo.ui.sound_grid.OnActivityCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutFragment extends Fragment {

  private static final String TAG = "AboutFragment";

  @BindView(R.id.about_recycler_view)
  RecyclerView aboutRecyclerView;

  private OnActivityCallback activityCallback;

  public static AboutFragment newInstance() {
    return new AboutFragment();
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
    aboutRecyclerView.setAdapter(
            new AboutListAdapter(
                    getContext(),
                    populateAboutItems(),
                    item -> {
                      switch (item.getName()) {
                        case REMOVE_ADS:
                          startRemoveAdsAction();
                          break;
                        case SETTINGS:
                          startSettingsActivity();
                          break;
                        case SHARE:
                          startShareAction();
                          break;
                        case PRIVACY_POLICY:
                          startPrivacyPolicyActivity();
                          break;
                        case RATE_APP:
                          startRateAppAction();
                          break;
                        case MORE_APPS:
                          startMoreAppsActivity();
                          break;
                      }
                    }));
  }

  private void startRemoveAdsAction() {
    activityCallback.removeAds();
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    this.activityCallback = (OnActivityCallback) context;
  }

  private void startMoreAppsActivity() {
    startActivity(new Intent(getActivity(), MoreAppsActivity.class));
  }

  private void startPrivacyPolicyActivity() {
    startActivity(new Intent(getActivity(), PrivacyPolicyActivity.class));
  }

  private void startRateAppAction() {
  }

  private void startShareAction() {
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_text));
    sendIntent.setType("text/plain");
    startActivity(sendIntent);
  }

  private void startSettingsActivity() {
    startActivity(new Intent(getActivity(), SettingsActivity.class));
  }

  private List<AboutItem> populateAboutItems() {
    List<AboutItem> aboutItems = new ArrayList<>();
    aboutItems.add(new AboutItem(MenuItemType.REMOVE_ADS, R.drawable.ic_message));
    aboutItems.add(new AboutItem(MenuItemType.SETTINGS, R.drawable.ic_message));
    aboutItems.add(new AboutItem(MenuItemType.SHARE, R.drawable.ic_message));
    aboutItems.add(new AboutItem(MenuItemType.PRIVACY_POLICY, R.drawable.ic_message));
    aboutItems.add(new AboutItem(MenuItemType.RATE_APP, R.drawable.ic_message));
    aboutItems.add(new AboutItem(MenuItemType.MORE_APPS, R.drawable.ic_message));
    return aboutItems;
  }
}
