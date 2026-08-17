package com.example.maadminiproject.ui.floor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FirstFloorDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(
            Intent(this, FloorDetailActivity::class.java)
                .putExtra("floorId", "floor2")
        )
        finish()
    }
}