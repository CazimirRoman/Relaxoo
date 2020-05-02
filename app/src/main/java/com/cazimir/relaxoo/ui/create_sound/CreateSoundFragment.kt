package com.cazimir.relaxoo.ui.create_sound

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.ScrollListenerRecycleView
import com.cazimir.relaxoo.adapter.RecordingAdapter
import com.cazimir.relaxoo.model.Recording
import com.cazimir.relaxoo.repository.RecordingRepository
import kotlinx.android.synthetic.main.create_sound_fragment.*
import kotlinx.android.synthetic.main.create_sound_fragment.view.*
import java.io.IOException

class CreateSoundFragment : Fragment() {

    private lateinit var viewModel: CreateSoundViewModel
    private lateinit var activityCallback: OnRecordingStarted
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var createSoundView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        createSoundView = inflater.inflate(R.layout.create_sound_fragment, container, false)
        createSoundView.recording_list.layoutManager = LinearLayoutManager(context)
        createSoundView.recording_list.adapter = RecordingAdapter(
                object : RecordingAdapter.OnItemClickListener {
                    override fun onPlayClicked(item: Recording?) {
                        playRecordedSound(item)
                    }

                    override fun onStopClicked() {
                        stopRecordedSound()
                    }

                    override fun onOptionsClicked(recording: Recording?) {
                        activityCallback.showBottomDialogForRecording(recording)
                    }
                },
                context,
                ArrayList())

        createSoundView.add_recording.setOnClickListener {
            onAddRecordingClicked()
        }
        return createSoundView
    }

    private fun stopRecordedSound() {
        Log.d(TAG, "stopRecordedSound() called")
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
        }
    }

    private fun playRecordedSound(recordedSound: Recording?) {
        val uri = Uri.fromFile(recordedSound?.file)
        try {
            mediaPlayer!!.setDataSource(context!!, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            mediaPlayer!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mediaPlayer!!.setOnCompletionListener {
            Log.d(TAG, "playRecordedSound() called with: finished")
            val adapter = recording_list.getAdapter() as RecordingAdapter
            adapter.finishedPlayingRecording(recordedSound)
        }
        mediaPlayer!!.start()
        Log.d(TAG, "playRecordedSound() called with: " + mediaPlayer!!.isPlaying)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: 22-Apr-20 use the factory thing here to provide the repository via the constructor
        viewModel = ViewModelProvider(this).get(CreateSoundViewModel::class.java)
        viewModel.repository = RecordingRepository()
        viewModel.refreshList()
        viewModel
                .recordingsLive
                .observe(
                        viewLifecycleOwner,
                        Observer<ArrayList<Recording>> { files: ArrayList<Recording> ->
                            Log.d(TAG, "onChanged() called with: files[]$files")
                            val adapter = createSoundView.recording_list.adapter as RecordingAdapter
                            adapter.setList(files)
                            createSoundView.no_recordings_text.visibility = if (files.size != 0) View.GONE else View.VISIBLE
                        })
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)

        recording_list.addOnScrollListener(object : ScrollListenerRecycleView(recording_list.layoutManager as LinearLayoutManager) {
            override fun onScrollFinished() {
            }

            override fun show() {
                add_recording.animate().translationX(0f).setInterpolator(DecelerateInterpolator(2f)).start()
            }

            override fun hide() {
                add_recording.animate().translationX(add_recording.width.toFloat() + 50f).setInterpolator(AccelerateInterpolator(2f)).start()

            }
        })
    }

    private fun onAddRecordingClicked() {
        Log.d(TAG, "onViewClicked() called")
        activityCallback.recordingStarted()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityCallback = context as MainActivity
    }

    fun updateList() {
        viewModel.refreshList()
    }

    fun deleteRecording(recording: Recording?) {
        viewModel.deleteRecording(recording!!)
    }

    fun renameRecording(recording: Recording?, newName: String?) {
        viewModel.editRecording(recording!!, newName!!)
    }

    companion object {
        private const val TAG = "CreateSoundFragment"
        fun newInstance(): CreateSoundFragment {
            return CreateSoundFragment()
        }
    }

    init {
        retainInstance = true
    }
}
