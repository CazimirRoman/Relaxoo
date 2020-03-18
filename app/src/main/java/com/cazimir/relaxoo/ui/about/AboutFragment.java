package com.cazimir.relaxoo.ui.about;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.adapter.AboutListAdapter;
import com.cazimir.relaxoo.model.AboutItem;
import com.cazimir.relaxoo.model.MenuItemType;
import com.cazimir.relaxoo.shared.SharedViewModel;
import com.cazimir.relaxoo.ui.more_apps.MoreAppsActivity;
import com.cazimir.relaxoo.ui.privacy_policy.PrivacyPolicyActivity;
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
  private SharedViewModel sharedViewModel;
  private List<AboutItem> aboutItems;

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

    sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);

    aboutItems = populateAboutItems(sharedViewModel.getAdsBought().getValue());

    aboutRecyclerView.setAdapter(
            new AboutListAdapter(
                    getContext(),
                    aboutItems,
                    item -> {
                      switch (item.getName()) {
                        case REMOVE_ADS:
                          startRemoveAdsAction();
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
    Intent intent = new Intent(getActivity(), MoreAppsActivity.class);
    startActivity(putAdsBoughExtra(intent));
  }

  private void startPrivacyPolicyActivity() {
    Intent intent = new Intent(getActivity(), PrivacyPolicyActivity.class);
    startActivity(putAdsBoughExtra(intent));
  }

  private Intent putAdsBoughExtra(Intent intent) {
    return intent.putExtra("ads_bought", sharedViewModel.getAdsBought().getValue());
  }

  private void startRateAppAction() {
    Uri uri = Uri.parse("market://details?id=" + getContext().getPackageName());
    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
    // To count with Play market backstack, After pressing back button,
    // to taken back to our application, we need to add following flags to intent.
    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    try {
      startActivity(goToMarket);
    } catch (ActivityNotFoundException e) {
      startActivity(new Intent(Intent.ACTION_VIEW,
              Uri.parse("http://play.google.com/store/apps/details?id=" + getContext().getPackageName())));
    }
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

  private List<AboutItem> populateAboutItems(Boolean adsBought) {
    List<AboutItem> aboutItems = new ArrayList<>();

    if (!adsBought) {
      aboutItems.add(new AboutItem(MenuItemType.REMOVE_ADS, R.drawable.ic_message));
    }
    aboutItems.add(new AboutItem(MenuItemType.SHARE, R.drawable.ic_message));
    aboutItems.add(new AboutItem(MenuItemType.PRIVACY_POLICY, R.drawable.ic_message));
    aboutItems.add(new AboutItem(MenuItemType.RATE_APP, R.drawable.ic_message));
    aboutItems.add(new AboutItem(MenuItemType.MORE_APPS, R.drawable.ic_message));
    return aboutItems;
  }
}
