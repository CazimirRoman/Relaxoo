package com.cazimir.relaxoo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.Sound;
import com.cazimir.relaxoo.ui.sound_grid.OnSoundClickListener;

import java.util.ArrayList;

public class GridBaseAdapter extends BaseAdapter {

  private static final String TAG = "GridBaseAdapter";
  private LayoutInflater inflater;
  private Context ctx;
  private ArrayList<Sound> soundList;
  private OnSoundClickListener listener;

  public GridBaseAdapter(Context ctx, ArrayList<Sound> sounds, OnSoundClickListener listener) {

    this.ctx = ctx;
    this.soundList = sounds;
    this.listener = listener;
  }

  @Override
  public int getCount() {
    return soundList.size();
  }

  @Override
  public Object getItem(int position) {
    return soundList.get(position);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {

    final ViewHolderItem viewHolderItem;

    if (convertView == null) {

      // inflate de layout
      LayoutInflater inflater =
          (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      if (inflater != null) {
        convertView = inflater.inflate(R.layout.grid_item, parent, false);
      }

      // will set up the ViewHolder

      viewHolderItem = new ViewHolderItem();
      viewHolderItem.soundImage = convertView.findViewById(R.id.sound_image);
      viewHolderItem.soundVolume = convertView.findViewById(R.id.sound_volume);
      viewHolderItem.cl = convertView.findViewById(R.id.cl);



      // store the holder with the view.
      convertView.setTag(viewHolderItem);
    } else {
      // we've just avoided calling findviewbyid on resource everytime
      // just use the viewHolder
      viewHolderItem = (ViewHolderItem) convertView.getTag();
    }

    // object item based on the position
    final Sound sound = soundList.get(position);

    // assign values if the object is not null

    if (sound != null) {
      viewHolderItem.soundImage.setImageResource(R.drawable.ic_windy);
    }

    viewHolderItem.cl.setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {

                viewHolderItem.soundVolume.setProgress(sound.getVolume());

                viewHolderItem.soundVolume.setVisibility(
                        viewHolderItem.soundVolume.getVisibility() == View.VISIBLE
                                ? View.INVISIBLE
                                : View.VISIBLE);
                listener.clicked(sound.isPlaying());
                soundList.get(position).setPlaying(!sound.isPlaying());

              }
            });

    viewHolderItem.soundVolume.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.d(TAG, "onProgressChanged: current value: " + progress);
            soundList.get(position).setVolume(progress);
            listener.volumeChange(progress);
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
    private ConstraintLayout cl;
  }
}
