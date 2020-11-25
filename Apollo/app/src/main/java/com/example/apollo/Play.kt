package com.example.apollo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Play : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.play)

        // hide title bar
        supportActionBar!!.hide()
    }

    // custom font: artographie_light (does not contain numbers, use Arial for numbers)

    // Hexcodes for Go and No-Go backgrounds:
    // WAIT: #17075A
    // DON'T TAP: #F734A9
    // TAP: #4BD8F9
    // SUCCESS: #8F5EEE
    // FAILURE: #FF7E64

    // Stats to collect:
    // - avg GO response speed
    // - GO accuracy %
    // - No-Go accuracy %

}