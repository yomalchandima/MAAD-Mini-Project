package com.example.maadminiproject.ui.room

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityBedroom2Binding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

import android.content.res.ColorStateList
import androidx.lifecycle.ViewModelProvider
import com.example.maadminiproject.viewmodel.device.DeviceViewModel

class Bedroom2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityBedroom2Binding
    private lateinit var deviceViewModel: DeviceViewModel
    private var isProgrammaticUpdate = false
    private var currentTemp = 21

    private var homeId = "home001"
    private var floorId = "floor2"
    private var zoneId = "bedroom2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBedroom2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        floorId = intent.getStringExtra("floorId") ?: "floor2"
        zoneId = intent.getStringExtra("zoneId") ?: "bedroom2"

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
            // 1. light08 - Bedroom 2 Light
            val light = deviceList.find { it.deviceId == "light08" }
            if (light != null) {
                isProgrammaticUpdate = true
                binding.swLight.isChecked = light.state
                if (light.state) {
                    binding.tvLightStatus.text = getString(R.string.online)
                    binding.tvLightStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.ivLightIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.deep_midnight))
                    binding.sliderBrightness.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvLightStatus.text = getString(R.string.status_off)
                    binding.tvLightStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                    binding.ivLightIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                    binding.sliderBrightness.visibility = android.view.View.GONE
                }
                light.brightness?.let {
                    binding.sliderBrightness.value = it.toFloat()
                }
                isProgrammaticUpdate = false
            }

            // 2. ac02 - Bedroom 2 AC
            val ac = deviceList.find { it.deviceId == "ac02" }
            if (ac != null) {
                binding.cardAc.visibility = android.view.View.VISIBLE
                isProgrammaticUpdate = true
                binding.swAc.isChecked = ac.state
                if (ac.state) {
                    binding.tvAcStatus.text = getString(R.string.status_on)
                    binding.tvAcStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.ivAcIcon.backgroundTintList = ColorStateList.valueOf(0x2000E5FF)
                    binding.tempControl.visibility = android.view.View.VISIBLE
                    binding.acModeToggleGroup.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvAcStatus.text = getString(R.string.status_off)
                    binding.tvAcStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.ivAcIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                    binding.tempControl.visibility = android.view.View.GONE
                    binding.acModeToggleGroup.visibility = android.view.View.GONE
                }
                ac.temperature?.let { temp ->
                    currentTemp = temp
                    binding.tvTemperature.text = getString(R.string.temp_format, temp)
                }
                when (ac.mode?.uppercase()) {
                    "COOL" -> binding.acModeToggleGroup.check(R.id.btnCool)
                    "HEAT" -> binding.acModeToggleGroup.check(R.id.btnHeat)
                    "AUTO" -> binding.acModeToggleGroup.check(R.id.btnAuto)
                    else -> binding.acModeToggleGroup.check(android.view.View.NO_ID)
                }
                isProgrammaticUpdate = false
            } else {
                binding.cardAc.visibility = android.view.View.GONE
            }
        }
    }

    private fun setupControls() {
        binding.swLight.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "light08", isChecked)
        }

        binding.sliderBrightness.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                deviceViewModel.setBrightness(homeId, floorId, zoneId, "light08", value.toInt())
            }
        }

        binding.swAc.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "ac02", isChecked)
        }

        binding.acModeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isProgrammaticUpdate || !isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                R.id.btnCool -> "COOL"
                R.id.btnHeat -> "HEAT"
                R.id.btnAuto -> "AUTO"
                else -> return@addOnButtonCheckedListener
            }
            deviceViewModel.setMode(homeId, floorId, zoneId, "ac02", mode)
        }

        binding.btnTempMinus.setOnClickListener {
            val ac = deviceViewModel.devices.value?.find { it.deviceId == "ac02" }
            val temp = ac?.temperature ?: currentTemp
            if (temp > 16) {
                deviceViewModel.setTemperature(homeId, floorId, zoneId, "ac02", temp - 1)
            }
        }

        binding.btnTempPlus.setOnClickListener {
            val ac = deviceViewModel.devices.value?.find { it.deviceId == "ac02" }
            val temp = ac?.temperature ?: currentTemp
            if (temp < 30) {
                deviceViewModel.setTemperature(homeId, floorId, zoneId, "ac02", temp + 1)
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