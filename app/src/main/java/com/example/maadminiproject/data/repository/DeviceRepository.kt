package com.example.maadminiproject.data.repository

import com.example.maadminiproject.data.datasource.FirebaseDataSource
import com.example.maadminiproject.data.models.Device
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

/**
 * Repository providing all device-related operations for the Smart Home application.
 *
 * This repository sits between the ViewModels and [FirebaseDataSource], abstracting
 * Firebase implementation details and working directly with [Device] models.
 */
class DeviceRepository {

    /**
     * Internal data source for Firebase operations.
     */
    private val dataSource = FirebaseDataSource()

    /**
     * Observes devices for a specific zone within a floor of a home.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param onDevicesChanged Callback invoked with a list of [Device] objects when data changes.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] attached to the database reference.
     */
    fun observeDevices(
        homeId: String,
        floorId: String,
        zoneId: String,
        onDevicesChanged: (List<Device>) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return dataSource.observeDevices(
            homeId,
            floorId,
            zoneId,
            onData = { snapshot ->
                onDevicesChanged(mapDevices(snapshot))
            },
            onFailure = onFailure,
        )
    }

    /**
     * Updates the state of a specific device.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param newState The new boolean state for the device.
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun updateDeviceState(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        newState: Boolean,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val updates = mapOf("state" to newState)
        dataSource.updateDevice(
            homeId,
            floorId,
            zoneId,
            deviceId,
            updates,
            onSuccess,
            onFailure
        )
    }

    /**
     * Updates multiple fields of a specific device.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param updates A map containing field names and their new values.
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun updateDeviceFields(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        updates: Map<String, Any>,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        dataSource.updateDevice(
            homeId,
            floorId,
            zoneId,
            deviceId,
            updates,
            onSuccess,
            onFailure
        )
    }

    /**
     * Removes an active device listener.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param listener The listener to be removed.
     */
    fun removeDevicesListener(
        homeId: String,
        floorId: String,
        zoneId: String,
        listener: ValueEventListener
    ) {
        dataSource.removeDeviceListener(homeId, floorId, zoneId, listener)
    }

    /**
     * Removes a listener from a database reference.
     *
     * @param reference The database reference to detach the listener from.
     * @param listener The listener to be removed.
     */
    fun removeListener(
        reference: DatabaseReference,
        listener: ValueEventListener,
    ) {
        dataSource.removeListener(reference, listener)
    }

    /**
     * Helper function to map a [DataSnapshot] to a list of [Device] models.
     *
     * @param snapshot The snapshot containing device children.
     * @return A list of [Device] objects, excluding any null results.
     */
    private fun mapDevices(snapshot: DataSnapshot): List<Device> {
        return snapshot.children.mapNotNull { child ->
            child.getValue(Device::class.java)
        }
    }
}
