package com.example.maadminiproject.data.models

/**
 * Data model representing a smart device in the smart house.
 *
 * This model is designed to be compatible with the Firebase Realtime Database structure.
 * It supports standard devices, multi-switch units, and safety-critical appliances.
 *
 * @property deviceId Unique identifier for the device.
 * @property deviceName User-friendly name of the device.
 * @property type Category of the device (e.g., Light, AC, Fan, Camera).
 * @property floorId The ID of the floor where the device is located.
 * @property zoneId The ID of the zone (room) where the device is located.
 * @property state Indicates whether the device is currently powered on.
 * @property status Current status message or state of the device.
 * @property power Current power consumption of the device.
 * @property unit Unit of measurement for power usage (e.g., Watts).
 * @property icon Resource name or identifier for the device's icon.
 * @property online Indicates if the device is currently connected to the network.
 * @property x The X-coordinate position of the device in the UI layout.
 * @property y The Y-coordinate position of the device in the UI layout.
 * @property lastUpdated Timestamp of the last status update.
 * @property brightness Optional brightness level for lights (0-100).
 * @property speed Optional speed setting for fans.
 * @property temperature Optional temperature setting for AC or heating devices.
 * @property mode Optional operating mode (e.g., "Cool", "Heat", "Auto").
 * @property recording Optional flag for cameras indicating if they are currently recording.
 * @property switchCount Number of individually addressable switches (for multi-switch units).
 * @property switches Map of switch identifiers to their respective ON/OFF states.
 * @property maxActiveDuration Maximum permissible active duration in minutes (for safety-critical devices).
 */
data class Device(
    val deviceId: String = "",
    val deviceName: String = "",
    val type: String = "",
    val floorId: String = "",
    val zoneId: String = "",
    val state: Boolean = false,
    val status: String = "",
    val power: Double = 0.0,
    val unit: String = "",
    val icon: String = "",
    val online: Boolean = false,
    val x: Float = 0f,
    val y: Float = 0f,
    val lastUpdated: Long = 0L,
    val brightness: Int? = null,
    val speed: Int? = null,
    val temperature: Int? = null,
    val mode: String? = null,
    val recording: Boolean? = null,
    val switchCount: Int = 0,
    val switches: Map<String, Boolean>? = null,
    val maxActiveDuration: Long? = null,
)
