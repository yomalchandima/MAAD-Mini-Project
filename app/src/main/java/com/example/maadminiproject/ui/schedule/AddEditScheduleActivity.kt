package com.example.maadminiproject.ui.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.Device
import com.example.maadminiproject.data.models.Schedule
import com.example.maadminiproject.databinding.ActivityAddEditScheduleBinding
import com.example.maadminiproject.viewmodel.floor.FloorViewModel
import com.example.maadminiproject.viewmodel.schedule.ScheduleViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditScheduleBinding
    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var floorViewModel: FloorViewModel

    private var homeId: String = "home001"
    private var editingScheduleId: String? = null
    private var createdAtTimestamp: Long = 0L

    private var selectedDeviceId: String = ""
    private var selectedDeviceName: String = ""
    private var selectedSwitchId: String? = null
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var selectedAction: String = "ON"
    private var selectedRepeat: String = "NONE"

    // Discovered devices in the home for selection
    private val discoveredDevices = mutableListOf<DiscoveredDeviceItem>()

    data class DiscoveredDeviceItem(
        val deviceId: String,
        val deviceName: String,
        val zoneName: String,
        val switchId: String? = null,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddEditScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        homeId = intent.getStringExtra("homeId") ?: "home001"
        editingScheduleId = intent.getStringExtra("scheduleId")
        createdAtTimestamp = intent.getLongExtra("createdAt", 0L)

        scheduleViewModel = ViewModelProvider(this)[ScheduleViewModel::class.java]
        floorViewModel = ViewModelProvider(this)[FloorViewModel::class.java]

        initFormDefaults()
        parseIntentData()
        setupListeners()
        observeFloorsForDevicePicker()
        updateToggleStyles()

        floorViewModel.observeFloors(homeId)
    }

    private fun updateToggleStyles() {
        // Action toggles
        if (binding.rbActionOn.isChecked) {
            binding.rbActionOn.setBackgroundResource(R.drawable.bg_option_selected)
            binding.rbActionOn.setTextColor(getColor(R.color.deep_midnight))
            binding.rbActionOff.setBackgroundResource(R.drawable.bg_social_button)
            binding.rbActionOff.setTextColor(getColor(R.color.white))
        } else {
            binding.rbActionOn.setBackgroundResource(R.drawable.bg_social_button)
            binding.rbActionOn.setTextColor(getColor(R.color.white))
            binding.rbActionOff.setBackgroundResource(R.drawable.bg_option_selected)
            binding.rbActionOff.setTextColor(getColor(R.color.deep_midnight))
        }

        // Repeat toggles
        binding.rbRepeatOnce.setBackgroundResource(if (binding.rbRepeatOnce.isChecked) R.drawable.bg_option_selected else R.drawable.bg_social_button)
        binding.rbRepeatOnce.setTextColor(if (binding.rbRepeatOnce.isChecked) getColor(R.color.deep_midnight) else getColor(R.color.white))

        binding.rbRepeatDaily.setBackgroundResource(if (binding.rbRepeatDaily.isChecked) R.drawable.bg_option_selected else R.drawable.bg_social_button)
        binding.rbRepeatDaily.setTextColor(if (binding.rbRepeatDaily.isChecked) getColor(R.color.deep_midnight) else getColor(R.color.white))

        binding.rbRepeatWeekdays.setBackgroundResource(if (binding.rbRepeatWeekdays.isChecked) R.drawable.bg_option_selected else R.drawable.bg_social_button)
        binding.rbRepeatWeekdays.setTextColor(if (binding.rbRepeatWeekdays.isChecked) getColor(R.color.deep_midnight) else getColor(R.color.white))
    }

    private fun initFormDefaults() {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        selectedDate = dateFormat.format(cal.time)
        // Default to next hour if possible
        cal.add(Calendar.HOUR_OF_DAY, 1)
        cal.set(Calendar.MINUTE, 0)
        selectedTime = timeFormat.format(cal.time)

        binding.tvSelectedDate.text = selectedDate
        binding.tvSelectedTime.text = selectedTime
    }

    private fun parseIntentData() {
        val isEdit = !editingScheduleId.isNullOrBlank()

        if (isEdit) {
            binding.tvHeaderTitle.text = getString(R.string.edit_schedule)
            binding.btnHeaderDelete.visibility = View.VISIBLE
            binding.btnSaveSchedule.text = getString(R.string.save)

            selectedDeviceId = intent.getStringExtra("deviceId").orEmpty()
            selectedDeviceName = intent.getStringExtra("deviceName").orEmpty()
            selectedAction = intent.getStringExtra("action") ?: "ON"
            selectedDate = intent.getStringExtra("startDate") ?: selectedDate
            selectedTime = intent.getStringExtra("startTime") ?: selectedTime
            selectedRepeat = intent.getStringExtra("repeat") ?: "NONE"
            selectedSwitchId = intent.getStringExtra("switchId")

            binding.tvSelectedDeviceName.text = if (selectedDeviceName.isNotBlank()) selectedDeviceName else selectedDeviceId
            binding.tvSelectedDeviceRoom.text = "Selected device"
            binding.tvSelectedDate.text = selectedDate
            binding.tvSelectedTime.text = selectedTime

            // Action radio
            if (selectedAction.equals("OFF", ignoreCase = true)) {
                binding.rbActionOff.isChecked = true
            } else {
                binding.rbActionOn.isChecked = true
            }

            // Repeat radio
            when (selectedRepeat.uppercase()) {
                "DAILY" -> binding.rbRepeatDaily.isChecked = true
                "WEEKDAYS" -> binding.rbRepeatWeekdays.isChecked = true
                else -> binding.rbRepeatOnce.isChecked = true
            }

            updateDeviceIcon(selectedDeviceId)
        } else {
            binding.tvHeaderTitle.text = getString(R.string.add_schedule)
            binding.btnHeaderDelete.visibility = View.GONE
            binding.btnSaveSchedule.text = getString(R.string.create)
            binding.rbActionOn.isChecked = true
            binding.rbRepeatOnce.isChecked = true
        }

        updateDateVisibility()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnHeaderDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.cardSelectDevice.setOnClickListener {
            showDevicePickerDialog()
        }

        binding.cardSelectDate.setOnClickListener {
            showDatePicker()
        }

        binding.cardSelectTime.setOnClickListener {
            showTimePicker()
        }

        binding.rgAction.setOnCheckedChangeListener { _, checkedId ->
            selectedAction = if (checkedId == R.id.rbActionOff) "OFF" else "ON"
            updateToggleStyles()
        }

        binding.rgRepeat.setOnCheckedChangeListener { _, checkedId ->
            selectedRepeat = when (checkedId) {
                R.id.rbRepeatDaily -> "DAILY"
                R.id.rbRepeatWeekdays -> "WEEKDAYS"
                else -> "NONE"
            }
            updateDateVisibility()
            updateToggleStyles()
        }

        binding.btnSaveSchedule.setOnClickListener {
            saveSchedule()
        }
    }

    private fun updateDateVisibility() {
        // Date is always shown, but for Daily/Weekdays it represents the starting date
        if (selectedRepeat == "NONE" || selectedRepeat == "ONCE") {
            binding.tvDateHeader.text = getString(R.string.date_label) + " (Required for One-Time)"
        } else {
            binding.tvDateHeader.text = getString(R.string.date_label) + " (Starting from)"
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        if (selectedDate.isNotBlank()) {
            try {
                val parts = selectedDate.split("-")
                if (parts.size == 3) {
                    cal.set(Calendar.YEAR, parts[0].toInt())
                    cal.set(Calendar.MONTH, parts[1].toInt() - 1)
                    cal.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                }
            } catch (_: Exception) {}
        }

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                selectedDate = formatted
                binding.tvSelectedDate.text = formatted
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        if (selectedTime.isNotBlank()) {
            try {
                val parts = selectedTime.split(":")
                if (parts.size == 2) {
                    cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    cal.set(Calendar.MINUTE, parts[1].toInt())
                }
            } catch (_: Exception) {}
        }

        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val formatted = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                selectedTime = formatted
                binding.tvSelectedTime.text = formatted
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true // 24 hour view
        )
        timePicker.show()
    }

    private fun observeFloorsForDevicePicker() {
        floorViewModel.floors.observe(this) { floors ->
            discoveredDevices.clear()
            floors.forEach { floor ->
                floor.zones.values.forEach { zone ->
                    zone.devices.values.forEach { device ->
                        discoveredDevices.add(
                            DiscoveredDeviceItem(
                                deviceId = device.deviceId,
                                deviceName = device.deviceName.ifBlank { device.deviceId },
                                zoneName = zone.zoneName.ifBlank { zone.zoneId }
                            )
                        )
                    }
                }
            }

            // If we have selectedDeviceId already, update subtitle room name
            if (selectedDeviceId.isNotBlank()) {
                val found = discoveredDevices.find { it.deviceId == selectedDeviceId }
                if (found != null) {
                    binding.tvSelectedDeviceName.text = found.deviceName
                    binding.tvSelectedDeviceRoom.text = found.zoneName
                }
            }
        }
    }

    private fun showDevicePickerDialog() {
        if (discoveredDevices.isEmpty()) {
            Toast.makeText(this, "Loading devices from home...", Toast.LENGTH_SHORT).show()
            return
        }

        val itemNames = discoveredDevices.map { "${it.deviceName} (${it.zoneName})" }.toTypedArray()
        val currentIndex = discoveredDevices.indexOfFirst { it.deviceId == selectedDeviceId }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.select_device))
            .setSingleChoiceItems(itemNames, currentIndex) { dialog, which ->
                val chosen = discoveredDevices[which]
                selectedDeviceId = chosen.deviceId
                selectedDeviceName = chosen.deviceName
                selectedSwitchId = chosen.switchId

                binding.tvSelectedDeviceName.text = chosen.deviceName
                binding.tvSelectedDeviceRoom.text = chosen.zoneName
                updateDeviceIcon(chosen.deviceId)

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateDeviceIcon(deviceId: String) {
        val iconRes = when {
            deviceId.contains("light", ignoreCase = true) -> R.drawable.ic_lightbulb
            deviceId.contains("fan", ignoreCase = true) -> R.drawable.ic_fan
            deviceId.contains("ac", ignoreCase = true) -> R.drawable.ic_ac
            deviceId.contains("iron", ignoreCase = true) -> R.drawable.ic_bolt
            deviceId.contains("switch", ignoreCase = true) -> R.drawable.ic_tune
            else -> R.drawable.ic_power_plug
        }
        binding.ivSelectedDeviceIcon.setImageResource(iconRes)
    }

    private fun saveSchedule() {
        if (selectedDeviceId.isBlank()) {
            Toast.makeText(this, "Please select a target device", Toast.LENGTH_SHORT).show()
            showDevicePickerDialog()
            return
        }

        if (selectedTime.isBlank()) {
            Toast.makeText(this, "Please set a valid start time", Toast.LENGTH_SHORT).show()
            showTimePicker()
            return
        }

        if ((selectedRepeat == "NONE" || selectedRepeat == "ONCE") && selectedDate.isBlank()) {
            Toast.makeText(this, "Please select a valid date for this one-time schedule", Toast.LENGTH_SHORT).show()
            showDatePicker()
            return
        }

        val isEdit = !editingScheduleId.isNullOrBlank()
        val finalScheduleId = if (isEdit) editingScheduleId!! else ""

        val scheduleToSave = Schedule(
            scheduleId = finalScheduleId,
            deviceId = selectedDeviceId,
            deviceName = selectedDeviceName,
            action = selectedAction,
            startDate = selectedDate,
            startTime = selectedTime,
            endTime = null,
            repeat = selectedRepeat,
            enabled = true,
            switchId = selectedSwitchId,
            createdAt = if (createdAtTimestamp > 0L) createdAtTimestamp else System.currentTimeMillis()
        )

        binding.btnSaveSchedule.isEnabled = false

        if (isEdit) {
            scheduleViewModel.updateSchedule(
                homeId = homeId,
                schedule = scheduleToSave,
                onSuccess = {
                    Toast.makeText(this, getString(R.string.schedule_updated), Toast.LENGTH_SHORT).show()
                    finish()
                }
            )
        } else {
            scheduleViewModel.createSchedule(
                homeId = homeId,
                schedule = scheduleToSave,
                onSuccess = { _ ->
                    Toast.makeText(this, getString(R.string.schedule_created), Toast.LENGTH_SHORT).show()
                    finish()
                }
            )
        }
    }

    private fun showDeleteConfirmation() {
        val sId = editingScheduleId ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_schedule_title))
            .setMessage(getString(R.string.delete_schedule_confirm, selectedDeviceName.ifBlank { selectedDeviceId }))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                scheduleViewModel.deleteSchedule(
                    homeId = homeId,
                    scheduleId = sId,
                    onSuccess = {
                        Toast.makeText(this, getString(R.string.schedule_deleted), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
