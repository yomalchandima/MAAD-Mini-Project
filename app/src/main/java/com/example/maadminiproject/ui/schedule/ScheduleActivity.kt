package com.example.maadminiproject.ui.schedule

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.Schedule
import com.example.maadminiproject.databinding.ActivityScheduleBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.report.ReportsActivity
import com.example.maadminiproject.ui.settings.SettingsActivity
import com.example.maadminiproject.viewmodel.schedule.ScheduleViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var scheduleAdapter: ScheduleAdapter

    private val homeId = "home001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        scheduleViewModel = ViewModelProvider(this)[ScheduleViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        setupBottomNav()
        observeViewModel()

        scheduleViewModel.observeSchedules(homeId)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAddScheduleHeader.setOnClickListener {
            openAddSchedule()
        }

        binding.btnEmptyAdd.setOnClickListener {
            openAddSchedule()
        }
    }

    private fun openAddSchedule() {
        val intent = Intent(this, AddEditScheduleActivity::class.java).apply {
            putExtra("homeId", homeId)
        }
        startActivity(intent)
    }

    private fun openEditSchedule(schedule: Schedule) {
        val intent = Intent(this, AddEditScheduleActivity::class.java).apply {
            putExtra("homeId", homeId)
            putExtra("scheduleId", schedule.scheduleId)
            putExtra("deviceId", schedule.deviceId)
            putExtra("deviceName", schedule.deviceName)
            putExtra("action", schedule.action)
            putExtra("startDate", schedule.startDate)
            putExtra("startTime", schedule.startTime)
            putExtra("repeat", schedule.repeat)
            putExtra("enabled", schedule.enabled)
            putExtra("switchId", schedule.switchId)
            putExtra("createdAt", schedule.createdAt)
        }
        startActivity(intent)
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(
            schedules = emptyList(),
            onToggleEnabled = { schedule, isChecked ->
                scheduleViewModel.setScheduleEnabled(
                    homeId = homeId,
                    scheduleId = schedule.scheduleId,
                    enabled = isChecked,
                    onSuccess = {
                        val msg = if (isChecked) "Schedule enabled" else "Schedule disabled"
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onEdit = { schedule ->
                openEditSchedule(schedule)
            },
            onDelete = { schedule ->
                showDeleteConfirmation(schedule)
            }
        )

        binding.rvSchedules.layoutManager = LinearLayoutManager(this)
        binding.rvSchedules.adapter = scheduleAdapter
    }

    private fun showDeleteConfirmation(schedule: Schedule) {
        val displayName = if (schedule.deviceName.isNotBlank()) schedule.deviceName else schedule.deviceId
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_schedule_title))
            .setMessage(getString(R.string.delete_schedule_confirm, displayName))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                scheduleViewModel.deleteSchedule(
                    homeId = homeId,
                    scheduleId = schedule.scheduleId,
                    onSuccess = {
                        Toast.makeText(this, getString(R.string.schedule_deleted), Toast.LENGTH_SHORT).show()
                    }
                )
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeViewModel() {
        scheduleViewModel.schedules.observe(this) { schedules ->
            scheduleAdapter.updateSchedules(schedules)

            val activeCount = schedules.count { it.enabled }
            binding.tvActiveStats.text = "$activeCount Active Schedule${if (activeCount != 1) "s" else ""}"

            if (schedules.isEmpty()) {
                binding.layoutEmptySchedules.visibility = View.VISIBLE
                binding.rvSchedules.visibility = View.GONE
            } else {
                binding.layoutEmptySchedules.visibility = View.GONE
                binding.rvSchedules.visibility = View.VISIBLE
            }
        }

        scheduleViewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        scheduleViewModel.errorMessage.observe(this) { err ->
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_LONG).show()
                scheduleViewModel.clearError()
            }
        }
    }

    private fun setupBottomNav() {
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
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
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
}
