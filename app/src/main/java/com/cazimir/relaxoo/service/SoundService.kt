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
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.application.MyApplication
import com.cazimir.relaxoo.dialog.timer.TimerDialog
import com.cazimir.relaxoo.eventbus.EventBusLoad
import com.cazimir.relaxoo.eventbus.EventBusLoadSingle
import com.cazimir.relaxoo.eventbus.EventBusLoadedToSoundPool
import com.cazimir.relaxoo.eventbus.EventBusPlayingSounds
import com.cazimir.relaxoo.eventbus.EventBusServiceDestroyed
import com.cazimir.relaxoo.eventbus.EventBusStop
import com.cazimir.relaxoo.eventbus.EventBusStopAll
import com.cazimir.relaxoo.eventbus.EventBusTimer
import com.cazimir.relaxoo.eventbus.EventBusUnload
import com.cazimir.relaxoo.model.PlayingSound
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.service.commands.ISoundServiceCommand
import com.cazimir.relaxoo.service.commands.LoadCustomSoundCommand
import com.cazimir.relaxoo.service.commands.LoadSoundsCommand
import com.cazimir.relaxoo.service.commands.MuteAllSoundsCommand
import com.cazimir.relaxoo.service.commands.PlayCommand
import com.cazimir.relaxoo.service.commands.PlayingSoundsCommand
import com.cazimir.relaxoo.service.commands.ShowNotificationCommand
import com.cazimir.relaxoo.service.commands.StopAllSoundsCommand
import com.cazimir.relaxoo.service.commands.StopCommand
import com.cazimir.relaxoo.service.commands.StopServiceCommand
import com.cazimir.relaxoo.service.commands.TimerTextCommand
import com.cazimir.relaxoo.service.commands.ToggleCountDownTimerCommand
import com.cazimir.relaxoo.service.commands.TriggerComboCommand
import com.cazimir.relaxoo.service.commands.UnloadSoundCommand
import com.cazimir.relaxoo.service.commands.VolumeCommand
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit


class SoundService : Service(), ISoundService {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationView: RemoteViews
    private var muted = false
    private val _muted: MutableLiveData<Boolean> = MutableLiveData(false)
    private var timerRunning = false
    private var _timerRunning: MutableLiveData<Boolean> = MutableLiveData()
    private val _timerText: MutableLiveData<String> = MutableLiveData("")
    private var countDownTimer: CountDownTimer? = null
    private var timerTextEnding = ""

    companion object {
        private const val TAG = "SoundPoolService"
        private const val MAX_SOUNDS = 999
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
            is LoadSoundsCommand -> loadToSoundPool(event.sounds)
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
            is MuteAllSoundsCommand -> toggleMute()
            else -> { // Note the block
                print("x is neither 1 nor 2")
            }
        }

        return START_STICKY
    }

    private fun toggleMute() {
        muted = !muted
        _muted.value = muted
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "createOrGetSoundPool: called")

        setupNotifications()

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

        _muted.observeForever(Observer {
            if (it) {
                soundPool.autoPause()
            } else {
                soundPool.autoResume()
            }
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

        this.notificationView = RemoteViews(packageName, R.layout.custom_notification)

        val notificationIntent = Intent(this, MainActivity::class.java)
        this.pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val stopAllIntent = getCommand(this, StopAllSoundsCommand())
        val muteIntent = getCommand(this, MuteAllSoundsCommand())

        val stopAllPendingIntent = PendingIntent.getService(this, 0, stopAllIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val mutePendingIntent = PendingIntent.getService(this, 1, muteIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        createNotificationBuilder()

        playingSoundsListLive.observeForever(Observer { playingSounds ->
            if (playingSounds.size == 0) {
                //stopForeground(true)
                notificationView.setImageViewResource(
                    R.id.remote_view_play_stop,
                    R.drawable.ic_play_black
                )
                notificationView.setTextViewText(R.id.remote_view_playing_txt, "No sound playing")
                this.notificationBuilder.setCustomContentView(notificationView)
                triggerNotificationRefresh()
            } else {
                notificationView.setTextViewText(
                    R.id.remote_view_playing_txt,
                    getNotificationText(playingSounds.size)
                )
                notificationView.setOnClickPendingIntent(
                    R.id.remote_view_play_stop,
                    stopAllPendingIntent
                )
                notificationView.setOnClickPendingIntent(R.id.remote_view_mute, mutePendingIntent)

                val notification = this.notificationBuilder
                        .build()

                startForeground(1, notification)

                _muted.observeForever {
                    if (it) {
                        notificationView.setImageViewResource(R.id.remote_view_mute, R.drawable.ic_mute_on_black)
                        this.notificationBuilder.setCustomContentView(notificationView)
                        // this whole thing is done because of this : https://stackoverflow.com/questions/25821903/change-android-notification-text-dynamically
                        // I need to update the builder with the updated view and then retrigger the notification makings sure the builder has this flag : .setOnlyAlertOnce(true)
                        triggerNotificationRefresh()
                    } else {
                        notificationView.setImageViewResource(
                            R.id.remote_view_mute,
                            R.drawable.ic_mute_off_black
                        )
                        this.notificationBuilder.setCustomContentView(notificationView)
                        triggerNotificationRefresh()
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        // also kill activity if used decides to kill service so that everything is recreated on relaunch
        Log.d(TAG, "onDestroy: called")
        EventBus.getDefault().post(EventBusServiceDestroyed())
        super.onDestroy()
    }

    private fun triggerNotificationRefresh() {
        startForeground(1, this.notificationBuilder.build())
    }

    private fun createNotificationBuilder() {
        this.notificationBuilder = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setContentTitle("SoundPoolService")
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_delete)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationView)
                .setContentIntent(pendingIntent)
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

    override fun loadToSoundPool(sounds: ArrayList<Sound>?) {

        var processedSounds = mutableListOf<Sound>()

        sounds?.mapTo(processedSounds, { sound ->
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

    fun stopAll(view: View) {
        stopAllSounds()
    }
}