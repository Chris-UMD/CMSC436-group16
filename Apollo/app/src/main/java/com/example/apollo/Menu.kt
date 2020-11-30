package com.example.apollo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat

class Menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)

        // hide title bar
        supportActionBar!!.hide()
    }

    // launch Stats Activity
    fun onClickStats(v: View) {
        Toast.makeText(this, "Stats button clicked!", Toast.LENGTH_SHORT).show()
         startActivity(Intent(applicationContext, Stats::class.java))
    }

    // launch Play Activity
    fun onClickPlay(v: View) {
        Toast.makeText(this, "Play button clicked!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(applicationContext, Play::class.java))
    }
}