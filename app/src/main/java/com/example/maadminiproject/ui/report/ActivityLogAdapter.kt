package com.example.maadminiproject.ui.report

import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.ActivityLog
import com.example.maadminiproject.databinding.ItemActivityLogBinding

/**
 * RecyclerView adapter for displaying activity logs inside the Reports screen.
 */
class ActivityLogAdapter : ListAdapter<ActivityLog, ActivityLogAdapter.LogViewHolder>(LogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemActivityLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LogViewHolder(
        private val binding: ItemActivityLogBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(log: ActivityLog) {
            val context = binding.root.context
            binding.tvLogDeviceName.text = log.deviceName.ifBlank { log.deviceId.ifBlank { "Smart Device" } }
            binding.tvLogAction.text = log.action.replace("_", " ").uppercase()
            binding.tvLogDescription.text = log.description.ifBlank { "Action performed" }
            binding.tvLogPerformedBy.text = log.performedBy.ifBlank { "system" }

            if (log.timestamp > 0L) {
                val now = System.currentTimeMillis()
                binding.tvLogTime.text = DateUtils.getRelativeTimeSpanString(
                    log.timestamp,
                    now,
                    DateUtils.MINUTE_IN_MILLIS
                )
            } else {
                binding.tvLogTime.text = ""
            }

            // Styling based on action type
            val redColor = ContextCompat.getColor(context, R.color.error_red)
            val cyanColor = ContextCompat.getColor(context, R.color.vibrant_cyan)
            val grayColor = ContextCompat.getColor(context, R.color.soft_gray)

            when {
                log.action.contains("SAFETY", ignoreCase = true) -> {
                    binding.ivLogIcon.setImageResource(R.drawable.ic_bolt)
                    binding.ivLogIcon.imageTintList = ColorStateList.valueOf(redColor)
                    binding.tvLogAction.setTextColor(redColor)
                }
                log.action.contains("SCHEDULE", ignoreCase = true) -> {
                    binding.ivLogIcon.setImageResource(R.drawable.ic_clock)
                    binding.ivLogIcon.imageTintList = ColorStateList.valueOf(cyanColor)
                    binding.tvLogAction.setTextColor(cyanColor)
                }
                log.action.contains("ON", ignoreCase = true) -> {
                    binding.ivLogIcon.setImageResource(R.drawable.ic_tune)
                    binding.ivLogIcon.imageTintList = ColorStateList.valueOf(cyanColor)
                    binding.tvLogAction.setTextColor(cyanColor)
                }
                else -> {
                    binding.ivLogIcon.setImageResource(R.drawable.ic_tune)
                    binding.ivLogIcon.imageTintList = ColorStateList.valueOf(grayColor)
                    binding.tvLogAction.setTextColor(grayColor)
                }
            }
        }
    }

    private class LogDiffCallback : DiffUtil.ItemCallback<ActivityLog>() {
        override fun areItemsTheSame(oldItem: ActivityLog, newItem: ActivityLog): Boolean {
            return oldItem.logId == newItem.logId
        }

        override fun areContentsTheSame(oldItem: ActivityLog, newItem: ActivityLog): Boolean {
            return oldItem == newItem
        }
    }
}
