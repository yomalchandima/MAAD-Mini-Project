package com.example.maadminiproject.data.repository

import com.example.maadminiproject.data.datasource.FirebaseDataSource
import com.example.maadminiproject.data.models.ActivityLog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

/**
 * Repository providing all activity-log-related operations for the Smart Home application.
 *
 * This repository interfaces between ViewModels / device managers and [FirebaseDataSource],
 * enabling recording and observation of system and user events.
 */
class ActivityLogRepository {

    /**
     * Internal data source for Firebase operations.
     */
    private val dataSource = FirebaseDataSource()

    /**
     * Observes activity logs for a specific home.
     *
     * @param homeId Unique identifier of the home.
     * @param onLogsChanged Callback invoked with the list of [ActivityLog] entries ordered chronologically.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] attached to the database reference.
     */
    fun observeActivityLogs(
        homeId: String,
        onLogsChanged: (List<ActivityLog>) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return dataSource.observeActivityLogs(
            homeId = homeId,
            onData = { snapshot ->
                onLogsChanged(mapActivityLogs(snapshot))
            },
            onFailure = onFailure,
        )
    }

    /**
     * Records a new activity log entry in Firebase.
     *
     * @param homeId Unique identifier of the home.
     * @param activityLog The [ActivityLog] to record.
     * @param onSuccess Optional callback returning the generated log ID upon success.
     * @param onFailure Optional callback invoked with an [Exception] upon failure.
     */
    fun logActivity(
        homeId: String,
        activityLog: ActivityLog,
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        dataSource.createActivityLog(
            homeId = homeId,
            activityLog = activityLog,
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

    /**
     * Helper to quickly log a device action.
     *
     * @param homeId Unique identifier of the home.
     * @param deviceId Identifier of the target device.
     * @param deviceName Display name of the target device.
     * @param action Action performed (e.g. "DEVICE_TURNED_ON", "DEVICE_TURNED_OFF").
     * @param description Human-readable description of the event.
     * @param performedBy Actor who initiated the action (e.g. "User", "safety-system", "schedule-executor").
     * @param onSuccess Optional callback upon success.
     * @param onFailure Optional callback upon failure.
     */
    fun logDeviceAction(
        homeId: String,
        deviceId: String,
        deviceName: String,
        action: String,
        description: String,
        performedBy: String = "User",
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val log = ActivityLog(
            deviceId = deviceId,
            deviceName = deviceName.ifBlank { deviceId },
            action = action,
            description = description,
            timestamp = System.currentTimeMillis(),
            performedBy = performedBy,
        )
        logActivity(homeId, log, onSuccess, onFailure)
    }

    /**
     * Removes an active activity logs listener.
     *
     * @param homeId Unique identifier of the home.
     * @param listener The listener to remove.
     */
    fun removeActivityLogsListener(
        homeId: String,
        listener: ValueEventListener,
    ) {
        dataSource.removeActivityLogsListener(homeId, listener)
    }

    /**
     * Removes a listener from a database reference.
     *
     * @param reference The database reference to detach the listener from.
     * @param listener The listener to remove.
     */
    fun removeListener(
        reference: DatabaseReference,
        listener: ValueEventListener,
    ) {
        dataSource.removeListener(reference, listener)
    }

    /**
     * Helper function to map a [DataSnapshot] to a sorted list of [ActivityLog] models.
     *
     * @param snapshot The snapshot containing activity log entries.
     * @return List of [ActivityLog] objects sorted newest first.
     */
    private fun mapActivityLogs(snapshot: DataSnapshot): List<ActivityLog> {
        return snapshot.children.mapNotNull { child ->
            try {
                if (child.hasChildren()) {
                    child.getValue(ActivityLog::class.java)?.copy(logId = child.key ?: "")
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }.sortedByDescending { it.timestamp }
    }
}
