package com.cazimir.relaxoo.ui.about;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.AboutItem;

import java.util.ArrayList;
import java.util.List;

public class AboutViewModel extends ViewModel {

  private static final String TAG = "AboutViewModel";

  private MutableLiveData<List<AboutItem>> mutableAboutItems = new MutableLiveData<>();
  private List<AboutItem> aboutItems = new ArrayList<>();

  public MutableLiveData<List<AboutItem>> aboutItems() {
    return mutableAboutItems;
  }

  public void populateAboutItems() {
    Log.d(TAG, "populateAboutItems: called");
    if (aboutItems.isEmpty()) {
      aboutItems.add(
              new AboutItem.AboutItemBuilder()
                      .withName("Remove Ads")
                      .withIcon(R.drawable.ic_message)
                      .build());
      aboutItems.add(
              new AboutItem.AboutItemBuilder()
                      .withName("Upgrade to PRO")
                      .withIcon(R.drawable.ic_message)
                      .build());
      aboutItems.add(
              new AboutItem.AboutItemBuilder()
                      .withName("Settings")
                      .withIcon(R.drawable.ic_message)
                      .build());
      aboutItems.add(
              new AboutItem.AboutItemBuilder()
                      .withName("Share")
                      .withIcon(R.drawable.ic_message)
                      .build());
      aboutItems.add(
              new AboutItem.AboutItemBuilder()
                      .withName("Privacy Policy")
                      .withIcon(R.drawable.ic_message)
                      .build());
      aboutItems.add(
              new AboutItem.AboutItemBuilder()
                      .withName("Rate App")
                      .withIcon(R.drawable.ic_message)
                      .build());
      aboutItems.add(
              new AboutItem.AboutItemBuilder()
                      .withName("More App")
                      .withIcon(R.drawable.ic_message)
                      .build());
    }

    aboutItems().setValue(aboutItems);
  }
  // TODO: Implement the ViewModel
}
