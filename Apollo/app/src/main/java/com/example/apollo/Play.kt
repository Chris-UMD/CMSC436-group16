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

    // colors for Go and No-Go backgrounds stored in res/color/colors.xml

    // Stats to collect:
    // - avg GO response speed
    // - GO accuracy %
    // - No-Go accuracy %

}