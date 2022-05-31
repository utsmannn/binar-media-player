package com.utsman.binarmediaplayer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.utsman.binarmediaplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val maxStream = 1
    private var isLoaded = false
    private var streamId = 0
    private var currentSoundId = 0

    private var isPlay = false
    private var isPaused = false

    private val soundPool: SoundPool by lazy {
        buildSoundPool()
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentSoundId = loadAudioAsSoundId(R.raw.audio_destroy)

        updateViewButton()
        binding.btnPlaySound.setOnClickListener {
            if (isPlay) {
                if (streamId != 0) {
                    stopAudio(streamId)
                } else {
                    Toast.makeText(this, "stream Id invalid!", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (currentSoundId != 0) {
                    playAudio(currentSoundId)
                } else {
                    Toast.makeText(this, "sound id invalid!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnPauseResumeSound.setOnClickListener {
            if (isPaused) {
                if (streamId != 0) {
                    resumeAudio(streamId)
                } else {
                    Toast.makeText(this, "stream Id invalid!", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (streamId != 0) {
                    pauseAudio(streamId)
                } else {
                    Toast.makeText(this, "stream Id invalid!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playAudio(soundId: Int) {
        val volumeManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val actualVolume = volumeManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        val maxStreamVolume = volumeManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()

        val soundVolume = actualVolume / maxStreamVolume
        if (isLoaded) {
            streamId = soundPool.play(soundId, soundVolume, soundVolume, 1, -1, 1f)
            isPlay = true
        } else {
            Toast.makeText(this, "Sound not loaded!", Toast.LENGTH_SHORT).show()
        }
        updateViewButton()
    }

    private fun stopAudio(streamId: Int) {
        if (isLoaded) {
            soundPool.stop(streamId)
            isPlay = false
        }
        updateViewButton()
    }

    private fun pauseAudio(streamId: Int) {
        if (isLoaded) {
            soundPool.pause(streamId)
            isPaused = true
        }
        updateViewButton()
    }

    private fun resumeAudio(streamId: Int) {
        if (isLoaded) {
            soundPool.resume(streamId)
            isPaused = false
        }
        updateViewButton()
    }

    private fun loadAudioAsSoundId(audioRes: Int): Int {
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            isLoaded = true
        }

        return soundPool.load(this, audioRes, 1)
    }

    private fun updateViewButton() {
        val btnPlayText = if (isPlay) {
            "Stop"
        } else {
            "Play"
        }

        val btnPauseText = if (isPaused) {
            "Resume"
        } else {
            "Pause"
        }

        binding.btnPlaySound.text = btnPlayText
        binding.btnPauseResumeSound.text = btnPauseText

        binding.btnPauseResumeSound.isVisible = isPlay
    }

    private fun buildSoundPool(): SoundPool {
        val audioAttribute = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val soundPoolBuilder = SoundPool.Builder()
            .setAudioAttributes(audioAttribute)
            .setMaxStreams(maxStream)

        return soundPoolBuilder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (currentSoundId != 0) {
            soundPool.unload(currentSoundId)
        }
        soundPool.release()
    }
}