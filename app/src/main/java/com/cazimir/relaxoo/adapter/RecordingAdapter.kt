package com.cazimir.relaxoo.adapter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.dialog.timer.TimerDialog
import com.cazimir.relaxoo.model.Recording
import kotlinx.android.synthetic.main.item_recording.view.*
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecordingAdapter(
        private val listener: OnItemClickListener,
        private val context: Context?,
        private var list: ArrayList<Recording>
) : RecyclerView.Adapter<RecordingAdapter.ViewHolder>() {

    private var currentlyPlaying: Recording? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_recording, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // get element from your dataset at this position
        val item = list[position]
        val uri = Uri.parse(item.file.path)
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(context, uri)
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val millSecond = durationStr.toInt()

        // replace the contents of the view with that element

        // get name without extension
        holder.recordingName.text = FilenameUtils.removeExtension(item.file.name)
        holder.recordingDuration.text = context?.getString(R.string.recording_duration, TimerDialog.getCountTimeByLong(millSecond.toLong()))
        val file = File(item.file.path)
        val lastModDate = Date(file.lastModified())

        val pattern = "yyyy-MM-dd HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(lastModDate)


        holder.recordingCreated.text = context?.getString(R.string.recording_created, date)
        holder.playRecording.setOnClickListener {
            // if current item is playing, stop it
            if (item.isPlaying) {
                val recordingWithStop = Recording.Builder().withFile(item.file).withPlaying(false).build()
                list[list.indexOf(item)] = recordingWithStop
                notifyDataSetChanged()
                listener.onStopClicked()
                currentlyPlaying = null
                // stop currently playing sound
            } else {
                listener.onStopClicked()
                if (currentlyPlaying != null) {
                    val recordingWithStop = Recording.Builder()
                            .withId(currentlyPlaying!!.id)
                            .withFile(currentlyPlaying!!.file)
                            .withFileName(currentlyPlaying!!.file.name)
                            .withFileName(currentlyPlaying!!.file.name)
                            .withPlaying(false)
                            .build()
                    list[list.indexOf(currentlyPlaying as Recording)] = recordingWithStop
                    notifyDataSetChanged()
                    currentlyPlaying = null
                }
                val recordingWithPlay = Recording.Builder()
                        .withId(item.id)
                        .withFile(item.file)
                        .withFileName(item.file.name)
                        .withFileName(item.file.name)
                        .withPlaying(true)
                        .build()
                list[list.indexOf(item)] = recordingWithPlay
                currentlyPlaying = recordingWithPlay
                notifyDataSetChanged()
                listener.onPlayClicked(recordingWithPlay)
            }
        }
        holder.playRecording.setImageDrawable(
                if (item.isPlaying) context?.resources?.getDrawable(R.drawable.ic_stop_white) else context?.resources?.getDrawable(R.drawable.ic_play))
        holder.optionsRecording.setOnClickListener {
            listener.onOptionsClicked(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(files: ArrayList<Recording>) {
        list = files
        notifyDataSetChanged()
    }

    fun finishedPlayingRecording(recordedSound: Recording?) {
        list[list.indexOf(recordedSound)] = Recording.Builder()
                .withFile(recordedSound?.file)
                .withFileName(recordedSound?.file?.name)
                .withPlaying(false)
                .build()
        currentlyPlaying = null
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onPlayClicked(item: Recording?)
        fun onStopClicked()
        fun onOptionsClicked(recording: Recording?)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recordingName: AppCompatTextView = view.recording_name
        val recordingDuration: AppCompatTextView = view.recording_duration
        val recordingCreated: AppCompatTextView = view.recording_created
        val playRecording: AppCompatImageButton = view.play_recording
        val optionsRecording: AppCompatImageButton = view.options_recording
    }

    companion object {
        private const val TAG = "RecordingAdapter"
    }
}
