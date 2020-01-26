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
import com.cazimir.relaxoo.dialog.TimerDialog;
import com.cazimir.relaxoo.model.Recording;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;


public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {

  private static final String TAG = "RecordingAdapter";

  private final OnItemClickListener listener;
  private Context context;
  private ArrayList<Recording> list;
  private Recording currentlyPlaying;

  public RecordingAdapter(
          OnItemClickListener listener, Context context, ArrayList<Recording> list) {
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
    Recording item = list.get(position);

    Uri uri = Uri.parse(item.getFile().getPath());
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    mmr.setDataSource(context, uri);
    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    int millSecond = Integer.parseInt(durationStr);

    // replace the contents of the view with that element

    // get name without extension
    holder.recordingName.setText(FilenameUtils.removeExtension(item.getFile().getName()));
    holder.recordingDuration.setText("Duration: " + TimerDialog.getCountTimeByLong(millSecond));

    File file = new File(item.getFile().getPath());
    Date lastModDate = new Date(file.lastModified());
    holder.recordingCreated.setText("Created at: " + lastModDate.toString());

    holder.playRecording.setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {

                // if current item is playing, stop it
                if (item.isPlaying()) {
                  Recording recordingWithStop =
                          new Recording.Builder().withFile(item.getFile()).withPlaying(false).build();
                  list.set(list.indexOf(item), recordingWithStop);
                  notifyDataSetChanged();
                  listener.onStopClicked();
                  currentlyPlaying = null;
                  // stop currently playing sound
                } else {
                  listener.onStopClicked();

                  if (currentlyPlaying != null) {
                    Recording recordingWithStop =
                            new Recording.Builder()
                                    .withFile(currentlyPlaying.getFile())
                                    .withFileName(currentlyPlaying.getFile().getName())
                                    .withFileName(currentlyPlaying.getFile().getName())
                                    .withPlaying(false)
                                    .build();
                    list.set(list.indexOf(currentlyPlaying), recordingWithStop);
                    notifyDataSetChanged();
                    currentlyPlaying = null;
                  }

                  Recording recordingWithPlay =
                          new Recording.Builder()
                                  .withFile(item.getFile())
                                  .withFileName(item.getFile().getName())
                                  .withFileName(item.getFile().getName())
                                  .withPlaying(true)
                                  .build();
                  list.set(list.indexOf(item), recordingWithPlay);
                  currentlyPlaying = recordingWithPlay;
                  notifyDataSetChanged();
                  listener.onPlayClicked(recordingWithPlay);
                }
              }
            });

    holder.playRecording.setImageDrawable(
            (item.isPlaying())
                    ? context.getResources().getDrawable(R.drawable.ic_stop)
                    : context.getResources().getDrawable(R.drawable.ic_play));

    holder.optionsRecording.setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                listener.onOptionsClicked(list.get(position));
              }
            });
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  public void setList(ArrayList<Recording> files) {
    this.list = files;
    notifyDataSetChanged();
  }

  public void finishedPlayingRecording(Recording recordedSound) {
    Log.d(TAG, "finishedPlayingRecording() called");
    list.set(
            list.indexOf(recordedSound),
            new Recording.Builder()
                    .withFile(recordedSound.getFile())
                    .withFileName(recordedSound.getFile().getName())
                    .withPlaying(false)
                    .build());
    notifyDataSetChanged();
  }

  public interface OnItemClickListener {
    void onPlayClicked(Recording item);

    void onStopClicked();

    void onOptionsClicked(Recording recording);
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

    @BindView(R.id.options_recording)
    AppCompatImageButton optionsRecording;

    public ViewHolder(View view) {
      super(view);
      ButterKnife.bind(this, view);
    }
  }
}
