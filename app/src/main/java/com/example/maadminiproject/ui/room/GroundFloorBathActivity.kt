package com.example.maadminiproject.ui.room

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityGroundFloorBathBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

class GroundFloorBathActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroundFloorBathBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGroundFloorBathBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupControls()
        setupBottomNav()
    }

    private fun setupControls() {
        binding.swMainLight.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvLightStatus.text = getString(R.string.status_on)
                binding.tvLightStatus.setTextColor(getColor(R.color.vibrant_cyan))
                binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                binding.ivLightIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.deep_midnight))
            } else {
                binding.tvLightStatus.text = getString(R.string.status_off)
                binding.tvLightStatus.setTextColor(getColor(R.color.soft_gray))
                binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                binding.ivLightIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
            }
        }

        binding.sliderBrightness.addOnChangeListener { _, value, _ ->
            binding.tvBrightnessValue.text = "${value.toInt()}%"
            if (value > 0 && !binding.swMainLight.isChecked) {
                binding.swMainLight.isChecked = true
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = 0
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