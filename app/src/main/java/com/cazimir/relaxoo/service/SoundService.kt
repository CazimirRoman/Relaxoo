package com.cazimir.relaxoo.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.SoundPool
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.application.MyApplication
import com.cazimir.relaxoo.dialog.timer.TimerDialog
import com.cazimir.relaxoo.eventbus.*
import com.cazimir.relaxoo.model.PlayingSound
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.service.commands.*
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

class SoundService : Service(), ISoundService {

    private var timerRunning = false
    private var _timerRunning: MutableLiveData<Boolean> = MutableLiveData()
    private val _timerText: MutableLiveData<String> = MutableLiveData("")
    private var countDownTimer: CountDownTimer? = null
    private var timerTextEnding = ""

    companion object {
        private const val TAG = "SoundPoolService"
        private const val MAX_SOUNDS = 99
        const val SOUND_POOL_ACTION = "sound_pool_action"

        fun getCommand(context: Context?, command: ISoundServiceCommand): Intent {
            val intent = Intent(context, SoundService::class.java)
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val event = intent?.getSerializableExtra(SOUND_POOL_ACTION) as? ISoundServiceCommand

        when (event) {
            is StopServiceCommand -> {
                stopAllSounds().also { stopSelf() }
            }
            is LoadSoundsCommand -> load(event.sounds)
            is UnloadSoundCommand -> unload(event.id, event.soundPoolId)
            is VolumeCommand -> setVolume(event.id, event.streamId, event.leftVolume, event.rightVolume)
            is PlayCommand -> play(event)
            is StopCommand -> stop(event)
            is StopAllSoundsCommand -> stopAllSounds()
            is ShowNotificationCommand -> setupNotifications()
            is PlayingSoundsCommand -> sendPlayingSounds()
            is TriggerComboCommand -> triggerCombo(event.soundList)
            is ToggleCountDownTimerCommand -> toggleCountDownTimer(event.minutes)
            is TimerTextCommand -> sendTimerText()
            is LoadCustomSoundCommand -> loadCustomSound(event.sound)
            else -> { // Note the block
                print("x is neither 1 nor 2")
            }
        }

        return START_STICKY
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

        playingSoundsListLive.observeForever(Observer {
            timerTextEnding = if (it.size > 1) {
                "s"
            } else {
                ""
            }
        })

        _timerRunning.observeForever(Observer {
            timerRunning = it
        })
    }

    private fun toggleCountDownTimer(minutes: Int) {
        if (timerRunning) {
            countDownTimer?.cancel()
            _timerRunning.value = false
        } else {
            countDownTimer = object : CountDownTimer(TimeUnit.MINUTES.toMillis(minutes.toLong()), 1000) {
                override fun onTick(millisUntilFinished: Long) { // updateLiveDataHere() observe from Fragment
                    // timerText is the observable that is being observed from the fragment
                    Log.d(TAG, "onTick: called with: ${_timerText.value}")
                    _timerText.value = String.format("Sound%s will stop in " +
                            TimerDialog.getCountTimeByLong(millisUntilFinished),
                            timerTextEnding)
                }

                override fun onFinish() { // live data observe timer finished
                    Log.d(TAG, "CountDownTimer finished")
                    _timerRunning.value = false
                    stopAllSounds()
                }
            }.start()

            _timerRunning.value = true
        }

        EventBus.getDefault().post(EventBusTimer(_timerRunning, _timerText))
    }

    private fun triggerCombo(soundList: List<Sound>) {
        stopAllSounds()
        soundList.forEach { sound -> play(PlayCommand(sound.id, sound.soundPoolId, sound.streamId, sound.volume, sound.volume, 0, -1, 1f)) }
    }

    // TODO: 11-Apr-20 I should bundle these in a single EventBus update
    private fun sendPlayingSounds() {
        EventBus.getDefault().post(EventBusPlayingSounds(playingSoundsListLive))
    }

    private fun sendTimerText() {
        if (timerRunning) {
            EventBus.getDefault().post(EventBusTimer(_timerRunning, _timerText))
        }
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

        var processedSounds = mutableListOf<Sound>()

        sounds.mapTo(processedSounds, { sound ->
            if (sound.soundPoolId == -1) {
                val soundPoolId = soundPool.load(sound.filePath, 1)
                sound.copy(soundPoolId = soundPoolId)
            } else {
                sound
            }
        })

        EventBus.getDefault().post(EventBusLoad(processedSounds as ArrayList<Sound>))
    }

    private fun loadCustomSound(sound: Sound) {
        val soundWithSoundPoolId = sound.copy(soundPoolId = soundPool.load(sound.filePath, 1))
        EventBus.getDefault().post(EventBusLoadSingle(soundWithSoundPoolId))
    }

    override fun unload(soundId: String, soundPoolId: Int) {
        soundPool.unload(soundPoolId)
        EventBus.getDefault().post(EventBusUnload(soundId, soundPoolId))
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

        playingSoundsList.remove(PlayingSound(stopCommand.id, stopCommand.streamId)).also { playingSoundsListLive.value = playingSoundsList }

        EventBus.getDefault().post(EventBusStop(stopCommand.soundPoolId))
    }

    override fun stopAllSounds() {
        Log.d(TAG, "stopAllSounds in service: called")

        for (playingSound: PlayingSound in playingSoundsList) {
            soundPool.stop(playingSound.streamId)
        }

        playingSoundsList.clear().also { playingSoundsListLive.value = playingSoundsList }

        countDownTimer?.cancel().also { _timerRunning.value = false }

        EventBus.getDefault().post(EventBusStopAll())
        // subscribe with a method here that receives the stopall event and does not allow the play to be run if observable not in certain state
    }

    override fun setVolume(id: String, streamId: Int, leftVolume: Float, rightVolume: Float) {
        soundPool.setVolume(streamId, leftVolume, rightVolume)
        val toBeReplaced = playingSoundsList.first { playingSound -> playingSound.id == id }
        val indexOf = playingSoundsList.indexOf(toBeReplaced)
        val replaceWith = toBeReplaced.copy(volume = leftVolume)

        playingSoundsList = playingSoundsList.toMutableList().apply { this[indexOf] = replaceWith } as ArrayList<PlayingSound>
    }
}