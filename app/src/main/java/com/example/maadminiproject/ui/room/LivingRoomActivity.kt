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
import androidx.lifecycle.ViewModelProvider
import com.example.maadminiproject.viewmodel.device.DeviceViewModel

class LivingRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLivingRoomBinding
    private lateinit var deviceViewModel: DeviceViewModel
    private var isProgrammaticUpdate = false

    private val homeId = "home001"
    private val floorId = "floor1"
    private val zoneId = "livingRoom"

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

        deviceViewModel = ViewModelProvider(this)[DeviceViewModel::class.java]

        setupBottomNav()
        setupLightingControls()
        observeDevices()

        deviceViewModel.observeDevices(homeId, floorId, zoneId)
    }

    private fun observeDevices() {
        deviceViewModel.devices.observe(this) { deviceList ->
            // 1. light01 - Living Light
            val light = deviceList.find { it.deviceId == "light01" }
            if (light != null) {
                isProgrammaticUpdate = true
                binding.swMainLighting.isChecked = light.state
                if (light.state) {
                    binding.tvLightStatus.text = getString(R.string.status_on)
                    binding.tvLightStatus.setTextColor(ContextCompat.getColor(this, R.color.vibrant_cyan))
                    binding.ivLightIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.vibrant_cyan))
                } else {
                    binding.tvLightStatus.text = getString(R.string.status_off)
                    binding.tvLightStatus.setTextColor(ContextCompat.getColor(this, R.color.soft_gray))
                    binding.ivLightIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft_gray))
                }
                isProgrammaticUpdate = false
            }

            // 2. fan01 - Ceiling Fan
            val fan = deviceList.find { it.deviceId == "fan01" }
            if (fan != null) {
                isProgrammaticUpdate = true
                binding.swCeilingFan.isChecked = fan.state
                if (fan.state) {
                    binding.tvFanStatus.text = getString(R.string.status_on)
                    binding.tvFanStatus.setTextColor(ContextCompat.getColor(this, R.color.vibrant_cyan))
                    binding.ivFanIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.vibrant_cyan))
                } else {
                    binding.tvFanStatus.text = getString(R.string.status_off)
                    binding.tvFanStatus.setTextColor(ContextCompat.getColor(this, R.color.soft_gray))
                    binding.ivFanIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft_gray))
                }
                isProgrammaticUpdate = false
            }

            // 3. camera01 - Entrance Camera
            val camera = deviceList.find { it.deviceId == "camera01" }
            if (camera != null) {
                isProgrammaticUpdate = true
                val isActive = camera.state || (camera.recording == true)
                binding.swCam.isChecked = isActive
                if (isActive) {
                    binding.tvCamStatus.text = getString(R.string.live)
                    binding.tvCamStatus.setTextColor(ContextCompat.getColor(this, R.color.vibrant_cyan))
                    binding.tvCamStatus.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.vibrant_cyan))
                    binding.ivCamIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.vibrant_cyan))
                    binding.camFrame.alpha = 1.0f
                } else {
                    binding.tvCamStatus.text = getString(R.string.status_off)
                    binding.tvCamStatus.setTextColor(ContextCompat.getColor(this, R.color.soft_gray))
                    binding.tvCamStatus.compoundDrawableTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft_gray))
                    binding.ivCamIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft_gray))
                    binding.camFrame.alpha = 0.5f
                }
                isProgrammaticUpdate = false
            }
        }
    }

    private fun setupLightingControls() {
        binding.swMainLighting.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "light01", isChecked)
        }

        binding.swCeilingFan.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "fan01", isChecked)
        }

        binding.swCam.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "camera01", isChecked)
            deviceViewModel.setRecording(homeId, floorId, zoneId, "camera01", isChecked)
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