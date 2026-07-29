package com.example.maadminiproject.data.models

/**
 * Data model representing a smart home notification.
 *
 * @property notificationId Unique identifier for the notification.
 * @property title The title of the notification (e.g., "Motion Detected").
 * @property message The detailed content of the notification.
 * @property type The category of notification (e.g., "Security", "System", "Device").
 * @property timestamp The time when the notification was generated.
 * @property isRead Whether the user has viewed the notification.
 * @property deviceId The ID of the device associated with this notification, if applicable.
 */
data class Notification(
    val notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false,
    val deviceId: String = "",
)
