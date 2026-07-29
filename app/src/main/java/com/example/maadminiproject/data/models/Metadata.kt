package com.example.maadminiproject.data.models

/**
 * Data model representing general information about a Smart Home.
 *
 * @property homeId Unique identifier for the smart home.
 * @property homeName User-friendly name of the smart home.
 * @property owner Name or ID of the home owner.
 * @property address Physical address of the smart home.
 * @property createdAt ISO 8601 formatted string or descriptive date of creation.
 * @property status Current operational status of the home (e.g., "Active", "Inactive").
 */
data class Metadata(
    val homeId: String = "",
    val homeName: String = "",
    val owner: String = "",
    val address: String = "",
    val createdAt: String = "",
    val status: String = "",
)
