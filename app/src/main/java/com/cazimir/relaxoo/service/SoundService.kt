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
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.cazimir.relaxoo.MainActivity
import com.cazimir.relaxoo.R
import com.cazimir.relaxoo.application.MyApplication
import com.cazimir.relaxoo.dialog.timer.TimerDialog
import com.cazimir.relaxoo.eventbus.*
import com.cazimir.relaxoo.model.Sound
import com.cazimir.relaxoo.service.commands.*
import com.cazimir.utilitieslibrary.pluralize
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

class SoundService : Service(), ISoundService {

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

    private var allSounds: MutableList<Sound> = mutableListOf()

    private val _muted: MutableLiveData<Boolean> = MutableLiveData()
    private val _allSoundsLive: MutableLiveData<List<Sound>> = MutableLiveData()
    private val _playingSounds: LiveData<ArrayList<Sound>> = Transformations.map(_allSoundsLive)
    { soundsList ->
        soundsList.filter { sound -> sound.playing } as ArrayList<Sound>
    }
    private val _timerRunning: MutableLiveData<Boolean> = MutableLiveData()
    private val _timerText: MutableLiveData<String> = MutableLiveData("")
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationView: RemoteViews
    private var muted = false
    private var timerRunning = false
    private var countDownTimer: CountDownTimer? = null
    private var timerTextEnding = ""
    private lateinit var pendingIntent: PendingIntent
    private lateinit var soundPool: SoundPool

    //    private var playingSoundsList: ArrayList<Sound> = ArrayList()
    private var playingSoundsListCached: ArrayList<Sound> = ArrayList()


    private val textEndingObserver: Observer<List<Sound>> = Observer {
        timerTextEnding = if (it.size > 1) {
            "s"
        } else {
            ""
        }
    }

    private val timerRunningObserver: Observer<Boolean> = Observer {
        timerRunning = it
    }

    private val mutedObserver: Observer<Boolean> = Observer {
        if (it) {
            soundPool.autoPause()
        } else {
            soundPool.autoResume()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val event = intent?.getSerializableExtra(SOUND_POOL_ACTION) as? ISoundServiceCommand

        Log.d(TAG, "onStartCommand: called with: $event")

        when (event) {
            is StopServiceCommand -> {
                stopAllSounds().also { stopSelf() }
            }
            is LoadSoundsCommand -> loadToSoundPool(event.sounds)
            is UnloadSoundCommand -> unload(event.sound)
            is VolumeCommand -> setVolume(
                    event.id,
                    event.streamId,
                    event.leftVolume,
                    event.rightVolume
            )
            is PlayCommand -> play(event)
            is StopCommand -> stop(event)
            is StopAllSoundsCommand -> stopAllSounds()
            is ShowNotificationCommand -> setupNotifications()
            is AllSoundsCommand -> sendAllSounds()
            is TriggerComboCommand -> triggerCombo(event.soundList, event.boughtPro)
            is ToggleCountDownTimerCommand -> toggleCountDownTimer(event.minutes)
            is TimerTextCommand -> sendTimerText()
            is LoadCustomSoundCommand -> loadCustomSound(event.sound)
            is ToggleMuteCommand -> toggleMute()
            is TogglePlayStopCommand -> togglePlayStopFromNotification()
            is MuteStatusCommand -> sendMuteStatus()
            else -> { // Note the block
                print("x is neither 1 nor 2")
            }
        }

        return START_STICKY
    }

    private fun sendMuteStatus() {
        EventBus.getDefault().post(EventBusMuteStatus(_muted))
    }

    private fun togglePlayStopFromNotification() {
        if (allSounds.any { it.playing }) {
            stopAllSounds()
        } else {
            playingSoundsListCached.forEach { sound -> play(PlayCommand(sound)) }
        }
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
            Log.d(TAG, "onLoadComplete: $soundPoolId")

            val soundWithSoundPoolId = allSounds.find { sound ->
                sound.soundPoolId == soundPoolId
            }

            //replace sound in list with loaded = true

            for ((index, value) in allSounds.withIndex()) {
                if (value.id == soundWithSoundPoolId?.id) {
                    allSounds[index] = soundWithSoundPoolId.copy(loaded = true)
                    break
                }
            }

//            allSounds[allSounds.indexOf(soundWithSoundPoolId)] = soundWithSoundPoolId!!.copy(loaded = true)

            _allSoundsLive.value = allSounds

            EventBus.getDefault().post(EventBusLoad(soundWithSoundPoolId!!.copy(loaded = true)))
        }


        // observe playing sounds to change text endings on timer text
        _playingSounds.observeForever(textEndingObserver)
        // observe playing sounds to cancel any running timer
        _playingSounds.observeForever {
            if (it.isEmpty()) {
                countDownTimer?.cancel()
                _timerRunning.value = false
            }
        }


        _timerRunning.observeForever(timerRunningObserver)
        _muted.observeForever(mutedObserver)
    }

    private fun MutableList<Sound>.hasAllSoundsLoaded(): Boolean {

        var allLoaded = true

        forEach {
            if (!it.loaded) {
                allLoaded = false
            }
        }

        return allLoaded
    }

    fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
        return map {
            if (block(it)) newValue else it
        }
    }

    private fun toggleCountDownTimer(minutes: Int) {
        Log.d(TAG, "toggleCountDownTimer: called")
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

    private fun triggerCombo(soundList: List<Sound>, boughtPro: Boolean) {
        Log.d(TAG, "triggerCombo: called")
        stopAllSounds()
        soundList.forEach { sound ->

            if (boughtPro) {
                play(PlayCommand(sound))
            } else if (sound.pro.not()) {
                play(PlayCommand(sound))
            }

        }
    }

    // TODO: 11-Apr-20 I should bundle these in a single EventBus update
    private fun sendAllSounds() {
        EventBus.getDefault().post(EventBusAllSounds(_allSoundsLive))
    }

    private fun sendTimerText() {
        Log.d(TAG, "sendTimerText: called")
        if (timerRunning) {
            EventBus.getDefault().post(EventBusTimer(_timerRunning, _timerText))
        }
    }

    private fun setupNotifications() {

        this.notificationView = RemoteViews(packageName, R.layout.custom_notification)
        val notificationIntent = Intent(this, MainActivity::class.java)
        this.pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val togglePlayStop = getCommand(this, TogglePlayStopCommand())
        val muteIntent = getCommand(this, ToggleMuteCommand())
        val closeIntent = getCommand(this, StopServiceCommand())

        val togglePlayStopIntent =
                PendingIntent.getService(this, 0, togglePlayStop, PendingIntent.FLAG_UPDATE_CURRENT)
        val mutePendingIntent =
                PendingIntent.getService(this, 1, muteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val closePendingIntent =
                PendingIntent.getService(this, 2, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        createNotificationBuilder()

        _playingSounds.observeForever { playingSounds ->
            if (playingSounds.isEmpty()) {
                // stopForeground(true)
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
                notificationView.setImageViewResource(
                        R.id.remote_view_play_stop,
                        R.drawable.ic_stop_black
                )
                notificationView.setOnClickPendingIntent(
                        R.id.remote_view_play_stop,
                        togglePlayStopIntent
                )
                notificationView.setOnClickPendingIntent(R.id.remote_view_mute, mutePendingIntent)
                notificationView.setOnClickPendingIntent(R.id.remote_view_close, closePendingIntent)

                val notification = this.notificationBuilder
                        .build()

                startForeground(1, notification)
            }
        }

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

    override fun onDestroy() {
        // also kill activity if used decides to kill service so that everything is recreated on relaunch
        Log.d(TAG, "onDestroy: called")
        EventBus.getDefault().post(EventBusServiceDestroyed())
        _allSoundsLive.removeObserver { textEndingObserver }
        _timerRunning.removeObserver { timerRunningObserver }
        _muted.removeObserver { mutedObserver }
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
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationView)
                .setContentIntent(pendingIntent)
    }

    private fun getNotificationText(numberOfPlayingSounds: Int?): String {
        val label = numberOfPlayingSounds?.let { "sound".pluralize(it) }
        return "$numberOfPlayingSounds $label playing..."
    }

    //entry point where the sounds are coming in to the service
    override fun loadToSoundPool(sounds: List<Sound>) {
        Log.d(TAG, "loadToSoundPool: called with sounds: $sounds")

        allSounds.clear()
        // add to processed sounds with soundPoolId and loaded
        // soundPool ID is returned but that does not mean that the sound has been loaded yet. we need to wait for the callback
        sounds.mapTo(allSounds, { sound ->
            if (sound.soundPoolId == -1) {
                val soundPoolId = soundPool.load(sound.filePath, 1)
                Log.d(TAG, "loadToSoundPool in Service: called")
                sound.copy(soundPoolId = soundPoolId)
            } else {
                sound
            }
        })
    }

    private fun loadCustomSound(sound: Sound) {
        Log.d(TAG, "loadCustomSound: called")
        val soundWithSoundPoolId = sound.copy(soundPoolId = soundPool.load(sound.filePath, 1))
        EventBus.getDefault().post(EventBusLoadSingle(soundWithSoundPoolId))
    }

    override fun unload(sound: Sound) {
        Log.d(TAG, "unload: called with: soundId: $sound.id and soundPoolId: ${sound.soundPoolId}")
        stop(StopCommand(sound))
        soundPool.unload(sound.soundPoolId)
        EventBus.getDefault().post(EventBusUnload(sound))
    }

    // TODO: 28-Mar-20 take whole sound object for play command
    override fun play(playCommand: PlayCommand) {
        Log.d(TAG, "play: called with: ${playCommand.sound}")
        val streamId = soundPool.play(
                playCommand.sound.soundPoolId,
                playCommand.sound.volume,
                playCommand.sound.volume,
                0,
                -1,
                1f
        )

        // if mute is active new playing sounds should also be paused
        if (muted) soundPool.autoPause()

        val newSoundWithStreamId = playCommand.sound.copy(streamId = streamId, playing = true)

        // the playing sounds list sends back an observable that is updated each time playing sounds is beeing updated.

        for ((index, value) in allSounds.withIndex()) {
            if (value.id == newSoundWithStreamId.id) {
                allSounds[index] = newSoundWithStreamId
                break
            }
        }


//        allSounds[allSounds.indexOf(newSoundWithStreamId)] = newSoundWithStreamId


        _allSoundsLive.value = allSounds

//        playingSoundsList.add(newSoundWithStreamId)
//                .also { _playingSoundsListLive.value = playingSoundsList }
    }

    override fun stop(stopCommand: StopCommand) {
        Log.d(TAG, "stop: called with: ${stopCommand.sound}")
        soundPool.stop(stopCommand.sound.streamId)


        val newStoppedSound = stopCommand.sound.copy(streamId = -1, playing = false)


        for ((index, value) in allSounds.withIndex()) {
            if (value.id == newStoppedSound.id) {
                allSounds[index] = newStoppedSound
                break
            }
        }

//        allSounds[allSounds.indexOf(newStoppedSound)] = newStoppedSound

        _allSoundsLive.value = allSounds

//        playingSoundsList.remove(stopCommand.sound)
//                .also { _playingSoundsListLive.value = playingSoundsList }

        EventBus.getDefault().post(EventBusStop(stopCommand.sound))
    }

    override fun stopAllSounds() {
        Log.d(TAG, "stopAllSounds in service: called")

        for (playingSound: Sound in allSounds.filter { it.playing }) {
            soundPool.stop(playingSound.streamId)
        }

        // save current state of playing sounds so by pressing play again the last selected sounds will
        // be played
        playingSoundsListCached = allSounds.filter { it.playing }.toMutableList() as ArrayList<Sound>

        val newList = mutableListOf<Sound>()

        allSounds.mapTo(newList, {
            it.copy(playing = false)
        })


        allSounds = newList
        _allSoundsLive.value = newList

//        playingSoundsList.clear().also { _playingSoundsListLive.value = allSounds }
        countDownTimer?.cancel().also { _timerRunning.value = false }

        EventBus.getDefault().post(EventBusStopAll())
        // subscribe with a method here that receives the stopall event and does not allow the play to be run if observable not in certain state
    }

    override fun setVolume(id: String, streamId: Int, leftVolume: Float, rightVolume: Float) {
        Log.d(TAG, "setVolume: called")
        soundPool.setVolume(streamId, leftVolume, rightVolume)
        val toBeReplaced = allSounds.first { playingSound -> playingSound.id == id }

        for ((index, value) in allSounds.withIndex()) {
            if (value.id == toBeReplaced.id) {
                allSounds[index] = toBeReplaced
                break
            }
        }

//        val indexOf = allSounds.indexOf(toBeReplaced)
        val replaceWith = toBeReplaced.copy(volume = leftVolume)

        for ((index, value) in allSounds.withIndex()) {
            if (value.id == replaceWith.id) {
                allSounds[index] = replaceWith
                break
            }
        }

//        allSounds[allSounds.indexOf(replaceWith)] = replaceWith
//        playingSoundsList = playingSoundsList.toMutableList().apply {
//            this[indexOf] = replaceWith
//        } as ArrayList<Sound>
    }
}
