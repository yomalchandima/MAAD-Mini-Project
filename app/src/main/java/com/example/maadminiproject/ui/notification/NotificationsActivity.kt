package com.example.maadminiproject.ui.notification

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.Notification
import com.example.maadminiproject.databinding.ActivityNotificationsBinding
import com.example.maadminiproject.viewmodel.notification.NotificationViewModel

/**
 * Screen displaying real-time smart home alerts and notifications.
 */
class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter

    private var currentFilter: String = "ALL"
    private var allNotifications: List<Notification> = emptyList()
    private val homeId = "home001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        setupObservers()

        viewModel.observeNotifications(homeId)
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            if (!notification.isRead && notification.notificationId.isNotBlank()) {
                viewModel.markAsRead(homeId, notification.notificationId)
            }
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnMarkAllRead.setOnClickListener {
            viewModel.markAllAsRead(homeId)
        }

        binding.chipAll.setOnClickListener { setFilter("ALL") }
        binding.chipUnread.setOnClickListener { setFilter("UNREAD") }
        binding.chipSafety.setOnClickListener { setFilter("SAFETY") }
        binding.chipSchedule.setOnClickListener { setFilter("SCHEDULE") }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.notifications.observe(this) { notifications ->
            allNotifications = notifications
            val unreadCount = notifications.count { !it.isRead }
            if (unreadCount > 0) {
                binding.tvHeaderSubtitle.text = getString(R.string.unread_count_format, unreadCount)
                binding.btnMarkAllRead.visibility = View.VISIBLE
            } else {
                binding.tvHeaderSubtitle.text = getString(R.string.notifications_subtitle)
                binding.btnMarkAllRead.visibility = View.GONE
            }

            applyFilter()
        }
    }

    private fun setFilter(filter: String) {
        currentFilter = filter
        updateFilterChipStyles()
        applyFilter()
    }

    private fun updateFilterChipStyles() {
        val selectedBg = R.drawable.bg_option_selected
        val normalBg = R.drawable.bg_social_button
        val cyanColor = ContextCompat.getColor(this, R.color.vibrant_cyan)
        val grayColor = ContextCompat.getColor(this, R.color.soft_gray)

        fun styleChip(chip: TextView, isSelected: Boolean) {
            chip.setBackgroundResource(if (isSelected) selectedBg else normalBg)
            chip.setTextColor(if (isSelected) cyanColor else grayColor)
        }

        styleChip(binding.chipAll, currentFilter == "ALL")
        styleChip(binding.chipUnread, currentFilter == "UNREAD")
        styleChip(binding.chipSafety, currentFilter == "SAFETY")
        styleChip(binding.chipSchedule, currentFilter == "SCHEDULE")
    }

    private fun applyFilter() {
        val filteredList = when (currentFilter) {
            "UNREAD" -> allNotifications.filter { !it.isRead }
            "SAFETY" -> allNotifications.filter { it.type.equals("SAFETY", ignoreCase = true) }
            "SCHEDULE" -> allNotifications.filter { it.type.equals("SCHEDULE", ignoreCase = true) }
            else -> allNotifications
        }

        adapter.submitList(filteredList)

        if (filteredList.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvNotifications.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvNotifications.visibility = View.VISIBLE
        }
    }
}
