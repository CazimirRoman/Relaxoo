package com.cazimir.relaxoo.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.Sound;
import com.cazimir.relaxoo.ui.sound_grid.OnSoundClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class GridAdapter extends ArrayAdapter<Sound> {

    private static final String TAG = "GridAdapter";
    private OnSoundClickListener listener;

    public GridAdapter(Context ctx, ArrayList<Sound> sounds, OnSoundClickListener listener) {
        super(ctx, 0, sounds);
        this.listener = listener;
    }

  @NonNull
  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {

    final ViewHolderItem viewHolderItem;

    if (convertView == null) {

      // inflate de layout
      LayoutInflater inflater =
          (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
        }

        // will set up the RowHolder

        viewHolderItem = new ViewHolderItem();
        viewHolderItem.soundImage = convertView.findViewById(R.id.sound_image);
        viewHolderItem.soundVolume = convertView.findViewById(R.id.sound_volume);
        viewHolderItem.parentLayout = convertView.findViewById(R.id.cl);
        viewHolderItem.proIcon = convertView.findViewById(R.id.pro_icon);
        viewHolderItem.moreOptions = convertView.findViewById(R.id.more_options);
        viewHolderItem.loading = convertView.findViewById(R.id.grid_item_loading);

        // store the holder with the view.
        convertView.setTag(viewHolderItem);
    } else {
        // we've just avoided calling findviewbyid on resource everytime
        // just use the viewHolder
        viewHolderItem = (ViewHolderItem) convertView.getTag();
    }

      // object item based on the position
      final Sound sound = getItem(position);

      viewHolderItem.soundVolume.setProgress(Math.round(sound.getVolume() * 100));
      viewHolderItem.soundVolume.setVisibility(sound.getPlaying() ? View.VISIBLE : View.INVISIBLE);
      viewHolderItem.moreOptions.setVisibility(sound.getCustom() ? View.VISIBLE : View.INVISIBLE);
      viewHolderItem.soundImage.setImageBitmap(BitmapFactory.decodeFile(sound.getLogoPath()));
      // hide progressbar once sound loaded to soundpool
      if (sound.getLoaded() == false) {
          viewHolderItem.loading.setVisibility(View.VISIBLE);
      } else {
          viewHolderItem.loading.setVisibility(View.GONE);
      }
      // because playing the sound refreshes the grid change color based on playing status
      if (sound.getPlaying()) {
          viewHolderItem.soundImage.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
      } else {
          viewHolderItem.soundImage.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
      }

      viewHolderItem.proIcon.setVisibility(sound.getPro() ? View.VISIBLE : View.INVISIBLE);

      viewHolderItem.soundImage.setOnClickListener(
              new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      listener.clicked(sound);
                  }
              });

      viewHolderItem.moreOptions.setOnClickListener(
              new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      listener.moreOptionsClicked(sound);
                  }
              });

      viewHolderItem.soundVolume.setOnSeekBarChangeListener(
              new SeekBar.OnSeekBarChangeListener() {
                  @Override
                  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                      Log.d(TAG, "onProgressChanged: current value: " + progress);
                      Sound sound = getItem(position);
                      listener.volumeChange(sound, progress);
                  }

                  @Override
                  public void onStartTrackingTouch(SeekBar seekBar) {
                  }

                  @Override
                  public void onStopTrackingTouch(SeekBar seekBar) {
                      Log.d(TAG, "onStopTrackingTouch() called");
                      // update ViewModel date only when user lets go of the progressbar, otherwise refreshing
                      // the view to many times results in stuttering
                      listener.volumeChangeStopped(sound, seekBar.getProgress());
                  }
              });

      return convertView;
  }

    public void refreshList(@NotNull ArrayList<Sound> sounds) {
        clear();
        for (Sound sound : sounds) {
            insert(sound, getCount());
        }

        notifyDataSetChanged();
    }

    private static class ViewHolderItem {

        private ImageView soundImage;
        private SeekBar soundVolume;
        private ConstraintLayout parentLayout;
        private ImageView proIcon;
        private ImageView moreOptions;
        private ProgressBar loading;
    }
}
