package com.example.maadminiproject.ui.room

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityLivingRoomBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class LivingRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLivingRoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLivingRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupBottomNav()
        setupLightingControls()
    }

    private fun setupLightingControls() {
        binding.swMainLighting.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvLightStatus.text = getString(R.string.status_on)
                binding.tvLightStatus.setTextColor(ContextCompat.getColor(this, R.color.vibrant_cyan))
                binding.ivLightIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.vibrant_cyan))
            } else {
                binding.tvLightStatus.text = getString(R.string.status_off)
                binding.tvLightStatus.setTextColor(ContextCompat.getColor(this, R.color.soft_gray))
                binding.ivLightIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft_gray))
            }
        }

        binding.swCeilingFan.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvFanStatus.text = getString(R.string.status_on)
                binding.tvFanStatus.setTextColor(ContextCompat.getColor(this, R.color.vibrant_cyan))
                binding.ivFanIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.vibrant_cyan))
            } else {
                binding.tvFanStatus.text = getString(R.string.status_off)
                binding.tvFanStatus.setTextColor(ContextCompat.getColor(this, R.color.soft_gray))
                binding.ivFanIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft_gray))
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_floors -> {
                    startActivity(Intent(this, FloorActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_reports -> {
                    true
                }
                else -> false
            }
        }
    }
}