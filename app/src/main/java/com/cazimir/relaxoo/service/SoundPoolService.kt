package com.cazimir.relaxoo.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.SoundPool
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.application.MyApplication
import com.cazimir.relaxoo.eventbus.EventBusLoad
import com.cazimir.relaxoo.eventbus.EventBusLoadedToSoundPool
import com.cazimir.relaxoo.eventbus.EventBusPlay
import com.cazimir.relaxoo.eventbus.EventBusStop
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.service.events.*
import org.greenrobot.eventbus.EventBus

class SoundPoolService : Service(), ISoundPoolService {

    companion object {
        private const val TAG = "SoundPoolService"
        private const val MAX_SOUNDS = 99
        const val SOUND_POOL_ACTION = "sound_pool_action"

        fun getCommand(context: Context?, command: ISoundPoolCommand): Intent {
            val intent = Intent(context, SoundPoolService::class.java)
            intent.putExtra(SOUND_POOL_ACTION, command)
            return intent
        }
    }

    private lateinit var pendingIntent: PendingIntent
    private lateinit var soundPool: SoundPool

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "createOrGetSoundPool: called")

        val notificationIntent = Intent(this, MainActivity::class.java)
        this.pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        soundPool = SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0)

        soundPool.setOnLoadCompleteListener { soundPool: SoundPool?, soundPoolId: Int, status: Int ->
            Log.d(TAG, "onLoadComplete: " + soundPoolId)
            //broadcast to viewModel
            EventBus.getDefault().post(EventBusLoadedToSoundPool(soundPoolId))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val event = intent?.getSerializableExtra(SOUND_POOL_ACTION) as? ISoundPoolCommand

        when (event) {
            is StopServiceCommand -> {
                stopAllSounds().also { stopSelf() }
            }
            is LoadSoundsCommand -> load(event.sounds)
            is VolumeCommand -> setVolume(event.streamId, event.leftVolume, event.rightVolume)
            is PlayCommand -> play(event)
            is StopCommand -> stop(event)
            is ShowNotificationCommand -> toggleNotification(event.selected)
            else -> { // Note the block
                print("x is neither 1 nor 2")
            }
        }

        return START_NOT_STICKY
    }

    private fun toggleNotification(numberOfPlaying: Int) {

        if (numberOfPlaying == 0) {
            stopForeground(true)
        } else {
            val notification = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setContentTitle("SoundPoolService")
                .setContentText("Playing $numberOfPlaying sounds")
                .setSmallIcon(R.drawable.ic_delete)
                .setContentIntent(pendingIntent)
                .build()

            startForeground(1, notification)
        }
    }

    override fun load(sounds: ArrayList<Sound>) {

        val processedSounds = ArrayList<Sound>()

        for (sound: Sound in sounds) {
            if (sound.soundPoolId() == 0) { // add to arraylist with soundId from soundpool
                val soundId = soundPool.load(sound.filePath, 1)
                processedSounds.add(Sound.withSoundPoolId(sound, soundId))
            } else {
                processedSounds.add(sound)
            }
        }

        EventBus.getDefault().post(EventBusLoad(processedSounds))
    }

    override fun unload(sound: Sound) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun play(playCommand: PlayCommand) {
        Log.d(TAG, "play: called with: ${playCommand.soundPoolID}")
        val streamId = soundPool.play(
            playCommand.soundPoolID,
            playCommand.leftVolume,
            playCommand.rightVolume,
            playCommand.priority,
            playCommand.loop,
            playCommand.rate
        )
        EventBus.getDefault().post(EventBusPlay(playCommand.soundPoolID, streamId))
    }

    override fun stop(stopCommand: StopCommand) {
        Log.d(TAG, "stop: called with: ${stopCommand.soundPoolId}")
        soundPool.stop(stopCommand.streamId)
        EventBus.getDefault().post(EventBusStop(stopCommand.soundPoolId))
    }

    override fun stopAllSounds() {
        soundPool.release()
    }

    override fun setVolume(streamId: Int, leftVolume: Float, rightVolume: Float) {
        soundPool.setVolume(streamId, leftVolume, rightVolume)
    }
}