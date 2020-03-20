package com.cazimir.relaxoo.service

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.SoundPool
import android.os.IBinder
import android.util.Log
import com.cazimir.relaxoo.model.Sound

class SoundPoolService : Service(), ISoundPoolService {

    companion object {
        private const val MAX_SOUNDS = 5
        private const val TAG = "SoundPoolService"
    }

    private lateinit var soundPool: SoundPool

    // called when startService is beeing called
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()
        soundPool = SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0)

        soundPool.setOnLoadCompleteListener { soundPool: SoundPool?, sampleId: Int, status: Int ->
            Log.d(TAG, "onLoadComplete: $sampleId")
            // EventBus.getDefault().post()
            // viewModel.addedSound()
        }
    }

    override fun addRecordingToSoundPool(sound: Sound) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeRecordingFromSoundPool(sound: Sound) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playStopSound(soundPoolId: Int, playing: Boolean, streamId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stopAllSounds() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}