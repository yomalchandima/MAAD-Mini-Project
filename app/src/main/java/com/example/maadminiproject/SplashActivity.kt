package com.example.maadminiproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Match the rest of the app's edge-to-edge style
        enableEdgeToEdge()

        setContentView(R.layout.activity_splash)

        // Delay for 3 seconds and then start LoginActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, com.example.maadminiproject.ui.authentication.LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}