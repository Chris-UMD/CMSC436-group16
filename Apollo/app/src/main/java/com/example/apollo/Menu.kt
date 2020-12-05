package com.example.apollo

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat

class Menu : AppCompatActivity() {

    // Logging TAG
    private val TAG : String = "Main-Menu"

    // audio systems
    private lateinit var soundPool : SoundPool // for button sound effects
    private lateinit var mediaPlayer : MediaPlayer // for background music

    // sound flag
    private var launchingActivity : Boolean = false

    // sound file
    private var launchSound : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)

        // hide title bar
        supportActionBar!!.hide()

        // setup audio systems
        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

        // initialize soundPool
        soundPool = SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build()

        // load sound effects
        launchSound = soundPool.load(this, R.raw.launch, 1)

        // initialize mediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.apollo_bg)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    // launch Stats Activity
    fun onClickStats(v: View) {
        //Toast.makeText(this, "Stats button clicked!", Toast.LENGTH_SHORT).show()
        launchingActivity = true
        startActivity(Intent(applicationContext, Stats::class.java))
    }

    // launch Play Activity
    fun onClickPlay(v: View) {
        soundPool.play(launchSound,0.5f,0.5f, 1, 0, 1.0f)

        // Toast.makeText(this, "Play button clicked!", Toast.LENGTH_SHORT).show()
        launchingActivity = true
        startActivity(Intent(applicationContext, Play::class.java))
    }

    override fun onStart() {
        super.onStart()
        launchingActivity = false
    }

    override fun onPause() {
        super.onPause()

        // Log.i(TAG, "(Pause) Playing: ${mediaPlayer.isPlaying}")

        // if not launching an activity, pause
        if (!launchingActivity)
            mediaPlayer.pause()
    }

    override fun onResume() {
        super.onResume()

        // Log.i(TAG, "(Resume) Playing: ${mediaPlayer.isPlaying}")

        // resume playback if paused
        if (!mediaPlayer.isPlaying)
            mediaPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
        mediaPlayer.release()
    }
}