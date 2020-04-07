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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.application.MyApplication
import com.cazimir.relaxoo.eventbus.EventBusLoad
import com.cazimir.relaxoo.eventbus.EventBusLoadedToSoundPool
import com.cazimir.relaxoo.eventbus.EventBusPlayingSounds
import com.cazimir.relaxoo.eventbus.EventBusStop
import com.cazimir.relaxoo.model.PlayingSound
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

    private var playingSoundsList: ArrayList<PlayingSound> = ArrayList()
    private val playingSoundsListLive: MutableLiveData<ArrayList<PlayingSound>> = MutableLiveData()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "createOrGetSoundPool: called")

        setupNotifications()

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
            is VolumeCommand -> setVolume(event.id, event.streamId, event.leftVolume, event.rightVolume)
            is PlayCommand -> play(event)
            is StopCommand -> stop(event)
            is StopAllSoundsCommand -> stopAllSounds()
            is ShowNotificationCommand -> setupNotifications()
            is PlayingSoundsCommand -> sendPlayingSounds()
            else -> { // Note the block
                print("x is neither 1 nor 2")
            }
        }

        return START_STICKY
    }

    private fun sendPlayingSounds() {
        EventBus.getDefault().post(EventBusPlayingSounds(playingSoundsListLive))
    }

    private fun setupNotifications() {

        Log.d(TAG, "toggleNotification: called")

        playingSoundsListLive.observeForever(Observer { playingSounds ->
            if (playingSounds.size == 0) {
                stopForeground(true)
            } else {
                val notification = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                        .setContentTitle("SoundPoolService")
                        .setAutoCancel(false)
                        .setContentText(getNotificationText(playingSounds.size))
                        .setSmallIcon(R.drawable.ic_delete)
                        .setContentIntent(pendingIntent)
                        .build()

                startForeground(1, notification)
            }
        })
    }

    private fun getNotificationText(numberOfPlayingSounds: Int?): String {
        val label = numberOfPlayingSounds?.let { "sound".pluralize(it) }
        return "$numberOfPlayingSounds $label playing..."
    }

    private fun String.pluralize(count: Int): String? {
        return if (count > 1) {
            this + 's'
        } else {
            this
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

    // TODO: 28-Mar-20 take whole sound object for play command
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

        //the playing sounds list sends back an observable that is updated each time playing sounds is beeing updated.
        playingSoundsList.add(PlayingSound(playCommand.id, streamId, playCommand.leftVolume)).also { playingSoundsListLive.value = playingSoundsList }
    }

    override fun stop(stopCommand: StopCommand) {
        Log.d(TAG, "stop: called with: ${stopCommand.soundPoolId}")
        soundPool.stop(stopCommand.streamId)

        playingSoundsList.remove(PlayingSound(stopCommand.id, stopCommand.streamId, null)).also { playingSoundsListLive.value = playingSoundsList }

        EventBus.getDefault().post(EventBusStop(stopCommand.soundPoolId))
    }

    override fun stopAllSounds() {
        Log.d(TAG, "stopAllSounds: called")
        for (playingSound: PlayingSound in playingSoundsList) {
            soundPool.stop(playingSound.streamId)
            playingSoundsList.remove(playingSound)
        }

        playingSoundsListLive.value = playingSoundsList
    }

    override fun setVolume(id: String, streamId: Int, leftVolume: Float, rightVolume: Float) {
        soundPool.setVolume(streamId, leftVolume, rightVolume)
        val toBeReplaced = playingSoundsList.first { playingSound -> playingSound.id == id }
        val indexOf = playingSoundsList.indexOf(toBeReplaced)
        val replaceWith = toBeReplaced.copy(volume = leftVolume)

        playingSoundsList = playingSoundsList.toMutableList().apply { this[indexOf] = replaceWith } as ArrayList<PlayingSound>
    }
}