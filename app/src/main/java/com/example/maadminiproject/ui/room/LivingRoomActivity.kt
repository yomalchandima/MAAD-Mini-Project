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

    private var homeId = "home001"
    private var floorId = "floor1"
    private var zoneId = "livingRoom"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLivingRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        floorId = intent.getStringExtra("floorId") ?: "floor1"
        zoneId = intent.getStringExtra("zoneId") ?: "livingRoom"

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
        setupControls()
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
                    binding.sliderBrightness.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvLightStatus.text = getString(R.string.status_off)
                    binding.tvLightStatus.setTextColor(ContextCompat.getColor(this, R.color.soft_gray))
                    binding.ivLightIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft_gray))
                    binding.sliderBrightness.visibility = android.view.View.GONE
                }
                light.brightness?.let {
                    binding.sliderBrightness.value = it.toFloat()
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
                    binding.fanSpeedToggleGroup.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvFanStatus.text = getString(R.string.status_off)
                    binding.tvFanStatus.setTextColor(ContextCompat.getColor(this, R.color.soft_gray))
                    binding.ivFanIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft_gray))
                    binding.fanSpeedToggleGroup.visibility = android.view.View.GONE
                }
                when (fan.speed) {
                    1 -> binding.fanSpeedToggleGroup.check(R.id.btnLow)
                    2 -> binding.fanSpeedToggleGroup.check(R.id.btnMed)
                    3 -> binding.fanSpeedToggleGroup.check(R.id.btnHigh)
                    else -> binding.fanSpeedToggleGroup.check(android.view.View.NO_ID)
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

    private fun setupControls() {
        binding.swMainLighting.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "light01", isChecked)
        }

        binding.sliderBrightness.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                deviceViewModel.setBrightness(homeId, floorId, zoneId, "light01", value.toInt())
            }
        }

        binding.swCeilingFan.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "fan01", isChecked)
        }

        binding.fanSpeedToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isProgrammaticUpdate || !isChecked) return@addOnButtonCheckedListener
            val speed = when (checkedId) {
                R.id.btnLow -> 1
                R.id.btnMed -> 2
                R.id.btnHigh -> 3
                else -> return@addOnButtonCheckedListener
            }
            deviceViewModel.setFanSpeed(homeId, floorId, zoneId, "fan01", speed)
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