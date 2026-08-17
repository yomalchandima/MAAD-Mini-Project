package com.example.maadminiproject

import com.example.maadminiproject.data.models.ActivityLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying the ActivityLog model and field requirements for Phase 11A.
 */
class ActivityLogModelTest {

    @Test
    fun activityLog_defaultValues_areCorrect() {
        val log = ActivityLog()
        assertEquals("", log.logId)
        assertEquals("", log.deviceId)
        assertEquals("", log.deviceName)
        assertEquals("", log.action)
        assertEquals("", log.description)
        assertEquals(0L, log.timestamp)
        assertEquals("", log.performedBy)
    }

    @Test
    fun activityLog_manualUserAction_hasCorrectFields() {
        val timestamp = System.currentTimeMillis()
        val log = ActivityLog(
            logId = "log_001",
            deviceId = "light01",
            deviceName = "Living Light",
            action = "DEVICE_TURNED_ON",
            description = "Living Light turned ON manually",
            timestamp = timestamp,
            performedBy = "User",
        )

        assertEquals("log_001", log.logId)
        assertEquals("light01", log.deviceId)
        assertEquals("Living Light", log.deviceName)
        assertEquals("DEVICE_TURNED_ON", log.action)
        assertEquals("Living Light turned ON manually", log.description)
        assertEquals(timestamp, log.timestamp)
        assertEquals("User", log.performedBy)
    }

    @Test
    fun activityLog_scheduleExecutorAction_hasCorrectFields() {
        val timestamp = System.currentTimeMillis()
        val log = ActivityLog(
            logId = "log_002",
            deviceId = "light01",
            deviceName = "Living Light",
            action = "SCHEDULE_TURN_ON",
            description = "Living Light turned ON by schedule 'schedule01'",
            timestamp = timestamp,
            performedBy = "schedule-executor",
        )

        assertEquals("log_002", log.logId)
        assertEquals("light01", log.deviceId)
        assertEquals("Living Light", log.deviceName)
        assertEquals("SCHEDULE_TURN_ON", log.action)
        assertEquals("schedule-executor", log.performedBy)
    }

    @Test
    fun activityLog_safetySystemAction_hasCorrectFields() {
        val timestamp = System.currentTimeMillis()
        val log = ActivityLog(
            logId = "log_003",
            deviceId = "iron01",
            deviceName = "Smart Clothing Iron",
            action = "SAFETY_SHUTDOWN",
            description = "Smart Clothing Iron automatically shut down by safety monitor after exceeding active limit",
            timestamp = timestamp,
            performedBy = "safety-system",
        )

        assertEquals("log_003", log.logId)
        assertEquals("iron01", log.deviceId)
        assertEquals("Smart Clothing Iron", log.deviceName)
        assertEquals("SAFETY_SHUTDOWN", log.action)
        assertEquals("safety-system", log.performedBy)
        assertTrue(log.description.contains("safety monitor"))
    }
}
