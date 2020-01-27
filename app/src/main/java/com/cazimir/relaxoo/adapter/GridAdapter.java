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
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.Sound;
import com.cazimir.relaxoo.ui.sound_grid.OnSoundClickListener;

import java.util.List;

public class GridAdapter extends ArrayAdapter<Sound> {

  private static final String TAG = "GridAdapter";
  private OnSoundClickListener listener;

  public GridAdapter(Context ctx, List<Sound> sounds, OnSoundClickListener listener) {
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

      // store the holder with the view.
      convertView.setTag(viewHolderItem);
    } else {
      // we've just avoided calling findviewbyid on resource everytime
      // just use the viewHolder
      viewHolderItem = (ViewHolderItem) convertView.getTag();
    }

    // object item based on the position
    final Sound sound = getItem(position);

    viewHolderItem.soundVolume.setProgress(Math.round(sound.volume() * 100));
    viewHolderItem.soundVolume.setVisibility(sound.isPlaying() ? View.VISIBLE : View.INVISIBLE);
      viewHolderItem.soundImage.setImageBitmap(BitmapFactory.decodeFile(sound.getLogoPath()));

      // because playing the sound refreshes the grid change color based on playing status
      if (sound.isPlaying()) {
          viewHolderItem.soundImage.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
      } else {
          viewHolderItem.soundImage.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
      }

      viewHolderItem.proIcon.setVisibility(sound.isPro() ? View.VISIBLE : View.INVISIBLE);

    viewHolderItem.parentLayout.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              listener.clicked(
                      sound.soundPoolId(), sound.isPlaying(), sound.streamId(), sound.isPro());
          }
        });

    viewHolderItem.soundVolume.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.d(TAG, "onProgressChanged: current value: " + progress);
            Sound sound = getItem(position);
            listener.volumeChange(sound.streamId(), progress);
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {}

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    return convertView;
  }

  private static class ViewHolderItem {

    private ImageView soundImage;
    private SeekBar soundVolume;
    private ConstraintLayout parentLayout;
    private ImageView proIcon;
  }
}
