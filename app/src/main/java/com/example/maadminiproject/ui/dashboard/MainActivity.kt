package com.example.maadminiproject.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityMainBinding
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity
import com.example.maadminiproject.viewmodel.dashboard.DashboardViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: DashboardViewModel

    private val homeId = "home001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupNavigation()
        observeViewModel()
        
        viewModel.observeDashboard(homeId, "floor1", emptyList())
    }

    private fun setupNavigation() {
        binding.tvManage.setOnClickListener {
            startActivity(Intent(this, FloorActivity::class.java))
        }

        binding.cardGroundFloor.setOnClickListener {
            startActivity(
                Intent(this, com.example.maadminiproject.ui.floor.FloorDetailActivity::class.java)
                    .putExtra("floorId", "floor1")
            )
        }

        binding.cardFirstFloor.setOnClickListener {
            startActivity(
                Intent(this, com.example.maadminiproject.ui.floor.FloorDetailActivity::class.java)
                    .putExtra("floorId", "floor2")
            )
        }

        binding.cardAutomations.setOnClickListener {
            startActivity(Intent(this, com.example.maadminiproject.ui.schedule.ScheduleActivity::class.java))
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_floors -> {
                    startActivity(Intent(this, FloorActivity::class.java))
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
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.totalDevices.observe(this) { total ->
            binding.tvSystemsStatus.text = getString(R.string.devices_count_dynamic, total)
        }

        viewModel.floors.observe(this) { floors ->
            floors.forEach { floor ->
                val activeCount = floor.zones.values.sumOf { it.devices.values.count { d -> d.state } }
                val roomCount = floor.zones.size
                val statusText = "$activeCount Active  •  $roomCount Rooms"
                
                when (floor.floorId) {
                    "floor1" -> {
                        binding.tvGroundFloorStatus.text = statusText
                        binding.tvAlertsFloorName.text = floor.floorName
                        binding.tvAlertsFloorStatus.text = statusText
                    }
                    "floor2" -> binding.tvFirstFloorStatus.text = statusText
                }
            }
        }
    }
}