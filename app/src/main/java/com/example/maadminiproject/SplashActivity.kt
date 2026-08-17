package com.example.maadminiproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import android.util.Log

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("SplashActivity", "onCreate called")

        setContentView(R.layout.activity_splash)

        // Delay for 3 seconds and then start LoginActivity
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("SplashActivity", "Moving to LoginActivity")
            val intent = Intent(this, com.example.maadminiproject.ui.authentication.LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}