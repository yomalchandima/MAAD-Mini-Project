package com.example.maadminiproject.ui.room

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityDiningBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

import android.content.res.ColorStateList
import androidx.lifecycle.ViewModelProvider
import com.example.maadminiproject.viewmodel.device.DeviceViewModel

class DiningActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiningBinding
    private lateinit var deviceViewModel: DeviceViewModel
    private var isProgrammaticUpdate = false

    private var homeId = "home001"
    private var floorId = "floor1"
    private var zoneId = "diningRoom"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDiningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        floorId = intent.getStringExtra("floorId") ?: "floor1"
        zoneId = intent.getStringExtra("zoneId") ?: "diningRoom"

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
            // 1. light03 - Dining Light (Pendant)
            val light = deviceList.find { it.deviceId == "light03" }
            if (light != null) {
                isProgrammaticUpdate = true
                binding.swPendant.isChecked = light.state
                if (light.state) {
                    binding.tvPendantStatus.text = getString(R.string.status_on)
                    binding.tvPendantStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.tvPendantStatus.compoundDrawableTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.ivPendantIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.ivPendantIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.deep_midnight))
                } else {
                    binding.tvPendantStatus.text = getString(R.string.status_off)
                    binding.tvPendantStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.tvPendantStatus.compoundDrawableTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                    binding.ivPendantIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                    binding.ivPendantIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                }
                isProgrammaticUpdate = false
            }

            // 2. plug02 - Dining Smart Plug (Coffee Bar)
            val plug = deviceList.find { it.deviceId == "plug02" }
            if (plug != null) {
                isProgrammaticUpdate = true
                binding.swCoffee.isChecked = plug.state
                if (plug.state) {
                    binding.tvCoffeeStatus.text = getString(R.string.status_on)
                    binding.tvCoffeeStatus.setTextColor(getColor(R.color.vibrant_cyan))
                    binding.tvCoffeeStatus.compoundDrawableTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.ivCoffeeIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                    binding.ivCoffeeIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.deep_midnight))
                } else {
                    binding.tvCoffeeStatus.text = getString(R.string.status_off)
                    binding.tvCoffeeStatus.setTextColor(getColor(R.color.soft_gray))
                    binding.tvCoffeeStatus.compoundDrawableTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                    binding.ivCoffeeIcon.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_container))
                    binding.ivCoffeeIcon.imageTintList = ColorStateList.valueOf(getColor(R.color.soft_gray))
                }
                isProgrammaticUpdate = false
            }
        }
    }

    private fun setupSwitchListeners() {
        binding.swPendant.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "light03", isChecked)
        }

        binding.swCoffee.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticUpdate) return@setOnCheckedChangeListener
            deviceViewModel.toggleDevice(homeId, floorId, zoneId, "plug02", isChecked)
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