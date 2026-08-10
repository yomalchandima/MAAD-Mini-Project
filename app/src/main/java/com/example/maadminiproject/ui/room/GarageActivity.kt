package com.example.maadminiproject.ui.room

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityGarageBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

class GarageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGarageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGarageBinding.inflate(layoutInflater)
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
        binding.swLight.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvBrightnessLabel.setTextColor(getColor(R.color.soft_gray))
                binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                binding.ivLightIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.deep_midnight))
            } else {
                binding.tvBrightnessLabel.setTextColor(getColor(R.color.soft_gray))
                binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                binding.ivLightIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
            }
        }

        binding.sliderBrightness.addOnChangeListener { _, value, _ ->
            binding.tvBrightnessLabel.text = "${value.toInt()}% Brightness"
            if (value > 0 && !binding.swLight.isChecked) {
                binding.swLight.isChecked = true
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