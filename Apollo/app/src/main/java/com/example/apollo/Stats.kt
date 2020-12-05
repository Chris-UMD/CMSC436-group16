package com.example.apollo

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class Stats : AppCompatActivity() {
    // Logging TAG
    private val TAG : String = "Stats"

    // audio systems
    private lateinit var soundPool : SoundPool // for button sound effects

    // sound file
    private var clearSound : Int = 0

    // event ImageView references
    private var events: MutableList<ImageView> = mutableListOf<ImageView>()

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

        // fill array
        getEvents()

        // display stats
        displayStats()
    }

    private fun displayStats() {
        Log.i(TAG, "displaying stats")

        val avgAccuracyNumTextView = findViewById<TextView>(R.id.avg_accuracy_num)
        val avgSpeedNumTextView = findViewById<TextView>(R.id.avg_speed_num)
        val avgNoGoAccuracyNumTextView = findViewById<TextView>(R.id.no_go_accuracy_num)

        val preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        if (preferences.contains("totalGoAccuracy")) {
            avgAccuracyNumTextView.text = preferences.getFloat("totalGoAccuracy", 0f).toString() + "%"
            avgSpeedNumTextView.text = preferences.getFloat("totalGoAvgSpeed", 0f).toString() + " sec"
            avgNoGoAccuracyNumTextView.text = preferences.getFloat("totalNogoAccuracy", 0f).toString() + "%"

            // programmatically add individual session data to screen
            displayEvents()

        } else {
            avgAccuracyNumTextView.text = "N/A"
            avgSpeedNumTextView.text = "N/A"
            avgNoGoAccuracyNumTextView.text = "N/A"

            // display "no stats notification"
            clearEvents()
        }
    }

    private fun getEvents() {
        Log.i(TAG, "filling events list")

        events.add(findViewById<ImageView>(R.id.event1))
        events.add(findViewById<ImageView>(R.id.event2))
        events.add(findViewById<ImageView>(R.id.event3))
        events.add(findViewById<ImageView>(R.id.event4))
        events.add(findViewById<ImageView>(R.id.event5))
        events.add(findViewById<ImageView>(R.id.event6))
        events.add(findViewById<ImageView>(R.id.event7))
        events.add(findViewById<ImageView>(R.id.event8))
        events.add(findViewById<ImageView>(R.id.event9))
        events.add(findViewById<ImageView>(R.id.event10))
    }

    private fun displayEvents() {
        Log.i(TAG, "displaying events")

        val goSuccess = getDrawable(R.drawable.go_success)
        val goFailure = getDrawable(R.drawable.go_failure)
        val nogoSuccess = getDrawable(R.drawable.nogo_success)
        val nogoFailure = getDrawable(R.drawable.nogo_failure)

        val iterator = events.iterator()

        // display all events
        while (iterator.hasNext()) {
            var view = iterator.next()
            view.visibility = View.VISIBLE

            // switch case on Go ✔/❌ or No-Go ✔/❌ to set background image
            view.background = goSuccess
//            view.background = goFailure
//            view.background = nogoSuccess
//            view.background = nogoFailure
        }

        // hide no events notification
        val noEvents = findViewById<TextView>(R.id.no_stats)
        noEvents.visibility = View.GONE
    }

    private fun clearEvents() {
        Log.i(TAG, "clearing events")

        val iterator = events.iterator()

        // hide all events
        while (iterator.hasNext()) {
            var view = iterator.next()
            view.visibility = View.GONE
        }

        // reveal no events notification
        val noEvents = findViewById<TextView>(R.id.no_stats)
        noEvents.visibility = View.VISIBLE
    }

    fun onClickClear(v: View) {
        val preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        // if there are stats to clear
        if (preferences.contains("totalGoAccuracy")) {
            // play sound effect
            soundPool.play(clearSound,0.5f,0.5f, 1, 0, 1.0f)

            val prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val prefsEditor = prefs.edit()
            prefsEditor.clear()
            prefsEditor.apply()

            // reset display
            displayStats()

            Toast.makeText(applicationContext, "Data cleared!", Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(applicationContext, "There is no data to clear!", Toast.LENGTH_SHORT).show()
        }
    }

    // return to main menu
    fun onClickBack(v: View) {
        onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}