package com.example.maadminiproject.ui.report

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.Device
import com.example.maadminiproject.databinding.ActivityReportsBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity
import com.example.maadminiproject.viewmodel.report.ReportViewModel

/**
 * Screen displaying live system reports, device statistics, power distribution, and activity logs.
 */
class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding
    private val viewModel: ReportViewModel by viewModels()
    private lateinit var activityLogAdapter: ActivityLogAdapter

    private val homeId = "home001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupRecyclerView()
        setupBottomNav()
        observeViewModel()

        viewModel.observeReport(homeId)
    }

    private fun setupRecyclerView() {
        activityLogAdapter = ActivityLogAdapter()
        binding.rvRecentActivity.layoutManager = LinearLayoutManager(this)
        binding.rvRecentActivity.adapter = activityLogAdapter
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Live Device Summary & Efficiency Cards
        viewModel.devices.observe(this) { _ ->
            val total = viewModel.totalDevicesCount.value ?: 0
            val active = viewModel.activeDevicesCount.value ?: 0
            val online = viewModel.onlineDevicesCount.value ?: 0
            val watts = viewModel.totalActivePowerWatts.value ?: 0.0

            binding.tvMainMsg.text = "$active of $total Devices Active"
            binding.tvEfficiencySub.text = "Current active load: ${watts.toInt()} W • $online devices online"
            binding.tvConnectedCountVal.text = "$online / $total"
        }

        // Live Active Power
        viewModel.totalActivePowerWatts.observe(this) { watts ->
            binding.tvActiveLoadVal.text = "${watts.toInt()}"
            binding.tvLoadCenterText.text = "${watts.toInt()} W\nActive"

            val total = viewModel.totalDevicesCount.value ?: 0
            val active = viewModel.activeDevicesCount.value ?: 0
            val online = viewModel.onlineDevicesCount.value ?: 0
            binding.tvEfficiencySub.text = "Current active load: ${watts.toInt()} W • $online devices online"
        }

        // Load Distribution Breakdown
        viewModel.categoryLoads.observe(this) { categoryMap ->
            val totalWatts = viewModel.totalActivePowerWatts.value ?: 0.0

            fun formatLoad(category: String): String {
                val watts = categoryMap[category] ?: 0.0
                val percent = if (totalWatts > 0.0) ((watts / totalWatts) * 100).toInt() else 0
                return "${watts.toInt()} W ($percent%)"
            }

            binding.tvClimateLoadPercent.text = formatLoad("Climate")
            binding.tvKitchenLoadPercent.text = formatLoad("Kitchen")
            binding.tvLightsLoadPercent.text = formatLoad("Lights")
            binding.tvOtherLoadPercent.text = formatLoad("Other")
        }

        // Top Consumer Devices
        viewModel.topConsumers.observe(this) { consumers ->
            bindTopConsumers(consumers)
        }

        // Activity Logs Timeline
        viewModel.activityLogs.observe(this) { logs ->
            activityLogAdapter.submitList(logs)
            if (logs.isEmpty()) {
                binding.tvEmptyActivity.visibility = View.VISIBLE
                binding.rvRecentActivity.visibility = View.GONE
            } else {
                binding.tvEmptyActivity.visibility = View.GONE
                binding.rvRecentActivity.visibility = View.VISIBLE
            }
        }
    }

    private fun bindTopConsumers(consumers: List<Device>) {
        val cyanColor = ContextCompat.getColor(this, R.color.vibrant_cyan)
        val grayColor = ContextCompat.getColor(this, R.color.soft_gray)

        fun bindConsumerCard(
            card: View,
            nameView: android.widget.TextView,
            usageView: android.widget.TextView,
            statusView: android.widget.TextView,
            iconView: android.widget.ImageView,
            device: Device?
        ) {
            if (device != null) {
                card.visibility = View.VISIBLE
                nameView.text = device.deviceName.ifBlank { device.deviceId }
                usageView.text = "${device.power.toInt()} W Rating"

                if (device.state) {
                    statusView.text = "ACTIVE"
                    statusView.setTextColor(cyanColor)
                    iconView.imageTintList = ColorStateList.valueOf(cyanColor)
                } else {
                    statusView.text = "OFF"
                    statusView.setTextColor(grayColor)
                    iconView.imageTintList = ColorStateList.valueOf(grayColor)
                }

                when {
                    device.type.contains("ac", ignoreCase = true) -> iconView.setImageResource(R.drawable.ic_ac)
                    device.type.contains("fan", ignoreCase = true) -> iconView.setImageResource(R.drawable.ic_fan)
                    device.type.contains("light", ignoreCase = true) -> iconView.setImageResource(R.drawable.ic_lightbulb)
                    else -> iconView.setImageResource(R.drawable.ic_power_plug)
                }
            } else {
                card.visibility = View.GONE
            }
        }

        bindConsumerCard(
            binding.cardConsumer1,
            binding.tvConsumer1Name,
            binding.tvConsumer1Usage,
            binding.tvConsumer1Status,
            binding.ivConsumer1,
            consumers.getOrNull(0)
        )

        bindConsumerCard(
            binding.cardConsumer2,
            binding.tvConsumer2Name,
            binding.tvConsumer2Usage,
            binding.tvConsumer2Status,
            binding.ivConsumer2,
            consumers.getOrNull(1)
        )

        bindConsumerCard(
            binding.cardConsumer3,
            binding.tvConsumer3Name,
            binding.tvConsumer3Usage,
            binding.tvConsumer3Status,
            binding.ivConsumer3,
            consumers.getOrNull(2)
        )
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_reports
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
                R.id.nav_reports -> true
                else -> false
            }
        }
    }
}