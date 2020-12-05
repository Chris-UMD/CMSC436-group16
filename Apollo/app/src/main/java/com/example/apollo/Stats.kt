package com.example.apollo

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Stats : AppCompatActivity() {
    // audio systems
    private lateinit var soundPool : SoundPool // for button sound effects

    // sound file
    private var clearSound : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats)

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
        clearSound = soundPool.load(this, R.raw.delete, 1)

        // display stats
        displayStats()
    }

    fun displayStats() {
        val avgAccuracyNumTextView = findViewById<TextView>(R.id.avg_accuracy_num)
        val avgSpeedNumTextView = findViewById<TextView>(R.id.avg_speed_num)
        val avgNoGoAccuracyNumTextView = findViewById<TextView>(R.id.no_go_accuracy_num)

        val preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        if (preferences.contains("totalGoAccuracy")) {
            avgAccuracyNumTextView.text = preferences.getFloat("totalGoAccuracy", 0f).toString()
            avgSpeedNumTextView.text = preferences.getFloat("totalGoAvgSpeed", 0f).toString() + " MS"
            avgNoGoAccuracyNumTextView.text = preferences.getFloat("totalNogoAccuracy", 0f).toString()
        } else {
            avgAccuracyNumTextView.text = "0"
            avgSpeedNumTextView.text = "0"
            avgNoGoAccuracyNumTextView.text = "0"
        }
    }

    fun onClickClear(v: View) {
        // play sound effect
        soundPool.play(clearSound,0.5f,0.5f, 1, 0, 1.0f)

        val prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val prefsEditor = prefs.edit()
        prefsEditor.clear()
        prefsEditor.apply()

        // reset display
        displayStats()

        Toast.makeText(applicationContext, "Data cleared!", Toast.LENGTH_SHORT).show()
    }

    // return to main menu
    fun onClickBack(v: View) {
        startActivity(Intent(applicationContext, Menu::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}