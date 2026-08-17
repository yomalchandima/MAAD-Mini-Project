package com.example.maadminiproject

import com.example.maadminiproject.data.models.Notification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying the Notification model, field integrity, and sorting for Phase 11B.
 */
class NotificationModelTest {

    @Test
    fun notification_defaultValues_areCorrect() {
        val notification = Notification()
        assertEquals("", notification.notificationId)
        assertEquals("", notification.title)
        assertEquals("", notification.message)
        assertEquals("", notification.type)
        assertEquals(0L, notification.timestamp)
        assertFalse(notification.isRead)
        assertEquals("", notification.deviceId)
    }

    @Test
    fun notification_safetyAlert_hasCorrectFields() {
        val timestamp = System.currentTimeMillis()
        val notification = Notification(
            notificationId = "notif_001",
            title = "Safety Alert",
            message = "Smart Clothing Iron was automatically turned OFF after reaching its maximum active duration.",
            type = "SAFETY",
            timestamp = timestamp,
            isRead = false,
            deviceId = "iron01",
        )

        assertEquals("notif_001", notification.notificationId)
        assertEquals("Safety Alert", notification.title)
        assertTrue(notification.message.contains("maximum active duration"))
        assertEquals("SAFETY", notification.type)
        assertEquals(timestamp, notification.timestamp)
        assertFalse(notification.isRead)
        assertEquals("iron01", notification.deviceId)
    }

    @Test
    fun notification_scheduleExecuted_hasCorrectFields() {
        val timestamp = System.currentTimeMillis()
        val notification = Notification(
            notificationId = "notif_002",
            title = "Schedule Executed",
            message = "Living Light was turned ON by its scheduled automation.",
            type = "SCHEDULE",
            timestamp = timestamp,
            isRead = false,
            deviceId = "light01",
        )

        assertEquals("notif_002", notification.notificationId)
        assertEquals("Schedule Executed", notification.title)
        assertEquals("SCHEDULE", notification.type)
        assertEquals("light01", notification.deviceId)
        assertFalse(notification.isRead)
    }

    @Test
    fun notification_markAsRead_modifiesIsReadProperty() {
        val notification = Notification(
            notificationId = "notif_003",
            title = "System Update",
            message = "System running normally",
            type = "SYSTEM",
            timestamp = System.currentTimeMillis(),
            isRead = false,
        )

        assertFalse(notification.isRead)
        val readNotification = notification.copy(isRead = true)
        assertTrue(readNotification.isRead)
    }

    @Test
    fun notification_sorting_newestFirst() {
        val n1 = Notification(notificationId = "1", timestamp = 1000L)
        val n2 = Notification(notificationId = "2", timestamp = 3000L)
        val n3 = Notification(notificationId = "3", timestamp = 2000L)

        val list = listOf(n1, n2, n3).sortedByDescending { it.timestamp }

        assertEquals("2", list[0].notificationId)
        assertEquals("3", list[1].notificationId)
        assertEquals("1", list[2].notificationId)
    }
}
