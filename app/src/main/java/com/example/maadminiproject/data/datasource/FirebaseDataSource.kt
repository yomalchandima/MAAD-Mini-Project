package com.example.maadminiproject.data.datasource

import com.example.maadminiproject.core.firebase.FirebaseManager
import com.example.maadminiproject.core.firebase.RealtimeDatabaseHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

/**
 * Data source responsible for interacting with the Smart Home Firebase Realtime Database.
 *
 * This class translates high-level smart home data requests into specific Firebase paths
 * and operations. It follows a strict hierarchy of homes, floors, zones, and devices.
 * It leverages [FirebaseManager] for database instance management and [RealtimeDatabaseHelper]
 * for generic database operations.
 */
@Suppress("unused")
class FirebaseDataSource {

    /**
     * Internal helper for performing generic database operations.
     */
    private val helper = RealtimeDatabaseHelper()

    /**
     * Returns a [DatabaseReference] for a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @return A reference to "homes/{homeId}".
     */
    fun getHomeReference(homeId: String): DatabaseReference {
        return FirebaseManager.database.getReference("homes").child(homeId)
    }

    /**
     * Helper to obtain a child reference under a specific home root.
     *
     * @param homeId The unique identifier of the home.
     * @param childPath The relative path of the child node.
     * @return A reference to "homes/{homeId}/{childPath}".
     */
    private fun getHomeChildRef(homeId: String, childPath: String): DatabaseReference {
        return getHomeReference(homeId).child(childPath)
    }

    /**
     * Observes metadata changes for a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param onData Callback invoked with the [DataSnapshot] upon success or change.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] created for this observation.
     */
    fun observeMetadata(
        homeId: String,
        onData: (DataSnapshot) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return helper.observe(getHomeChildRef(homeId, "metadata"), onData, onFailure)
    }

    /**
     * Observes floor structure changes for a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param onData Callback invoked with the [DataSnapshot] upon success or change.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] created for this observation.
     */
    fun observeFloors(
        homeId: String,
        onData: (DataSnapshot) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return helper.observe(getHomeChildRef(homeId, "floors"), onData, onFailure)
    }

    /**
     * Observes devices for a specific zone within a floor of a home.
     *
     * Path: homes/{homeId}/floors/{floorId}/zones/{zoneId}/devices
     *
     * @param homeId The unique identifier of the home.
     * @param floorId The unique identifier of the floor.
     * @param zoneId The unique identifier of the zone.
     * @param onData Callback invoked with the [DataSnapshot] upon success or change.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] created for this observation.
     */
    fun observeDevices(
        homeId: String,
        floorId: String,
        zoneId: String,
        onData: (DataSnapshot) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        val devicesRef = getHomeReference(homeId)
            .child("floors").child(floorId)
            .child("zones").child(zoneId)
            .child("devices")
        return helper.observe(devicesRef, onData, onFailure)
    }

    /**
     * Performs a partial update on a specific device within the nested hierarchy.
     *
     * Path: homes/{homeId}/floors/{floorId}/zones/{zoneId}/devices/{deviceId}
     *
     * @param homeId The unique identifier of the home.
     * @param floorId The unique identifier of the floor.
     * @param zoneId The unique identifier of the zone.
     * @param deviceId The unique identifier of the device.
     * @param updates A map of fields to update.
     * @param onSuccess Optional success callback.
     * @param onFailure Optional failure callback.
     */
    fun updateDevice(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        updates: Map<String, Any>,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val deviceRef = getHomeReference(homeId)
            .child("floors").child(floorId)
            .child("zones").child(zoneId)
            .child("devices").child(deviceId)

        helper.updateData(deviceRef, updates, onSuccess, onFailure)
    }

    /**
     * Updates an individual switch state within a multi-switch device.
     *
     * Path: homes/{homeId}/floors/{floorId}/zones/{zoneId}/devices/{deviceId}
     * Target fields: "switches/{switchId}", "state" (optional overall state), "lastUpdated"
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the multi-switch device.
     * @param switchId Identifier of the switch channel (e.g. "switch_1", "switch_2").
     * @param newState The new boolean state for the switch channel.
     * @param overallState Optional derived overall device state (true if at least one switch is ON).
     * @param onSuccess Optional success callback.
     * @param onFailure Optional failure callback.
     */
    fun updateDeviceSwitchState(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        switchId: String,
        newState: Boolean,
        overallState: Boolean? = null,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val updates = mutableMapOf<String, Any>(
            "switches/$switchId" to newState,
            "lastUpdated" to System.currentTimeMillis(),
        )
        if (overallState != null) {
            updates["state"] = overallState
        }
        updateDevice(homeId, floorId, zoneId, deviceId, updates, onSuccess, onFailure)
    }

    /**
     * Updates the brightness level for a light device.
     *
     * Path: homes/{homeId}/floors/{floorId}/zones/{zoneId}/devices/{deviceId}/brightness
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param brightness Brightness level (0-100).
     * @param onSuccess Optional success callback.
     * @param onFailure Optional failure callback.
     */
    fun updateDeviceBrightness(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        brightness: Int,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val updates = mapOf<String, Any>(
            "brightness" to brightness,
            "lastUpdated" to System.currentTimeMillis(),
        )
        updateDevice(homeId, floorId, zoneId, deviceId, updates, onSuccess, onFailure)
    }

    /**
     * Updates the fan speed setting.
     *
     * Path: homes/{homeId}/floors/{floorId}/zones/{zoneId}/devices/{deviceId}/speed
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param speed Speed level.
     * @param onSuccess Optional success callback.
     * @param onFailure Optional failure callback.
     */
    fun updateDeviceFanSpeed(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        speed: Int,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val updates = mapOf<String, Any>(
            "speed" to speed,
            "lastUpdated" to System.currentTimeMillis(),
        )
        updateDevice(homeId, floorId, zoneId, deviceId, updates, onSuccess, onFailure)
    }

    /**
     * Updates the temperature setting for an AC device.
     *
     * Path: homes/{homeId}/floors/{floorId}/zones/{zoneId}/devices/{deviceId}/temperature
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param temperature Temperature in degrees Celsius.
     * @param onSuccess Optional success callback.
     * @param onFailure Optional failure callback.
     */
    fun updateDeviceTemperature(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        temperature: Int,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val updates = mapOf<String, Any>(
            "temperature" to temperature,
            "lastUpdated" to System.currentTimeMillis(),
        )
        updateDevice(homeId, floorId, zoneId, deviceId, updates, onSuccess, onFailure)
    }

    /**
     * Updates the operating mode for an AC device.
     *
     * Path: homes/{homeId}/floors/{floorId}/zones/{zoneId}/devices/{deviceId}/mode
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param mode Operating mode string (e.g. "Cool", "Heat", "Auto", "Dry", "Fan").
     * @param onSuccess Optional success callback.
     * @param onFailure Optional failure callback.
     */
    fun updateDeviceMode(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        mode: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val updates = mapOf<String, Any>(
            "mode" to mode,
            "lastUpdated" to System.currentTimeMillis(),
        )
        updateDevice(homeId, floorId, zoneId, deviceId, updates, onSuccess, onFailure)
    }

    /**
     * Updates the camera recording state.
     *
     * Path: homes/{homeId}/floors/{floorId}/zones/{zoneId}/devices/{deviceId}/recording
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param recording Boolean flag indicating whether recording is active.
     * @param onSuccess Optional success callback.
     * @param onFailure Optional failure callback.
     */
    fun updateDeviceRecording(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        recording: Boolean,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val updates = mapOf<String, Any>(
            "recording" to recording,
            "lastUpdated" to System.currentTimeMillis(),
        )
        updateDevice(homeId, floorId, zoneId, deviceId, updates, onSuccess, onFailure)
    }

    /**
     * Updates the maximum active duration safety parameter for safety-critical appliances.
     *
     * Path: homes/{homeId}/floors/{floorId}/zones/{zoneId}/devices/{deviceId}/maxActiveDuration
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param maxActiveDuration Maximum active duration in minutes.
     * @param onSuccess Optional success callback.
     * @param onFailure Optional failure callback.
     */
    fun updateDeviceMaxActiveDuration(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        maxActiveDuration: Long,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val updates = mapOf<String, Any>(
            "maxActiveDuration" to maxActiveDuration,
            "lastUpdated" to System.currentTimeMillis(),
        )
        updateDevice(homeId, floorId, zoneId, deviceId, updates, onSuccess, onFailure)
    }

    /**
     * Observes schedules for a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param onData Callback invoked with the [DataSnapshot] upon success or change.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] created for this observation.
     */
    fun observeSchedules(
        homeId: String,
        onData: (DataSnapshot) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return helper.observe(getHomeChildRef(homeId, "schedules"), onData, onFailure)
    }

    /**
     * Observes reports for a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param onData Callback invoked with the [DataSnapshot] upon success or change.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] created for this observation.
     */
    fun observeReports(
        homeId: String,
        onData: (DataSnapshot) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return helper.observe(getHomeChildRef(homeId, "reports"), onData, onFailure)
    }

    /**
     * Observes notifications for a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param onData Callback invoked with the [DataSnapshot] upon success or change.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] created for this observation.
     */
    fun observeNotifications(
        homeId: String,
        onData: (DataSnapshot) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return helper.observe(getHomeChildRef(homeId, "notifications"), onData, onFailure)
    }

    /**
     * Performs a partial update on a specific notification.
     *
     * Path: homes/{homeId}/notifications/{notificationId}
     *
     * @param homeId The unique identifier of the home.
     * @param notificationId The unique identifier of the notification.
     * @param updates A map of fields to update.
     * @param onSuccess Optional success callback.
     * @param onFailure Optional failure callback.
     */
    fun updateNotification(
        homeId: String,
        notificationId: String,
        updates: Map<String, Any>,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val notificationRef = getHomeReference(homeId)
            .child("notifications").child(notificationId)

        helper.updateData(notificationRef, updates, onSuccess, onFailure)
    }

    /**
     * Observes activity logs for a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param onData Callback invoked with the [DataSnapshot] upon success or change.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] created for this observation.
     */
    fun observeActivityLogs(
        homeId: String,
        onData: (DataSnapshot) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return helper.observe(getHomeChildRef(homeId, "activityLogs"), onData, onFailure)
    }

    /**
     * Observes settings for a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param onData Callback invoked with the [DataSnapshot] upon success or change.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] created for this observation.
     */
    fun observeSettings(
        homeId: String,
        onData: (DataSnapshot) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return helper.observe(getHomeChildRef(homeId, "settings"), onData, onFailure)
    }

    /**
     * Removes a settings listener from a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param listener The listener to remove.
     */
    fun removeSettingsListener(
        homeId: String,
        listener: ValueEventListener,
    ) {
        val settingsRef = getHomeReference(homeId).child("settings")
        helper.removeListener(settingsRef, listener)
    }

    /**
     * Removes a report listener from a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param listener The listener to remove.
     */
    fun removeReportListener(
        homeId: String,
        listener: ValueEventListener,
    ) {
        val reportsRef = getHomeReference(homeId).child("reports")
        helper.removeListener(reportsRef, listener)
    }

    /**
     * Removes a notification listener from a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param listener The listener to remove.
     */
    fun removeNotificationListener(
        homeId: String,
        listener: ValueEventListener,
    ) {
        val notificationsRef = getHomeReference(homeId).child("notifications")
        helper.removeListener(notificationsRef, listener)
    }

    /**
     * Removes a schedule listener from a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param listener The listener to remove.
     */
    fun removeScheduleListener(
        homeId: String,
        listener: ValueEventListener,
    ) {
        val schedulesRef = getHomeReference(homeId).child("schedules")
        helper.removeListener(schedulesRef, listener)
    }

    /**
     * Removes a floor listener from a specific home.
     *
     * @param homeId The unique identifier of the home.
     * @param listener The listener to remove.
     */
    fun removeFloorListener(
        homeId: String,
        listener: ValueEventListener,
    ) {
        val floorsRef = getHomeReference(homeId).child("floors")
        helper.removeListener(floorsRef, listener)
    }

    /**
     * Removes a device listener from a specific zone.
     *
     * @param homeId The unique identifier of the home.
     * @param floorId The unique identifier of the floor.
     * @param zoneId The unique identifier of the zone.
     * @param listener The listener to remove.
     */
    fun removeDeviceListener(
        homeId: String,
        floorId: String,
        zoneId: String,
        listener: ValueEventListener,
    ) {
        val devicesRef = getHomeReference(homeId)
            .child("floors").child(floorId)
            .child("zones").child(zoneId)
            .child("devices")
        helper.removeListener(devicesRef, listener)
    }

    /**
     * Removes a [ValueEventListener] from a [DatabaseReference].
     *
     * @param reference The [DatabaseReference] where the listener is attached.
     * @param listener The [ValueEventListener] to remove.
     */
    fun removeListener(
        reference: DatabaseReference,
        listener: ValueEventListener,
    ) {
        helper.removeListener(reference, listener)
    }
}
