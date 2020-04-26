package com.cazimir.relaxoo.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.recyclerview.widget.RecyclerView
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.ui.sound_grid.OnSoundClickListener
import com.cazimir.utilitieslibrary.inflate
import kotlinx.android.synthetic.main.grid_item.view.*


class GridRecyclerViewAdapter(private var sounds: List<Sound>, private val listener: OnSoundClickListener) : RecyclerView.Adapter<GridRecyclerViewAdapter.SoundHolder>() {

    companion object {
        private const val TAG = "StaggeredRecyclerViewAd"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundHolder {
        val inflatedView = parent.inflate(R.layout.grid_item, false)
        return SoundHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return sounds.size
    }

    override fun onBindViewHolder(holder: SoundHolder, position: Int) {
        val sound = sounds[position]

        holder.itemView.sound_name.text = sound.name
        holder.itemView.sound_volume.progress = Math.round(sound.volume * 100)
        holder.itemView.sound_volume.visibility = if (sound.playing) View.VISIBLE else View.INVISIBLE
        holder.itemView.more_options.visibility = if (sound.custom) View.VISIBLE else View.INVISIBLE
        holder.itemView.sound_image.setImageBitmap(BitmapFactory.decodeFile(sound.logoPath))

        // hide progressbar once sound loaded to soundpool
        holder.itemView.grid_item_loading.visibility = if (!sound.loaded) View.VISIBLE else View.GONE

        // because playing the sound refreshes the grid change color based on playing status
        if (sound.playing) {
            holder.itemView.sound_image.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        } else {
            holder.itemView.sound_image.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN)
        }

        holder.itemView.pro_icon.visibility = if (sound.pro) View.VISIBLE else View.INVISIBLE

        holder.itemView.sound_image.setOnClickListener { listener.clicked(sound) }

        holder.itemView.more_options.setOnClickListener { listener.moreOptionsClicked(sound) }

        holder.itemView.sound_volume.setOnSeekBarChangeListener(
                object : OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        Log.d(TAG, "onProgressChanged: current value: $progress")
                        listener.volumeChange(sound, progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        Log.d(TAG, "onStopTrackingTouch() called")
                        // update ViewModel date only when user lets go of the progressbar, otherwise refreshing
                        // the view to many times results in stuttering
                        listener.volumeChangeStopped(sound, seekBar.progress)
                    }
                })
    }

    fun refreshList(newSounds: List<Sound>) {
        sounds = newSounds
        notifyDataSetChanged()
    }


    class SoundHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

        }
    }
}