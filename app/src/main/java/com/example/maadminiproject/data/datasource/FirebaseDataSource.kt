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
        onFailure: (DatabaseError) -> Unit
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
        onFailure: (DatabaseError) -> Unit
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
        onFailure: (DatabaseError) -> Unit
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
        onFailure: ((Exception) -> Unit)? = null
    ) {
        val deviceRef = getHomeReference(homeId)
            .child("floors").child(floorId)
            .child("zones").child(zoneId)
            .child("devices").child(deviceId)

        helper.updateData(deviceRef, updates, onSuccess, onFailure)
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
        onFailure: (DatabaseError) -> Unit
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
        onFailure: (DatabaseError) -> Unit
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
        onFailure: (DatabaseError) -> Unit
    ): ValueEventListener {
        return helper.observe(getHomeChildRef(homeId, "notifications"), onData, onFailure)
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
        onFailure: (DatabaseError) -> Unit
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
        onFailure: (DatabaseError) -> Unit
    ): ValueEventListener {
        return helper.observe(getHomeChildRef(homeId, "settings"), onData, onFailure)
    }

    /**
     * Removes a [ValueEventListener] from a [DatabaseReference].
     *
     * @param reference The [DatabaseReference] where the listener is attached.
     * @param listener The [ValueEventListener] to remove.
     */
    fun removeListener(
        reference: DatabaseReference,
        listener: ValueEventListener
    ) {
        helper.removeListener(reference, listener)
    }
}
