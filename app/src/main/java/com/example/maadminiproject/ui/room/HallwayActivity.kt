package com.example.maadminiproject.ui.room

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityHallwayBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity
import androidx.lifecycle.ViewModelProvider
import com.example.maadminiproject.viewmodel.device.DeviceViewModel

class HallwayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHallwayBinding
    private lateinit var deviceViewModel: DeviceViewModel
    private var isProgrammaticUpdate = false

    private var homeId = "home001"
    private var floorId = "floor2"
    private var zoneId = "hallway"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHallwayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        floorId = intent.getStringExtra("floorId") ?: "floor2"
        zoneId = intent.getStringExtra("zoneId") ?: "hallway"

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
            // 1. light11 - Hallway Light
            val light = deviceList.find { it.deviceId == "light11" }
            if (light != null) {
                isProgrammaticUpdate = true
                binding.swLight.isChecked = light.state
                if (light.state) {
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
                isProgrammaticUpdate = false
            }

            // 2. camera03 - Hallway/Indoor Camera
            val camera = deviceList.find { it.deviceId == "camera03" }
            if (camera != null) {
                isProgrammaticUpdate = true
                val isActive = camera.state || (camera.recording == true)
                binding.swCam.isChecked = isActive
                if (isActive) {

                    binding.tvCamStatus.text = getString(R.string.status_disconnected)
                    binding.tvCamStatus.setTextColor(getColor(R.color.error_red))
                    binding.camFrame.alpha = 0.4f
                    
                    // Red switch for disconnected
                    binding.swCam.thumbTintList = ColorStateList.valueOf(getColor(R.color.error_red))
                    binding.swCam.trackTintList = ColorStateList.valueOf(getColor(R.color.error_red)).withAlpha(0x66)
                } else {
                    binding.tvCamStatus.text = getString(R.string.status_off)
                    binding.tvCamStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.camFrame.alpha = 0.5f
                    
                    // Gray switch for OFF
                    binding.swCam.thumbTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                    binding.swCam.trackTintList = ColorStateList.valueOf(getColor(R.color.soft_gray)).withAlpha(0x44)
                }
                isProgrammaticUpdate = false
            }
        }
    }

    private fun setupControls() {
        binding.swLight.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "light11", isChecked)
        }

        binding.swCam.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            if (isChecked) {
                // Demo requirement: Set status to DISCONNECTED and online to false in Firebase when turned ON
                val updates = mapOf(
                    "state" to true,
                    "recording" to true,
                    "status" to "DISCONNECTED",
                    "online" to false
                )
                deviceViewModel.updateDeviceFields(homeId, floorId, zoneId, "camera03", updates)
            } else {
                val updates = mapOf(
                    "state" to false,
                    "recording" to false,
                    "status" to "OFF",
                    "online" to true
                )
                deviceViewModel.updateDeviceFields(homeId, floorId, zoneId, "camera03", updates)
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