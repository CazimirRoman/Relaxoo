package com.cazimir.relaxoo.adapter;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.cazimir.relaxoo.R;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {

    private static final String TAG = "RecordingAdapter";

    private final OnItemClickListener listener;
    private Context context;
    private List<File> list;

    public RecordingAdapter(OnItemClickListener listener, Context context, List<File> list) {
        this.listener = listener;
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_recording, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get element from your dataset at this position
        File item = list.get(position);

        Uri uri = Uri.parse(item.getPath());
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int millSecond = Integer.parseInt(durationStr);

        // replace the contents of the view with that element
        holder.recordingName.setText(durationStr);

        holder.playRecording.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

        holder.deleteRecording.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onDeleteClicked(list.get(position));
                        Log.d(TAG, "onClick() called with: ");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onDeleteClicked(File file);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.play_recording)
        AppCompatImageButton playRecording;

        @BindView(R.id.recording_name)
        TextView recordingName;

        @BindView(R.id.recording_duration)
        TextView recordingDuration;

        @BindView(R.id.recording_created)
        TextView recordingCreated;

        @BindView(R.id.delete_recording)
        AppCompatImageButton deleteRecording;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
