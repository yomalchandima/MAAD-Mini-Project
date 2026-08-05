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

        setupBottomNav()
        setupLightingControls()
        setupFanControls()
    }

    private fun setupLightingControls() {
        binding.swMainLighting.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvLightStatus.text = getString(R.string.online)
                binding.tvLightStatus.setTextColor(ContextCompat.getColor(this, R.color.vibrant_cyan))
                binding.ivLightIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.vibrant_cyan))
            } else {
                binding.tvLightStatus.text = "Off"
                binding.tvLightStatus.setTextColor(ContextCompat.getColor(this, R.color.soft_gray))
                binding.ivLightIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft_gray))
            }
        }
    }

    private fun setupFanControls() {
        // Initial state
        updateFanButtonStyles(R.id.btnMed)
        
        binding.swCeilingFan.setOnCheckedChangeListener { _, isChecked ->
            binding.fanSpeedToggleGroup.isEnabled = isChecked
            // Also visual feedback for buttons when group is disabled
            for (i in 0 until binding.fanSpeedToggleGroup.childCount) {
                binding.fanSpeedToggleGroup.getChildAt(i).isEnabled = isChecked
            }
            
            if (isChecked) {
                val checkedId = binding.fanSpeedToggleGroup.checkedButtonId
                updateFanStatusText(checkedId)
                updateFanButtonStyles(checkedId)
            } else {
                binding.tvFanStatus.text = "Off"
            }
        }

        binding.fanSpeedToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && binding.swCeilingFan.isChecked) {
                updateFanButtonStyles(checkedId)
                updateFanStatusText(checkedId)
            }
        }
    }

    private fun updateFanStatusText(checkedId: Int) {
        when (checkedId) {
            R.id.btnLow -> binding.tvFanStatus.text = "Speed 1"
            R.id.btnMed -> binding.tvFanStatus.text = "Speed 2"
            R.id.btnHigh -> binding.tvFanStatus.text = "Speed 3"
        }
    }

    private fun updateFanButtonStyles(checkedId: Int) {
        val buttons = listOf(binding.btnLow, binding.btnMed, binding.btnHigh)
        val activeColor = ContextCompat.getColor(this, R.color.vibrant_cyan)
        val inactiveColor = ContextCompat.getColor(this, R.color.soft_gray)
        val activeBg = ContextCompat.getColor(this, R.color.surface_container)

        buttons.forEach { button ->
            if (button.id == checkedId) {
                button.setTextColor(activeColor)
                button.strokeColor = ColorStateList.valueOf(activeColor)
                button.setBackgroundColor(activeBg)
            } else {
                button.setTextColor(inactiveColor)
                button.strokeColor = ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
                button.setBackgroundColor(android.graphics.Color.TRANSPARENT)
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