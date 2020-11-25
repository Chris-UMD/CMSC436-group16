package com.example.apollo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Stats : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats)

        // hide title bar
        supportActionBar!!.hide()
    }

    // custom font: artographie_light (does not contain numbers, use Arial for numbers)

    // Stats to display:
    // - avg GO response speed
    // - GO accuracy %
    // - No-Go accuracy %

}