package com.example.maadminiproject.data.models

/**
 * Data model representing a smart device in the smart house.
 *
 * @property deviceId Unique identifier for the device.
 * @property deviceName User-friendly name of the device.
 * @property deviceType Category of the device (e.g., Light, AC, Fan, Camera).
 * @property floorId The ID of the floor where the device is located.
 * @property zoneId The ID of the zone (room) where the device is located.
 * @property isOn Indicates whether the device is currently powered on.
 * @property status Current status message or state of the device.
 * @property powerUsage Current power consumption of the device.
 * @property unit Unit of measurement for power usage (e.g., Watts).
 * @property icon Resource name or identifier for the device's icon.
 * @property online Indicates if the device is currently connected to the network.
 * @property lastUpdated Timestamp of the last status update.
 * @property brightness Optional brightness level for lights (0-100).
 * @property fanSpeed Optional speed setting for fans.
 * @property temperature Optional temperature setting for AC or heating devices.
 * @property mode Optional operating mode (e.g., "Cool", "Heat", "Auto").
 * @property recording Optional flag for cameras indicating if they are currently recording.
 */
data class Device(
    val deviceId: String = "",
    val deviceName: String = "",
    val deviceType: String = "",
    val floorId: String = "",
    val zoneId: String = "",
    val isOn: Boolean = false,
    val status: String = "",
    val powerUsage: Double = 0.0,
    val unit: String = "",
    val icon: String = "",
    val online: Boolean = false,
    val lastUpdated: Long = 0L,
    val brightness: Int? = null,
    val fanSpeed: Int? = null,
    val temperature: Int? = null,
    val mode: String? = null,
    val recording: Boolean? = null,
)
