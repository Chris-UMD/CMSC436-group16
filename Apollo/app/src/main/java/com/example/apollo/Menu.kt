package com.example.apollo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat

class Menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)
    }

    // launch Stats Activity
    fun onClickStats(v: View) {
        startActivity(Intent(this, Play::class.java))
    }

    // launch Play Activity
    fun onClickPlay(v: View) {
        startActivity(Intent(this, Stats::class.java))
    }
}