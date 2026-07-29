package com.example.maadminiproject.data.models

/**
 * Data model representing the root Smart Home object.
 * This is the top-level entity stored in Firebase Realtime Database.
 *
 * @property homeId Unique identifier for the smart home.
 * @property metadata Basic information about the home (e.g., owner, address).
 * @property floors Map of floors in the home, keyed by their ID.
 * @property reports Map of reports generated for the home.
 * @property settings Global settings for the smart home.
 * @property schedules Map of scheduled tasks or automations.
 * @property notifications Map of system notifications.
 * @property activityLogs Map of activity logs recorded by the system.
 */
data class Home(
    val homeId: String = "",
    val metadata: Metadata = Metadata(),
    val floors: MutableMap<String, Floor> = mutableMapOf(),
    val reports: MutableMap<String, Report> = mutableMapOf(),
    val settings: Settings = Settings(),
    val schedules: MutableMap<String, Any> = mutableMapOf(),
    val notifications: MutableMap<String, Any> = mutableMapOf(),
    val activityLogs: MutableMap<String, Any> = mutableMapOf(),
)

/**
 * Data model for home reports.
 * Implementation to be expanded later.
 */
data class Report(
    val reportId: String = "",
    val type: String = "",
)
