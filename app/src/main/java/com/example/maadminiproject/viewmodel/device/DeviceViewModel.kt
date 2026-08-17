package com.example.maadminiproject.viewmodel.device

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.maadminiproject.data.models.Device
import com.example.maadminiproject.data.repository.DeviceRepository
import com.google.firebase.database.ValueEventListener

/**
 * ViewModel for managing device states and operations in the presentation layer.
 *
 * DeviceViewModel is responsible for exposing device state to the Android UI and
 * requesting device operations through [DeviceRepository]. It maintains a real-time
 * list of devices and handles loading and error states.
 *
 * Architecture: UI -> DeviceViewModel -> DeviceRepository
 */
class DeviceViewModel : ViewModel() {

    /**
     * The repository used for all device-related operations.
     */
    private val repository = DeviceRepository()

    private val _devices = MutableLiveData<List<Device>>(emptyList())
    /**
     * Observable LiveData containing the list of devices for the currently observed zone.
     */
    val devices: LiveData<List<Device>> = _devices

    private val _isLoading = MutableLiveData(false)
    /**
     * Observable LiveData representing the loading state of device operations.
     */
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    /**
     * Observable LiveData containing the current error message, if any.
     */
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Holds the active Firebase listener to allow for cleanup when the ViewModel is cleared.
     */
    private var activeListener: ValueEventListener? = null

    /**
     * Identifiers for the currently observed zone, used for listener cleanup.
     */
    private var activeHomeId: String? = null
    private var activeFloorId: String? = null
    private var activeZoneId: String? = null

    /**
     * Starts observing devices for a specific zone within a home.
     *
     * This method sets up a real-time listener that updates the [devices] LiveData
     * whenever data changes in the Firebase Realtime Database.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     */
    fun observeDevices(homeId: String, floorId: String, zoneId: String) {
        // Remove existing listener before starting a new one
        removeActiveListener()

        _isLoading.value = true

        activeListener = repository.observeDevices(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            onDevicesChanged = { deviceList ->
                _devices.value = deviceList
                _isLoading.value = false
            },
            onFailure = { databaseError ->
                _errorMessage.value = "Database Error: ${databaseError.message}"
                _isLoading.value = false
            }
        )

        // Store identifiers for cleanup in onCleared
        activeHomeId = homeId
        activeFloorId = floorId
        activeZoneId = zoneId
    }

    /**
     * Toggles the state (ON/OFF) of a specific device.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device to toggle.
     * @param newState The new state to set (true for ON, false for OFF).
     */
    fun toggleDevice(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        newState: Boolean
    ) {
        val devName = _devices.value?.find { it.deviceId == deviceId }?.deviceName ?: ""
        repository.updateDeviceState(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            newState = newState,
            deviceName = devName,
            onFailure = { exception ->
                _errorMessage.value = "Failed to toggle device: ${exception.message}"
            }
        )
    }

    /**
     * Updates multiple fields of a specific device.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param updates A map containing the fields to update and their new values.
     */
    fun updateDeviceFields(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        updates: Map<String, Any>
    ) {
        repository.updateDeviceFields(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            updates = updates,
            onFailure = { exception ->
                _errorMessage.value = "Failed to update device: ${exception.message}"
            }
        )
    }

    /**
     * Sets the state of a specific switch channel within a multi-switch unit.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param switchId Identifier of the switch channel (e.g. "switch_1").
     * @param newState The new state for the switch channel.
     */
    fun setSwitchState(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        switchId: String,
        newState: Boolean,
    ) {
        val currentDevice = _devices.value?.find { it.deviceId == deviceId }
        val devName = currentDevice?.deviceName ?: ""
        repository.setSwitchState(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            switchId = switchId,
            newState = newState,
            currentSwitches = currentDevice?.switches,
            deviceName = devName,
            onFailure = { exception ->
                _errorMessage.value = "Failed to update switch $switchId: ${exception.message}"
            }
        )
    }

    /**
     * Toggles the state of a specific switch channel within a multi-switch unit.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param switchId Identifier of the switch channel (e.g. "switch_1").
     */
    fun toggleSwitchState(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        switchId: String,
    ) {
        val currentDevice = _devices.value?.find { it.deviceId == deviceId }
        val currentVal = currentDevice?.switches?.get(switchId) ?: false
        setSwitchState(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            switchId = switchId,
            newState = !currentVal,
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
     */
    fun setBrightness(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        brightness: Int,
    ) {
        val clampedBrightness = brightness.coerceIn(0, 100)
        repository.setBrightness(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            brightness = clampedBrightness,
            onFailure = { exception ->
                _errorMessage.value = "Failed to set brightness: ${exception.message}"
            }
        )
    }

    /**
     * Sets the fan speed setting.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param speed Fan speed level.
     */
    fun setFanSpeed(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        speed: Int,
    ) {
        repository.setFanSpeed(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            speed = speed,
            onFailure = { exception ->
                _errorMessage.value = "Failed to set fan speed: ${exception.message}"
            }
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
     */
    fun setTemperature(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        temperature: Int,
    ) {
        repository.setTemperature(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            temperature = temperature,
            onFailure = { exception ->
                _errorMessage.value = "Failed to set temperature: ${exception.message}"
            }
        )
    }

    /**
     * Sets the operating mode for an AC device.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneId Unique identifier of the zone.
     * @param deviceId Unique identifier of the device.
     * @param mode Operating mode (e.g. "Cool", "Heat", "Auto", "Dry", "Fan").
     */
    fun setMode(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        mode: String,
    ) {
        repository.setMode(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            mode = mode,
            onFailure = { exception ->
                _errorMessage.value = "Failed to set mode: ${exception.message}"
            }
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
     */
    fun setRecording(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        recording: Boolean,
    ) {
        repository.setRecording(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            recording = recording,
            onFailure = { exception ->
                _errorMessage.value = "Failed to set recording: ${exception.message}"
            }
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
     */
    fun setMaxActiveDuration(
        homeId: String,
        floorId: String,
        zoneId: String,
        deviceId: String,
        maxActiveDuration: Long,
    ) {
        repository.setMaxActiveDuration(
            homeId = homeId,
            floorId = floorId,
            zoneId = zoneId,
            deviceId = deviceId,
            maxActiveDuration = maxActiveDuration,
            onFailure = { exception ->
                _errorMessage.value = "Failed to set max active duration: ${exception.message}"
            }
        )
    }

    /**
     * Clears the current error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Internal helper to remove the active listener through the repository.
     */
    private fun removeActiveListener() {
        val listener = activeListener
        val hId = activeHomeId
        val fId = activeFloorId
        val zId = activeZoneId

        if (listener != null && hId != null && fId != null && zId != null) {
            repository.removeDevicesListener(hId, fId, zId, listener)
        }

        activeListener = null
        activeHomeId = null
        activeFloorId = null
        activeZoneId = null
    }

    /**
     * Cleans up resources when the ViewModel is no longer in use.
     * Removes the active Firebase listener to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        removeActiveListener()
    }
}
