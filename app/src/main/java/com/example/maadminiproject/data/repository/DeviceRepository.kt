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
     * Sets the state of an individual switch channel within a multi-switch device.
     *
     * Computes the overall device state (true if at least one switch is ON, false if all OFF)
     * when [currentSwitches] is provided.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the multi-switch device.
     * @param switchId Identifier of the switch channel (e.g. "switch_1").
     * @param newState New boolean state for the switch channel.
     * @param currentSwitches Current map of switches on the device, if known.
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun setSwitchState(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        switchId: String,
        newState: Boolean,
        currentSwitches: Map<String, Boolean>? = null,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val overallState = currentSwitches?.let { map ->
            val updated = map.toMutableMap().apply { put(switchId, newState) }
            updated.values.any { it }
        } ?: if (newState) true else null

        dataSource.updateDeviceSwitchState(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            switchId = switchId,
            newState = newState,
            overallState = overallState,
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

    /**
     * Sets the brightness level for a light device.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param brightness Brightness level (0-100).
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun setBrightness(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        brightness: Int,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        dataSource.updateDeviceBrightness(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            brightness = brightness,
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

    /**
     * Sets the fan speed setting.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param speed Speed level.
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun setFanSpeed(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        speed: Int,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        dataSource.updateDeviceFanSpeed(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            speed = speed,
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

    /**
     * Sets the temperature setting for an AC device.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param temperature Temperature in degrees Celsius.
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun setTemperature(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        temperature: Int,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        dataSource.updateDeviceTemperature(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            temperature = temperature,
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

    /**
     * Sets the operating mode for an AC device.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param mode Operating mode string (e.g. "Cool", "Heat", "Auto", "Dry", "Fan").
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun setMode(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        mode: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        dataSource.updateDeviceMode(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            mode = mode,
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

    /**
     * Sets the camera recording state.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param recording Boolean flag indicating whether recording is active.
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun setRecording(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        recording: Boolean,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        dataSource.updateDeviceRecording(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            recording = recording,
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

    /**
     * Sets the maximum active duration safety parameter for safety-critical appliances.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param maxActiveDuration Maximum active duration in minutes.
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun setMaxActiveDuration(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        maxActiveDuration: Long,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        dataSource.updateDeviceMaxActiveDuration(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            maxActiveDuration = maxActiveDuration,
            onSuccess = onSuccess,
            onFailure = onFailure,
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
