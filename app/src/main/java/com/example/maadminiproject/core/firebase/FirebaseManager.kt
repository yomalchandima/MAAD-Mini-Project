package com.example.maadminiproject.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * Singleton manager for Firebase services used in the Smart Home application.
 *
 * This class serves as the single entry point for Firebase Authentication and Realtime Database.
 * It provides centralized access to the root database reference and specific child nodes
 * within the smart home hierarchy.
 */
@Suppress("unused")
object FirebaseManager {

    /**
     * The [FirebaseAuth] instance used for application-wide authentication operations.
     */
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * The [FirebaseDatabase] instance used for Realtime Database operations.
     */
    val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://maad-mini-project-4b71e-default-rtdb.asia-southeast1.firebasedatabase.app")

    /**
     * The root [DatabaseReference] of the Realtime Database.
     */
    val rootReference: DatabaseReference = database.reference

    /**
     * Returns a [DatabaseReference] for the "metadata" node.
     * Use this for general application metadata.
     */
    fun metadataRef(): DatabaseReference = rootReference.child("metadata")

    /**
     * Returns a [DatabaseReference] for the "settings" node.
     * Use this for application or user-specific settings.
     */
    fun settingsRef(): DatabaseReference = rootReference.child("settings")

    /**
     * Returns a [DatabaseReference] for the "floors" node.
     * Use this for floor layouts and structural data.
     */
    fun floorsRef(): DatabaseReference = rootReference.child("floors")

    /**
     * Returns a [DatabaseReference] for the "devices" node.
     * Use this for IoT device status and control.
     */
    fun devicesRef(): DatabaseReference = rootReference.child("devices")

    /**
     * Returns a [DatabaseReference] for the "schedules" node.
     * Use this for automation and device schedules.
     */
    fun schedulesRef(): DatabaseReference = rootReference.child("schedules")

    /**
     * Returns a [DatabaseReference] for the "reports" node.
     * Use this for data analytics and usage reports.
     */
    fun reportsRef(): DatabaseReference = rootReference.child("reports")

    /**
     * Returns a [DatabaseReference] for the "notifications" node.
     * Use this for system and user alerts.
     */
    fun notificationsRef(): DatabaseReference = rootReference.child("notifications")

    /**
     * Returns a [DatabaseReference] for the "activityLogs" node.
     * Use this for historical event logging.
     */
    fun activityLogsRef(): DatabaseReference = rootReference.child("activityLogs")

    /**
     * Returns a [DatabaseReference] for the "users" node.
     * Use this for user profiles and account-related data.
     */
    fun usersRef(): DatabaseReference = rootReference.child("users")
}
