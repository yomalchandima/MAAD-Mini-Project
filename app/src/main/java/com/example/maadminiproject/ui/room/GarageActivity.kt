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
import androidx.lifecycle.ViewModelProvider
import com.example.maadminiproject.viewmodel.device.DeviceViewModel

class GarageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGarageBinding
    private lateinit var deviceViewModel: DeviceViewModel
    private var isProgrammaticUpdate = false

    private val homeId = "home001"
    private val floorId = "floor1"
    private val zoneId = "garage"

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

        deviceViewModel = ViewModelProvider(this)[DeviceViewModel::class.java]

        setupSwitchListeners()
        setupBottomNav()
        observeDevices()

        deviceViewModel.observeDevices(homeId, floorId, zoneId)
    }

    private fun observeDevices() {
        deviceViewModel.devices.observe(this) { deviceList ->
            // 1. light04 - Garage Light
            val light = deviceList.find { it.deviceId == "light04" }
            if (light != null) {
                isProgrammaticUpdate = true
                binding.swLight.isChecked = light.state
                if (light.state) {
                    binding.tvLightStatus.text = getString(R.string.status_on)
                    binding.tvLightStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.tvLightStatus.compoundDrawableTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.ivLightIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.deep_midnight))
                } else {
                    binding.tvLightStatus.text = getString(R.string.status_off)
                    binding.tvLightStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.tvLightStatus.compoundDrawableTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                    binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                    binding.ivLightIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                }
                isProgrammaticUpdate = false
            }

            // 2. camera02 - Garage Camera
            val camera = deviceList.find { it.deviceId == "camera02" }
            if (camera != null) {
                isProgrammaticUpdate = true
                val isActive = camera.state || (camera.recording == true)
                binding.swCam.isChecked = isActive
                if (isActive) {
                    binding.tvCamStatus.text = getString(R.string.live_recording)
                    binding.tvCamStatus.setTextColor(android.graphics.Color.parseColor("#FF8A80"))
                    binding.tvCamStatus.compoundDrawableTintList = ColorStateList.valueOf(android.graphics.Color.parseColor("#FF8A80"))
                    binding.camFrame.alpha = 1.0f
                } else {
                    binding.tvCamStatus.text = getString(R.string.status_disconnected)
                    binding.tvCamStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.tvCamStatus.compoundDrawableTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                    binding.camFrame.alpha = 0.3f
                }
                isProgrammaticUpdate = false
            }
        }
    }

    private fun setupSwitchListeners() {
        binding.swLight.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "light04", isChecked)
        }

        binding.swCam.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "camera02", isChecked)
            deviceViewModel.setRecording(homeId, floorId, zoneId, "camera02", isChecked)
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