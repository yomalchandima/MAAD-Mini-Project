package com.example.maadminiproject.ui.notification

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.Notification
import com.example.maadminiproject.databinding.ItemNotificationBinding

/**
 * RecyclerView adapter for displaying real-time smart home notifications.
 */
class NotificationAdapter(
    private val onNotificationClick: (Notification) -> Unit,
) : ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.tvTitle.text = notification.title.ifBlank { "Notification" }
            binding.tvMessage.text = notification.message

            // Format Relative Timestamp
            if (notification.timestamp > 0L) {
                val now = System.currentTimeMillis()
                val relativeTime = DateUtils.getRelativeTimeSpanString(
                    notification.timestamp,
                    now,
                    DateUtils.MINUTE_IN_MILLIS
                )
                binding.tvTimestamp.text = relativeTime
            } else {
                binding.tvTimestamp.text = ""
            }

            // Type category formatting
            val typeUpper = notification.type.uppercase()
            binding.tvTypeBadge.text = typeUpper.ifBlank { "SYSTEM" }

            when (typeUpper) {
                "SAFETY" -> {
                    val redColor = ContextCompat.getColor(binding.root.context, R.color.error_red)
                    binding.ivTypeIcon.setImageResource(R.drawable.ic_bolt)
                    binding.ivTypeIcon.imageTintList = ColorStateList.valueOf(redColor)
                    binding.tvTypeBadge.setTextColor(redColor)
                    binding.tvTypeBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33FF5252"))
                }
                "SCHEDULE" -> {
                    val cyanColor = ContextCompat.getColor(binding.root.context, R.color.vibrant_cyan)
                    binding.ivTypeIcon.setImageResource(R.drawable.ic_clock)
                    binding.ivTypeIcon.imageTintList = ColorStateList.valueOf(cyanColor)
                    binding.tvTypeBadge.setTextColor(cyanColor)
                    binding.tvTypeBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3300F0FF"))
                }
                "SECURITY" -> {
                    val purpleColor = Color.parseColor("#B388FF")
                    binding.ivTypeIcon.setImageResource(R.drawable.ic_camera)
                    binding.ivTypeIcon.imageTintList = ColorStateList.valueOf(purpleColor)
                    binding.tvTypeBadge.setTextColor(purpleColor)
                    binding.tvTypeBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33B388FF"))
                }
                else -> {
                    val grayColor = ContextCompat.getColor(binding.root.context, R.color.soft_gray)
                    binding.ivTypeIcon.setImageResource(R.drawable.ic_bell)
                    binding.ivTypeIcon.imageTintList = ColorStateList.valueOf(grayColor)
                    binding.tvTypeBadge.setTextColor(grayColor)
                    binding.tvTypeBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33B0BEC5"))
                }
            }

            // Read vs Unread Visual Styling
            if (!notification.isRead) {
                binding.viewUnreadDot.visibility = View.VISIBLE
                binding.tvTitle.setTypeface(null, Typeface.BOLD)
                binding.cardNotification.alpha = 1.0f
            } else {
                binding.viewUnreadDot.visibility = View.GONE
                binding.tvTitle.setTypeface(null, Typeface.NORMAL)
                binding.cardNotification.alpha = 0.7f
            }

            binding.cardNotification.setOnClickListener {
                onNotificationClick(notification)
            }
        }
    }

    private class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.notificationId == newItem.notificationId
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}
