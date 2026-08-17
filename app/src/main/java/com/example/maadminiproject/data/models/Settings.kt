package com.example.maadminiproject.data.models

/**
 * Data model representing application and home settings.
 *
 * @property darkMode Whether the application should use dark mode.
 * @property notificationsEnabled Whether system notifications are enabled.
 * @property language Preferred language for the application (e.g., "en", "es").
 * @property temperatureUnit Preferred temperature unit (e.g., "Celsius", "Fahrenheit").
 */
data class Settings(
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val language: String = "en",
    val temperatureUnit: String = "Celsius",
)
