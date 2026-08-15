package com.example.maadminiproject.ui.room

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityBathroomBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

import android.content.res.ColorStateList
import androidx.lifecycle.ViewModelProvider
import com.example.maadminiproject.viewmodel.device.DeviceViewModel

class BathroomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBathroomBinding
    private lateinit var deviceViewModel: DeviceViewModel
    private var isProgrammaticUpdate = false

    private var homeId = "home001"
    private var floorId = "floor2"
    private var zoneId = "bathroomFF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBathroomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        floorId = intent.getStringExtra("floorId") ?: "floor2"
        zoneId = intent.getStringExtra("zoneId") ?: "bathroomFF"

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        deviceViewModel = ViewModelProvider(this)[DeviceViewModel::class.java]

        setupControls()
        setupBottomNav()
        observeDevices()

        deviceViewModel.observeDevices(homeId, floorId, zoneId)
    }

    private fun observeDevices() {
        deviceViewModel.devices.observe(this) { deviceList ->
            // 1. light10 - First Floor Bathroom Light
            val light = deviceList.find { it.deviceId == "light10" }
            if (light != null) {
                isProgrammaticUpdate = true
                binding.swMainLighting.isChecked = light.state
                updateUI(light.state)
                isProgrammaticUpdate = false
            }
        }
    }

    private fun setupControls() {
        binding.swMainLighting.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "light10", isChecked)
        }
    }

    private fun updateUI(isOn: Boolean) {
        if (isOn) {
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