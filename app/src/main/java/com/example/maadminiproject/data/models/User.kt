package com.example.maadminiproject.data.models

/**
 * Data model representing a user of the Smart Home system.
 *
 * @property userId Unique identifier for the user (typically from Firebase Auth).
 * @property name Full name or display name of the user.
 * @property email Email address of the user.
 * @property role The role of the user within the home system (e.g., "Owner", "Member", "Guest").
 */
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Member",
)
