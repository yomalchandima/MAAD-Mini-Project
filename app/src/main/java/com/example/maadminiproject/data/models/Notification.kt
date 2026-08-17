package com.example.maadminiproject.data.models

import com.google.firebase.database.PropertyName

/**
 * Data model representing a smart home notification.
 *
 * @property notificationId Unique identifier for the notification.
 * @property title The title of the notification (e.g., "Motion Detected").
 * @property message The detailed content of the notification.
 * @property type The category of notification (e.g., "SAFETY", "SCHEDULE", "SECURITY", "SYSTEM").
 * @property timestamp The time when the notification was generated.
 * @property isRead Whether the user has viewed the notification.
 * @property deviceId The ID of the device associated with this notification, if applicable.
 */
data class Notification(
    var notificationId: String = "",
    var title: String = "",
    var message: String = "",
    var type: String = "",
    var timestamp: Long = 0L,
    @get:PropertyName("isRead") @set:PropertyName("isRead") var isRead: Boolean = false,
    var deviceId: String = "",
)
