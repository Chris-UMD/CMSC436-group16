package com.example.apollo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Stats : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu) // UPDATE CONTENT VIEW
    }

    // Stats to display:
    // - avg GO response speed
    // - GO accuracy %
    // - No-Go accuracy %

}