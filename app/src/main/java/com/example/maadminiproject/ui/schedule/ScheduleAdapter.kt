package com.example.maadminiproject.ui.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.Schedule
import com.example.maadminiproject.databinding.ItemScheduleCardBinding

class ScheduleAdapter(
    private var schedules: List<Schedule>,
    private val onToggleEnabled: (Schedule, Boolean) -> Unit,
    private val onEdit: (Schedule) -> Unit,
    private val onDelete: (Schedule) -> Unit,
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    fun updateSchedules(newSchedules: List<Schedule>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(schedules[position])
    }

    override fun getItemCount(): Int = schedules.size

    inner class ScheduleViewHolder(private val binding: ItemScheduleCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: Schedule) {
            val context = binding.root.context

            // Device Name
            binding.tvDeviceName.text = if (schedule.deviceName.isNotBlank()) {
                schedule.deviceName
            } else {
                schedule.deviceId
            }

            // Action Badge (ON / OFF)
            val isActionOn = schedule.action.equals("ON", ignoreCase = true)
            binding.tvActionBadge.text = if (isActionOn) "ON" else "OFF"
            if (isActionOn) {
                binding.tvActionBadge.setBackgroundResource(R.drawable.bg_badge_on)
                binding.tvActionBadge.setTextColor(ContextCompat.getColor(context, R.color.vibrant_cyan))
            } else {
                binding.tvActionBadge.setBackgroundResource(R.drawable.bg_badge_off)
                binding.tvActionBadge.setTextColor(ContextCompat.getColor(context, R.color.error_red))
            }

            // Target Detail / Switch info
            binding.tvTargetDetail.text = if (schedule.switchId != null) {
                "Switch ${schedule.switchId.replace("switch_", "")}"
            } else {
                "Standard ON/OFF"
            }

            // Device Icon heuristic
            val iconRes = when {
                schedule.deviceId.contains("light", ignoreCase = true) -> R.drawable.ic_lightbulb
                schedule.deviceId.contains("fan", ignoreCase = true) -> R.drawable.ic_fan
                schedule.deviceId.contains("ac", ignoreCase = true) -> R.drawable.ic_ac
                schedule.deviceId.contains("iron", ignoreCase = true) -> R.drawable.ic_bolt
                schedule.deviceId.contains("switch", ignoreCase = true) -> R.drawable.ic_tune
                else -> R.drawable.ic_power_plug
            }
            binding.ivDeviceIcon.setImageResource(iconRes)

            // Start Time
            binding.tvStartTime.text = schedule.startTime.ifBlank { "--:--" }

            // Repeat & Date Pill
            val repeatUpper = schedule.repeat.uppercase()
            val repeatText = when (repeatUpper) {
                "DAILY" -> "Daily"
                "WEEKDAYS" -> "Weekdays"
                "NONE", "ONCE", "" -> {
                    if (schedule.startDate.isNotBlank()) {
                        "Once • ${schedule.startDate}"
                    } else {
                        "Once"
                    }
                }
                else -> schedule.repeat
            }
            binding.tvRepeatBadge.text = repeatText

            // Enabled Switch (clear listener first to prevent loop on bind)
            binding.switchSchedule.setOnCheckedChangeListener(null)
            binding.switchSchedule.isChecked = schedule.enabled
            binding.switchSchedule.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != schedule.enabled) {
                    onToggleEnabled(schedule, isChecked)
                }
            }

            // Edit & Delete actions
            binding.btnEditSchedule.setOnClickListener {
                onEdit(schedule)
            }

            binding.btnDeleteSchedule.setOnClickListener {
                onDelete(schedule)
            }

            binding.root.setOnClickListener {
                onEdit(schedule)
            }
        }
    }
}
