package com.example.maadminiproject.ui.floor

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.Device
import com.example.maadminiproject.data.models.Zone
import com.example.maadminiproject.databinding.ActivityFloorDetailBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.room.*
import com.example.maadminiproject.ui.settings.SettingsActivity
import com.example.maadminiproject.viewmodel.device.DeviceViewModel
import com.example.maadminiproject.viewmodel.floor.FloorViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FloorDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFloorDetailBinding
    private lateinit var floorViewModel: FloorViewModel
    private lateinit var deviceViewModel: DeviceViewModel
    private lateinit var zoneAdapter: ZoneAdapter

    private val homeId = "home001"
    private var floorId: String = "floor1"
    private var isGridOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFloorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        floorId = intent.getStringExtra("floorId") ?: "floor1"

        floorViewModel = ViewModelProvider(this)[FloorViewModel::class.java]
        deviceViewModel = ViewModelProvider(this)[DeviceViewModel::class.java]

        setupUI()
        setupBottomNav()
        observeData()

        floorViewModel.observeFloors(homeId)
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup zones recycler view with 2 columns
        zoneAdapter = ZoneAdapter(emptyList()) { zone ->
            navigateToRoom(zone)
        }
        binding.rvZones.layoutManager = GridLayoutManager(this, 2)
        binding.rvZones.adapter = zoneAdapter

        // Grid toggle button
        binding.btnToggleGrid.setOnClickListener {
            isGridOn = !isGridOn
            binding.floorPlanOverlay.setGridEnabled(isGridOn)
            if (isGridOn) {
                binding.btnToggleGrid.text = getString(R.string.grid_on)
                binding.btnToggleGrid.setTextColor(getColor(R.color.vibrant_cyan))
                binding.btnToggleGrid.iconTint = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                binding.btnToggleGrid.strokeColor = ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
            } else {
                binding.btnToggleGrid.text = getString(R.string.grid_off)
                binding.btnToggleGrid.setTextColor(getColor(R.color.soft_gray))
                binding.btnToggleGrid.iconTint = ColorStateList.valueOf(getColor(R.color.soft_gray))
                binding.btnToggleGrid.strokeColor = ColorStateList.valueOf(getColor(R.color.surface_container))
            }
        }

        // Device marker click on the floor plan
        binding.floorPlanOverlay.setOnDeviceClickListener { device ->
            showQuickDeviceDialog(device)
        }
    }

    private fun observeData() {
        floorViewModel.floors.observe(this) { floorList ->
            val floor = floorList.find { it.floorId == floorId } ?: return@observe

            binding.tvFloorTitle.text = floor.floorName.ifBlank { floor.floorId }
            binding.floorPlanOverlay.setFloorPlan(floor.floorPlanImage)

            val zonesList = floor.zones.values.toList()
            zoneAdapter.updateZones(zonesList)

            // Collect all devices from all zones on this floor
            val allDevices = mutableListOf<Device>()
            var activeCount = 0

            for (zone in zonesList) {
                for (device in zone.devices.values) {
                    allDevices.add(device)
                    if (device.state) {
                        activeCount++
                    }
                }
            }

            binding.tvActiveCount.text = getString(R.string.active_count_dynamic, activeCount)
            binding.floorPlanOverlay.setDevices(allDevices)
        }
    }

    private fun showQuickDeviceDialog(device: Device) {
        val stateText = if (device.state) "ON" else "OFF"
        val toggleActionText = if (device.state) "Turn OFF" else "Turn ON"

        MaterialAlertDialogBuilder(this)
            .setTitle(device.deviceName.ifBlank { device.deviceId })
            .setMessage("Type: ${device.type}\nStatus: ${device.status}\nCurrent State: $stateText")
            .setPositiveButton(toggleActionText) { dialog, _ ->
                val targetZone = device.zoneId.ifBlank { "livingRoom" }
                deviceViewModel.toggleDevice(homeId, floorId, targetZone, device.deviceId, !device.state)
                Toast.makeText(this, "${device.deviceName} toggled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNeutralButton(getString(R.string.go_to_room)) { dialog, _ ->
                val dummyZone = Zone(zoneId = device.zoneId)
                navigateToRoom(dummyZone)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToRoom(zone: Zone) {
        val targetClass = when (zone.zoneId.lowercase()) {
            "livingroom", "living" -> LivingRoomActivity::class.java
            "kitchen" -> KitchenActivity::class.java
            "diningroom", "dining" -> DiningActivity::class.java
            "garage" -> GarageActivity::class.java
            "bathroomgf" -> GroundFloorBathActivity::class.java
            "staircase" -> StaircaseActivity::class.java
            "masterbedroom", "master" -> MasterBedroomActivity::class.java
            "bedroom2", "bedroom" -> Bedroom2Activity::class.java
            "workroom", "office" -> WorkRoomActivity::class.java
            "bathroomff", "bathroom" -> BathroomActivity::class.java
            "hallway" -> HallwayActivity::class.java
            else -> LivingRoomActivity::class.java
        }
        startActivity(Intent(this, targetClass))
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_floors
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
