package com.example.maadminiproject.ui.room

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityKitchenBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

import android.content.res.ColorStateList
import androidx.lifecycle.ViewModelProvider
import com.example.maadminiproject.viewmodel.device.DeviceViewModel

class KitchenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKitchenBinding
    private lateinit var deviceViewModel: DeviceViewModel
    private var isProgrammaticUpdate = false

    private var homeId = "home001"
    private var floorId = "floor1"
    private var zoneId = "kitchen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityKitchenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        floorId = intent.getStringExtra("floorId") ?: "floor1"
        zoneId = intent.getStringExtra("zoneId") ?: "kitchen"

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
            // 1. light02 - Kitchen Light
            val light = deviceList.find { it.deviceId == "light02" }
            if (light != null) {
                isProgrammaticUpdate = true
                binding.swMainLighting.isChecked = light.state
                if (light.state) {
                    binding.tvLightStatus.text = getString(R.string.online_caps)
                    binding.tvLightStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.tvLightStatus.compoundDrawableTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                } else {
                    binding.tvLightStatus.text = getString(R.string.status_off)
                    binding.tvLightStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.tvLightStatus.compoundDrawableTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                    binding.ivLightIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                }
                isProgrammaticUpdate = false
            }

            // 2. plug01 - Kitchen Smart Plug (Espresso Machine)
            val plug = deviceList.find { it.deviceId == "plug01" }
            if (plug != null) {
                isProgrammaticUpdate = true
                binding.swEspresso.isChecked = plug.state
                if (plug.state) {
                    binding.tvEspressoStatus.text = getString(R.string.status_on)
                    binding.tvEspressoStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.tvEspressoStatus.compoundDrawableTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.ivPlugIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.ivPlugIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.deep_midnight))
                } else {
                    binding.tvEspressoStatus.text = getString(R.string.status_off)
                    binding.tvEspressoStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.tvEspressoStatus.compoundDrawableTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                    binding.ivPlugIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                    binding.ivPlugIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                }
                isProgrammaticUpdate = false
            }

            // 3. switchUnit01 - Kitchen Multi-Switch
            val multiSwitch = deviceList.find { it.deviceId == "switchUnit01" }
            if (multiSwitch != null) {
                binding.cardMultiSwitch.visibility = android.view.View.VISIBLE
                isProgrammaticUpdate = true
                binding.swMulti1.isChecked = multiSwitch.switches?.get("switch_1") ?: false
                binding.swMulti2.isChecked = multiSwitch.switches?.get("switch_2") ?: false
                binding.swMulti3.isChecked = multiSwitch.switches?.get("switch_3") ?: false
                isProgrammaticUpdate = false
            } else {
                binding.cardMultiSwitch.visibility = android.view.View.GONE
            }
        }
    }

    private fun setupSwitchListeners() {
        binding.swMainLighting.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "light02", isChecked)
        }

        binding.swEspresso.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "plug01", isChecked)
        }

        binding.swMulti1.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.setSwitchState(homeId, floorId, zoneId, "switchUnit01", "switch_1", isChecked)
        }

        binding.swMulti2.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.setSwitchState(homeId, floorId, zoneId, "switchUnit01", "switch_2", isChecked)
        }

        binding.swMulti3.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.setSwitchState(homeId, floorId, zoneId, "switchUnit01", "switch_3", isChecked)
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = 0 // Not a main nav item, or choose a related one
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
                    startActivity(Intent(this, com.example.maadminiproject.ui.report.ReportsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}