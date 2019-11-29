package com.cazimir.relaxoo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cazimir.relaxoo.R;
import com.cazimir.relaxoo.model.Sound;

import java.util.ArrayList;

public class GridBaseAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context ctx;
    private ArrayList<Sound> soundList;
    private ImageView soundImage;
    private TextView soundName;

    public GridBaseAdapter(Context ctx, ArrayList<Sound> sounds) {

        this.ctx = ctx;
        this.soundList = sounds;
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
    public View getView(int position, View convertView, ViewGroup parent) {

        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(R.layout.grid_item, parent, false);

        soundImage = (ImageView) itemView.findViewById(R.id.soundImage);
        soundName = (TextView) itemView.findViewById(R.id.soundName);

        soundName.setText(soundList.get(position).name());

        return itemView;
    }
}
