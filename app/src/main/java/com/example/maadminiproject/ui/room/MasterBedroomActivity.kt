package com.example.maadminiproject.ui.room

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityMasterBedroomBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

import android.content.res.ColorStateList
import androidx.lifecycle.ViewModelProvider
import com.example.maadminiproject.viewmodel.device.DeviceViewModel

class MasterBedroomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMasterBedroomBinding
    private lateinit var deviceViewModel: DeviceViewModel
    private var isProgrammaticUpdate = false

    private var currentTemp = 21
    private var ironMaxDuration = 10

    private var homeId = "home001"
    private var floorId = "floor2"
    private var zoneId = "masterBedroom"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMasterBedroomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        floorId = intent.getStringExtra("floorId") ?: "floor2"
        zoneId = intent.getStringExtra("zoneId") ?: "masterBedroom"

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
            // 1. light07 - Master Bedroom Light
            val light = deviceList.find { it.deviceId == "light07" }
            if (light != null) {
                isProgrammaticUpdate = true
                binding.swMainLights.isChecked = light.state
                if (light.state) {
                    binding.tvLightsStatus.text = getString(R.string.status_on)
                    binding.tvLightsStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.ivLightsIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.sliderBrightness.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvLightsStatus.text = getString(R.string.status_off)
                    binding.tvLightsStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.ivLightsIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                    binding.sliderBrightness.visibility = android.view.View.GONE
                }
                light.brightness?.let {
                    binding.sliderBrightness.value = it.toFloat()
                }
                isProgrammaticUpdate = false
            }

            // 2. ac01 - Master Bedroom AC
            val ac = deviceList.find { it.deviceId == "ac01" }
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

            // 3. plug03 - Bedroom Smart Plug
            val plug = deviceList.find { it.deviceId == "plug03" }
            if (plug != null) {
                isProgrammaticUpdate = true
                binding.swSmartPlug.isChecked = plug.state
                if (plug.state) {
                    binding.tvPlugStatus.text = getString(R.string.status_on)
                    binding.tvPlugStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.ivPlugIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                } else {
                    binding.tvPlugStatus.text = getString(R.string.status_off)
                    binding.tvPlugStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.ivPlugIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                }
                isProgrammaticUpdate = false
            }

            // 4. iron01 - Smart Clothing Iron (Master Bedroom)
            val iron = deviceList.find { it.deviceId == "iron01" }
            if (iron != null) {
                isProgrammaticUpdate = true
                binding.swIron.isChecked = iron.state
                if (iron.state) {
                    binding.tvIronStatus.text = getString(R.string.status_on)
                    binding.tvIronStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.ivIronIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.ivIronIcon.backgroundTintList = ColorStateList.valueOf(0x2000E5FF.toInt())
                } else {
                    binding.tvIronStatus.text = getString(R.string.status_off)
                    binding.tvIronStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.ivIronIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                    binding.ivIronIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                }
                iron.maxActiveDuration?.let { dur ->
                    ironMaxDuration = dur.toInt()
                    binding.tvIronDurationValue.text = getString(R.string.duration_format, ironMaxDuration)
                    binding.sliderIronDuration.value = ironMaxDuration.toFloat().coerceIn(1f, 30f)
                }
                isProgrammaticUpdate = false
            }
        }
    }

    private fun setupControls() {
        binding.swAc.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "ac01", isChecked)
        }

        binding.acModeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isProgrammaticUpdate || !isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                R.id.btnCool -> "COOL"
                R.id.btnHeat -> "HEAT"
                R.id.btnAuto -> "AUTO"
                else -> return@addOnButtonCheckedListener
            }
            deviceViewModel.setMode(homeId, floorId, zoneId, "ac01", mode)
        }

        binding.btnTempMinus.setOnClickListener {
            val ac = deviceViewModel.devices.value?.find { it.deviceId == "ac01" }
            val temp = ac?.temperature ?: currentTemp
            if (temp > 16) {
                deviceViewModel.setTemperature(homeId, floorId, zoneId, "ac01", temp - 1)
            }
        }

        binding.btnTempPlus.setOnClickListener {
            val ac = deviceViewModel.devices.value?.find { it.deviceId == "ac01" }
            val temp = ac?.temperature ?: currentTemp
            if (temp < 30) {
                deviceViewModel.setTemperature(homeId, floorId, zoneId, "ac01", temp + 1)
            }
        }

        binding.swMainLights.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "light07", isChecked)
        }

        binding.sliderBrightness.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                deviceViewModel.setBrightness(homeId, floorId, zoneId, "light07", value.toInt())
            }
        }

        binding.swSmartPlug.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "plug03", isChecked)
        }

        binding.swIron.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "iron01", isChecked)
        }

        binding.sliderIronDuration.addOnChangeListener { _, value, fromUser ->
            ironMaxDuration = value.toInt()
            binding.tvIronDurationValue.text = getString(R.string.duration_format, ironMaxDuration)
            if (fromUser) {
                deviceViewModel.setMaxActiveDuration(homeId, floorId, zoneId, "iron01", ironMaxDuration.toLong())
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