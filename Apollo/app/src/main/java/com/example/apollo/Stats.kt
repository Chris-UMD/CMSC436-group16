package com.example.apollo

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Stats : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats)

        // hide title bar
        supportActionBar!!.hide()

        val avgAccuracyNumTextView = findViewById<TextView>(R.id.avg_accuracy_num)
        val avgSpeedNumTextView = findViewById<TextView>(R.id.avg_speed_num)
        val avgNoGoAccuracyNumTextView = findViewById<TextView>(R.id.no_go_accuracy_num)

        val preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        if (preferences.contains("totalGoAccuracy")) {
            avgAccuracyNumTextView.text = preferences.getFloat("totalGoAccuracy", 0f).toString()
            avgSpeedNumTextView.text = preferences.getFloat("totalGoAvgSpeed", 0f).toString()
            avgNoGoAccuracyNumTextView.text = preferences.getFloat("totalNogoAccuracy", 0f).toString()
        } else {
            avgAccuracyNumTextView.text = "0"
            avgSpeedNumTextView.text = "0"
            avgNoGoAccuracyNumTextView.text = "0"
        }
    }

    // custom font: artographie_light (does not contain numbers, use Arial for numbers)

    // Stats to display:
    // - avg GO response speed
    // - GO accuracy %
    // - No-Go accuracy %
}