package com.example.maadminiproject.viewmodel.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.maadminiproject.data.models.ActivityLog
import com.example.maadminiproject.data.models.Device
import com.example.maadminiproject.data.models.Floor
import com.example.maadminiproject.data.models.Report
import com.example.maadminiproject.data.repository.ActivityLogRepository
import com.example.maadminiproject.data.repository.FloorRepository
import com.example.maadminiproject.data.repository.ReportRepository
import com.google.firebase.database.ValueEventListener

/**
 * ViewModel for managing live reporting, device load calculations, and activity logs.
 *
 * Architecture: UI -> ReportViewModel -> [ReportRepository, FloorRepository, ActivityLogRepository]
 */
class ReportViewModel : ViewModel() {

    private val reportRepository = ReportRepository()
    private val floorRepository = FloorRepository()
    private val activityLogRepository = ActivityLogRepository()

    private val _report = MutableLiveData<Report?>(null)
    val report: LiveData<Report?> = _report

    private val _devices = MutableLiveData<List<Device>>(emptyList())
    val devices: LiveData<List<Device>> = _devices

    private val _totalDevicesCount = MutableLiveData(0)
    val totalDevicesCount: LiveData<Int> = _totalDevicesCount

    private val _activeDevicesCount = MutableLiveData(0)
    val activeDevicesCount: LiveData<Int> = _activeDevicesCount

    private val _onlineDevicesCount = MutableLiveData(0)
    val onlineDevicesCount: LiveData<Int> = _onlineDevicesCount

    private val _offlineDevicesCount = MutableLiveData(0)
    val offlineDevicesCount: LiveData<Int> = _offlineDevicesCount

    private val _totalActivePowerWatts = MutableLiveData(0.0)
    val totalActivePowerWatts: LiveData<Double> = _totalActivePowerWatts

    private val _topConsumers = MutableLiveData<List<Device>>(emptyList())
    val topConsumers: LiveData<List<Device>> = _topConsumers

    private val _categoryLoads = MutableLiveData<Map<String, Double>>(emptyMap())
    val categoryLoads: LiveData<Map<String, Double>> = _categoryLoads

    private val _activityLogs = MutableLiveData<List<ActivityLog>>(emptyList())
    val activityLogs: LiveData<List<ActivityLog>> = _activityLogs

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private var reportListener: ValueEventListener? = null
    private var floorListener: ValueEventListener? = null
    private var activityLogListener: ValueEventListener? = null
    private var activeHomeId: String? = null

    /**
     * Starts observing live reports, device status, and activity logs.
     *
     * @param homeId Unique identifier of the home.
     */
    fun observeReport(homeId: String) {
        removeActiveListeners()
        activeHomeId = homeId
        _isLoading.value = true

        // 1. Observe Firebase reports node
        reportListener = reportRepository.observeReports(
            homeId = homeId,
            onReportChanged = { reportData ->
                if (reportData != null) {
                    _report.value = reportData
                }
            },
            onFailure = { error ->
                _errorMessage.value = "Reports Database Error: ${error.message}"
            }
        )

        // 2. Observe Floors / Devices node for live counts and active load
        floorListener = floorRepository.observeFloors(
            homeId = homeId,
            onFloorsChanged = { floors ->
                processFloorsData(floors)
                _isLoading.value = false
            },
            onFailure = { error ->
                _errorMessage.value = "Floors Database Error: ${error.message}"
                _isLoading.value = false
            }
        )

        // 3. Observe Activity Logs for recent activity timeline
        activityLogListener = activityLogRepository.observeActivityLogs(
            homeId = homeId,
            onLogsChanged = { logs ->
                _activityLogs.value = logs
            },
            onFailure = { error ->
                _errorMessage.value = "ActivityLogs Error: ${error.message}"
            }
        )
    }

    private fun processFloorsData(floors: List<Floor>) {
        val allDevices = floors.flatMap { floor ->
            floor.zones.values.flatMap { zone ->
                zone.devices.values
            }
        }

        _devices.value = allDevices

        val total = allDevices.size
        val active = allDevices.count { it.state }
        val online = allDevices.count { it.online || !it.status.equals("Offline", ignoreCase = true) }
        val offline = allDevices.count { !it.online && it.status.equals("Offline", ignoreCase = true) }

        _totalDevicesCount.value = total
        _activeDevicesCount.value = active
        _onlineDevicesCount.value = online
        _offlineDevicesCount.value = offline

        // Calculate live active power consumption
        val activeDevicesList = allDevices.filter { it.state }
        val totalActiveWatts = activeDevicesList.sumOf { it.power }
        _totalActivePowerWatts.value = totalActiveWatts

        // Top consuming devices (all devices with power rating, prioritized by active state and wattage)
        val sortedConsumers = allDevices.filter { it.power > 0 }
            .sortedWith(compareByDescending<Device> { it.state }.thenByDescending { it.power })
        _topConsumers.value = sortedConsumers

        // Category Load breakdown
        val catMap = mutableMapOf<String, Double>()
        activeDevicesList.forEach { dev ->
            val category = getCategoryForDevice(dev)
            catMap[category] = (catMap[category] ?: 0.0) + dev.power
        }
        _categoryLoads.value = catMap

        // Update synthetic report model if base report wasn't provided or to keep counts live
        val currentReport = _report.value
        _report.value = Report(
            energyUsage = if (currentReport?.energyUsage != null && currentReport.energyUsage > 0.0) currentReport.energyUsage else totalActiveWatts,
            activeDevices = active,
            onlineDevices = online,
            offlineDevices = offline,
            totalDevices = total,
        )
    }

    private fun getCategoryForDevice(device: Device): String {
        val type = device.type.lowercase()
        val name = device.deviceName.lowercase()
        return when {
            type.contains("ac") || type.contains("fan") || type.contains("heat") || type.contains("climate") || name.contains("ac") || name.contains("fan") -> "Climate"
            type.contains("iron") || type.contains("plug") || type.contains("oven") || type.contains("kitchen") || name.contains("iron") || name.contains("plug") || name.contains("oven") || name.contains("fridge") -> "Kitchen"
            type.contains("light") || name.contains("light") || name.contains("lamp") -> "Lights"
            else -> "Other"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun removeActiveListeners() {
        val homeId = activeHomeId ?: return
        reportListener?.let { reportRepository.removeReportsListener(homeId, it) }
        floorListener?.let { floorRepository.removeFloorsListener(homeId, it) }
        activityLogListener?.let { activityLogRepository.removeActivityLogsListener(homeId, it) }

        reportListener = null
        floorListener = null
        activityLogListener = null
        activeHomeId = null
    }

    override fun onCleared() {
        super.onCleared()
        removeActiveListeners()
    }
}
