package com.example.maadminiproject.viewmodel.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.maadminiproject.data.models.Device
import com.example.maadminiproject.data.models.Floor
import com.example.maadminiproject.data.models.Notification
import com.example.maadminiproject.data.models.Report
import com.example.maadminiproject.data.repository.DeviceRepository
import com.example.maadminiproject.data.repository.FloorRepository
import com.example.maadminiproject.data.repository.NotificationRepository
import com.example.maadminiproject.data.repository.ReportRepository
import com.google.firebase.database.ValueEventListener

/**
 * ViewModel responsible for aggregating and exposing smart home dashboard data.
 *
 * It combines data from multiple repositories to provide a unified view of floors,
 * notifications, energy reports, and device status summaries for the smart house.
 *
 * Architecture: UI -> DashboardViewModel -> Repositories
 */
class DashboardViewModel : ViewModel() {

    private val deviceRepository = DeviceRepository()
    private val floorRepository = FloorRepository()
    private val notificationRepository = NotificationRepository()
    private val reportRepository = ReportRepository()

    // Dashboard State
    private val _floors = MutableLiveData<List<Floor>>(emptyList())
    /**
     * Observable LiveData containing the list of floors in the home.
     */
    val floors: LiveData<List<Floor>> = _floors

    private val _notifications = MutableLiveData<List<Notification>>(emptyList())
    /**
     * Observable LiveData containing recent notifications.
     */
    val notifications: LiveData<List<Notification>> = _notifications

    private val _report = MutableLiveData<Report?>(null)
    /**
     * Observable LiveData containing the daily energy and usage report.
     */
    val report: LiveData<Report?> = _report

    private val _isLoading = MutableLiveData(false)
    /**
     * Observable LiveData representing the global loading state of the dashboard.
     */
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    /**
     * Observable LiveData containing the current error message, if any.
     */
    val errorMessage: LiveData<String?> = _errorMessage

    // Device Summary State
    private val _totalDevices = MutableLiveData(0)
    /**
     * Total number of devices across all observed zones.
     */
    val totalDevices: LiveData<Int> = _totalDevices

    private val _activeDevices = MutableLiveData(0)
    /**
     * Total number of currently active (turned on) devices across all observed zones.
     */
    val activeDevices: LiveData<Int> = _activeDevices

    // Listener and ID Tracking for Cleanup
    private var floorListener: ValueEventListener? = null
    private var notificationListener: ValueEventListener? = null
    private var reportListener: ValueEventListener? = null
    private val deviceListeners = mutableMapOf<String, ValueEventListener>()

    private var activeHomeId: String? = null
    private var activeFloorId: String? = null

    // Internal data storage for summary calculation
    private val devicesByZone = mutableMapOf<String, List<Device>>()

    // Tracking initial loads to toggle isLoading
    private val pendingInitialObservations = mutableSetOf<String>()

    /**
     * Starts observing all dashboard-related data for a specific home and floor.
     *
     * @param homeId Unique identifier of the home.
     * @param floorId Unique identifier of the floor.
     * @param zoneIds List of zone identifiers to observe for device summaries.
     */
    fun observeDashboard(homeId: String, floorId: String, zoneIds: List<String>) {
        // Stop any existing observations
        cleanup()

        _isLoading.value = true
        activeHomeId = homeId
        activeFloorId = floorId

        // Prepare observation tracking keys
        pendingInitialObservations.clear()
        pendingInitialObservations.add("floors")
        pendingInitialObservations.add("notifications")
        pendingInitialObservations.add("report")
        zoneIds.forEach { pendingInitialObservations.add("devices_$it") }

        // 1. Observe Floors
        floorListener = floorRepository.observeFloors(
            homeId = homeId,
            onFloorsChanged = { floorList ->
                _floors.value = floorList
                markObservationComplete("floors")
            },
            onFailure = { handleError(it.message) },
        )

        // 2. Observe Notifications
        notificationListener = notificationRepository.observeNotifications(
            homeId = homeId,
            onNotificationsChanged = { notificationList ->
                _notifications.value = notificationList
                markObservationComplete("notifications")
            },
            onFailure = { handleError(it.message) },
        )

        // 3. Observe Daily Report
        reportListener = reportRepository.observeReports(
            homeId = homeId,
            onReportChanged = { reportData ->
                _report.value = reportData
                markObservationComplete("report")
            },
            onFailure = { handleError(it.message) },
        )

        // 4. Observe Devices for each requested zone
        zoneIds.forEach { zoneId ->
            val listener = deviceRepository.observeDevices(
                homeId = homeId,
                floorId = floorId,
                zoneId = zoneId,
                onDevicesChanged = { deviceList ->
                    devicesByZone[zoneId] = deviceList
                    updateDeviceSummary()
                    markObservationComplete("devices_$zoneId")
                },
                onFailure = { handleError(it.message) },
            )
            deviceListeners[zoneId] = listener
        }
    }

    /**
     * Recalculates total and active device counts from the aggregated device data.
     */
    private fun updateDeviceSummary() {
        val allDevices = devicesByZone.values.flatten()
        _totalDevices.value = allDevices.size
        // Using 'state' property from Device model to determine active status
        _activeDevices.value = allDevices.count { it.state }
    }

    /**
     * Marks an initial observation as complete and stops the loading indicator if all are finished.
     *
     * @param key Unique key for the observation type.
     */
    private fun markObservationComplete(key: String) {
        pendingInitialObservations.remove(key)
        if (pendingInitialObservations.isEmpty()) {
            _isLoading.value = false
        }
    }

    /**
     * Handles repository errors by exposing the message and stopping the loading indicator.
     *
     * @param message The error message to display.
     */
    private fun handleError(message: String?) {
        _errorMessage.value = "Database Error: ${message ?: "Unknown error"}"
        _isLoading.value = false
    }

    /**
     * Clears the current dashboard error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Detaches all active Firebase listeners and resets tracking state.
     */
    private fun cleanup() {
        val hId = activeHomeId
        val fId = activeFloorId

        if (hId != null) {
            // Cleanup Floors
            floorListener?.let { floorRepository.removeFloorsListener(hId, it) }

            // Cleanup Notifications
            notificationListener?.let { notificationRepository.removeNotificationsListener(hId, it) }

            // Cleanup Report
            reportListener?.let { reportRepository.removeReportsListener(hId, it) }

            // Cleanup Devices for all observed zones
            if (fId != null) {
                deviceListeners.forEach { (zoneId, listener) ->
                    deviceRepository.removeDevicesListener(hId, fId, zoneId, listener)
                }
            }
        }

        // Reset state
        floorListener = null
        notificationListener = null
        reportListener = null
        deviceListeners.clear()
        devicesByZone.clear()
        pendingInitialObservations.clear()

        activeHomeId = null
        activeFloorId = null
    }

    /**
     * Lifecycle callback to ensure all real-time listeners are removed.
     */
    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}
