package com.example.maadminiproject.data.models

/**
 * Data model representing an energy and device usage report.
 *
 * @property energyUsage Total energy consumption recorded in this report.
 * @property activeDevices Number of devices currently active or turned on.
 * @property onlineDevices Number of devices currently connected to the network.
 * @property offlineDevices Number of devices currently disconnected from the network.
 */
data class Report(
    val energyUsage: Double = 0.0,
    val activeDevices: Int = 0,
    val onlineDevices: Int = 0,
    val offlineDevices: Int = 0,
    val totalDevices: Int = 0,
)
