package com.example.maadminiproject.data.models

/**
 * Data model representing a room or zone in the smart house.
 *
 * @property zoneId Unique identifier for the zone.
 * @property zoneName User-friendly name of the zone (e.g., "Living Room", "Kitchen").
 * @property devices A map of devices located within this zone, keyed by their [Device.deviceId].
 */
data class Zone(
    val zoneId: String = "",
    val zoneName: String = "",
    val devices: MutableMap<String, Device> = mutableMapOf(),
)
