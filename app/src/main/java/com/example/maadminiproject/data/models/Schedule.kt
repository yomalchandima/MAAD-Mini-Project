package com.example.maadminiproject.data.models

/**
 * Data model representing a scheduled automation for a smart device.
 *
 * @property scheduleId Unique identifier for the schedule.
 * @property deviceId The ID of the device this schedule applies to.
 * @property deviceName The name of the device for easy identification.
 * @property action The action to perform (e.g., "Turn On", "Turn Off", "Set Temperature").
 * @property startTime The time at which the action should start (e.g., "08:00").
 * @property endTime The time at which the action should end (e.g., "17:00"), or null for point-in-time actions.
 * @property repeat How often the schedule repeats (e.g., "Daily", "None", "Mon,Wed,Fri").
 * @property enabled Whether the schedule is currently active.
 * @property switchId Optional identifier for a specific switch in a multi-switch unit.
 * @property createdAt The timestamp when the schedule was created.
 */
data class Schedule(
    val scheduleId: String = "",
    val deviceId: String = "",
    val deviceName: String = "",
    val action: String = "",
    val startTime: String = "",
    val endTime: String? = null,
    val repeat: String = "",
    val enabled: Boolean = true,
    val switchId: String? = null,
    val createdAt: Long = 0L,
)
