package com.example.maadminiproject.data.models

/**
 * Data model representing a reusable smart device template.
 *
 * @property icon Resource name or identifier for the device's icon.
 * @property defaultPower Default power consumption for the device type.
 */
data class DeviceTemplate(
    val icon: String = "",
    val defaultPower: Double = 0.0,
)
