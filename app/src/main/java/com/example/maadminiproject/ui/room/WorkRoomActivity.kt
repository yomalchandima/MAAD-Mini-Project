package com.example.maadminiproject.ui.room

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityWorkRoomBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

import android.content.res.ColorStateList
import androidx.lifecycle.ViewModelProvider
import com.example.maadminiproject.viewmodel.device.DeviceViewModel

class WorkRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWorkRoomBinding
    private lateinit var deviceViewModel: DeviceViewModel
    private var isProgrammaticUpdate = false
    private var currentTemp = 21

    private var homeId = "home001"
    private var floorId = "floor2"
    private var zoneId = "workRoom"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWorkRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        floorId = intent.getStringExtra("floorId") ?: "floor2"
        zoneId = intent.getStringExtra("zoneId") ?: "workRoom"

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
            // 1. light09 - Office Light
            val light = deviceList.find { it.deviceId == "light09" }
            if (light != null) {
                isProgrammaticUpdate = true
                binding.swMainLight.isChecked = light.state
                if (light.state) {
                    binding.tvLightStatus.text = getString(R.string.status_on)
                    binding.tvLightStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.sliderBrightness.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvLightStatus.text = getString(R.string.status_off)
                    binding.tvLightStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                    binding.sliderBrightness.visibility = android.view.View.GONE
                }
                light.brightness?.let {
                    binding.sliderBrightness.value = it.toFloat()
                }
                isProgrammaticUpdate = false
            }

            // 2. plug04 - Office Smart Plug (Desk Setup)
            val plug = deviceList.find { it.deviceId == "plug04" }
            if (plug != null) {
                isProgrammaticUpdate = true
                binding.swDesk.isChecked = plug.state
                if (plug.state) {
                    binding.tvDeskStatus.text = getString(R.string.status_on)
                    binding.tvDeskStatus.setTextColor(getColor(R.color.vibrant_cyan))
                } else {
                    binding.tvDeskStatus.text = getString(R.string.status_off)
                    binding.tvDeskStatus.setTextColor(getColor(R.color.soft_gray))
                }
                isProgrammaticUpdate = false
            }

            // 3. ac03 - Office AC
            val ac = deviceList.find { it.deviceId == "ac03" }
            if (ac != null) {
                isProgrammaticUpdate = true
                binding.swAc.isChecked = ac.state
                if (ac.state) {
                    binding.tvAcStatus.text = getString(R.string.status_on)
                    binding.tvAcStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.ivAcIcon.backgroundTintList = ColorStateList.valueOf(0x2000E5FF.toInt())
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
            }
        }
    }

    private fun setupControls() {
        binding.swMainLight.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "light09", isChecked)
        }

        binding.sliderBrightness.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                deviceViewModel.setBrightness(homeId, floorId, zoneId, "light09", value.toInt())
            }
        }

        binding.swDesk.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "plug04", isChecked)
        }

        binding.swAc.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "ac03", isChecked)
        }

        binding.acModeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isProgrammaticUpdate || !isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                R.id.btnCool -> "COOL"
                R.id.btnHeat -> "HEAT"
                R.id.btnAuto -> "AUTO"
                else -> return@addOnButtonCheckedListener
            }
            deviceViewModel.setMode(homeId, floorId, zoneId, "ac03", mode)
        }

        binding.btnTempMinus.setOnClickListener {
            val ac = deviceViewModel.devices.value?.find { it.deviceId == "ac03" }
            val temp = ac?.temperature ?: currentTemp
            if (temp > 16) {
                deviceViewModel.setTemperature(homeId, floorId, zoneId, "ac03", temp - 1)
            }
        }

        binding.btnTempPlus.setOnClickListener {
            val ac = deviceViewModel.devices.value?.find { it.deviceId == "ac03" }
            val temp = ac?.temperature ?: currentTemp
            if (temp < 30) {
                deviceViewModel.setTemperature(homeId, floorId, zoneId, "ac03", temp + 1)
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