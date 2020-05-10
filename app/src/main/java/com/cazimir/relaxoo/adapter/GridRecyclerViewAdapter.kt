package com.cazimir.relaxoo.adapter

import android.content.Context
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
import kotlin.math.roundToInt


class GridRecyclerViewAdapter(val context: Context, var sounds: ArrayList<Sound>, private val listener: OnSoundClickListener) : RecyclerView.Adapter<GridRecyclerViewAdapter.SoundHolder>() {

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
        holder.itemView.sound_volume.progress = (sound.volume * 100).roundToInt()
        holder.itemView.sound_volume.visibility = if (sound.playing) View.VISIBLE else View.INVISIBLE
        holder.itemView.more_options.visibility = if (sound.custom) View.VISIBLE else View.INVISIBLE

        val logo = if (sound.custom) BitmapFactory.decodeResource(context.resources,
                R.drawable.custom) else BitmapFactory.decodeFile(sound.logoPath)

        holder.itemView.sound_image.setImageBitmap(logo)

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

                        if (fromUser) {
                            val soundWithNewVolume = sounds.find { foundSound -> sound.id == foundSound.id }?.copy(volume = (progress.toDouble() / 100).toString().toFloat())

                            //update the sounds list here as well
                            soundWithNewVolume?.let {
                                sounds[sounds.indexOf(soundWithNewVolume)] = soundWithNewVolume
                            }

                            listener.volumeChange(sound, progress)
                        }
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
        if (sounds.isNotEmpty()) {
            for (sound: Sound in newSounds) {
                modifySingleSoundInList(sound)
            }
        } else {
            sounds = newSounds as ArrayList<Sound>
            notifyDataSetChanged()
        }
    }

    fun modifySingleSoundInList(sound: Sound) {
        // cannot use indexOf because of substract method previously. nned to override equals
        for ((index, value) in sounds.withIndex()) {
            if (value.id == sound.id) {
                sounds[index] = sound
                notifyItemChanged(index)
                break
            }
        }
    }

    /*Used when deleting a pinned custom sound on the dashboard*/
    fun removeSingleSoundInList(sound: Sound) {
        val index = sounds.indexOf(sound)
        sounds.remove(sound)
        notifyItemRemoved(index)
    }

    class SoundHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

        }
    }
}