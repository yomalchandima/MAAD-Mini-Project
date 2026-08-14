package com.example.maadminiproject.data.repository

import com.example.maadminiproject.data.datasource.FirebaseDataSource
import com.example.maadminiproject.data.models.Schedule
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

/**
 * Repository providing all schedule-related operations for the Smart Home application.
 *
 * This repository sits between the ViewModels and [FirebaseDataSource], abstracting
 * Firebase implementation details and working directly with [Schedule] models.
 */
class ScheduleRepository {

    /**
     * Internal data source for Firebase operations.
     */
    private val dataSource = FirebaseDataSource()

    /**
     * Observes schedules for a specific home.
     *
     * @param homeId Unique identifier of the home.
     * @param onSchedulesChanged Callback invoked with a list of [Schedule] objects when data changes.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] attached to the database reference.
     */
    fun observeSchedules(
        homeId: String,
        onSchedulesChanged: (List<Schedule>) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return dataSource.observeSchedules(
            homeId,
            onData = { snapshot ->
                onSchedulesChanged(mapSchedules(snapshot))
            },
            onFailure = onFailure,
        )
    }

    /**
     * Removes an active schedules listener.
     *
     * @param homeId Unique identifier of the home.
     * @param listener The listener to be removed.
     */
    fun removeSchedulesListener(
        homeId: String,
        listener: ValueEventListener,
    ) {
        dataSource.removeScheduleListener(homeId, listener)
    }

    /**
     * Creates a new schedule.
     *
     * @param homeId Unique identifier of the home.
     * @param schedule The schedule to create.
     * @param onSuccess Optional callback receiving the created scheduleId.
     * @param onFailure Optional callback for failed creation.
     */
    fun createSchedule(
        homeId: String,
        schedule: Schedule,
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        if (schedule.deviceId.isBlank()) {
            onFailure?.invoke(IllegalArgumentException("Device ID must not be blank"))
            return
        }
        if (schedule.action.isBlank()) {
            onFailure?.invoke(IllegalArgumentException("Action must not be blank"))
            return
        }
        if (schedule.startTime.isBlank()) {
            onFailure?.invoke(IllegalArgumentException("Start time must not be blank"))
            return
        }
        dataSource.createSchedule(homeId, schedule, onSuccess, onFailure)
    }

    /**
     * Updates an existing schedule.
     *
     * @param homeId Unique identifier of the home.
     * @param schedule The schedule to update.
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun updateSchedule(
        homeId: String,
        schedule: Schedule,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        if (schedule.scheduleId.isBlank()) {
            onFailure?.invoke(IllegalArgumentException("Schedule ID must not be blank for updates"))
            return
        }
        if (schedule.deviceId.isBlank()) {
            onFailure?.invoke(IllegalArgumentException("Device ID must not be blank"))
            return
        }
        if (schedule.action.isBlank()) {
            onFailure?.invoke(IllegalArgumentException("Action must not be blank"))
            return
        }
        if (schedule.startTime.isBlank()) {
            onFailure?.invoke(IllegalArgumentException("Start time must not be blank"))
            return
        }
        dataSource.updateSchedule(homeId, schedule, onSuccess, onFailure)
    }

    /**
     * Updates the enabled state of a specific schedule.
     *
     * @param homeId Unique identifier of the home.
     * @param scheduleId Unique identifier of the schedule.
     * @param enabled New enabled state.
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun setScheduleEnabled(
        homeId: String,
        scheduleId: String,
        enabled: Boolean,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        if (scheduleId.isBlank()) {
            onFailure?.invoke(IllegalArgumentException("Schedule ID must not be blank"))
            return
        }
        dataSource.updateScheduleEnabled(homeId, scheduleId, enabled, onSuccess, onFailure)
    }

    /**
     * Deletes a schedule.
     *
     * @param homeId Unique identifier of the home.
     * @param scheduleId Unique identifier of the schedule to delete.
     * @param onSuccess Optional callback for successful deletion.
     * @param onFailure Optional callback for failed deletion.
     */
    fun deleteSchedule(
        homeId: String,
        scheduleId: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        if (scheduleId.isBlank()) {
            onFailure?.invoke(IllegalArgumentException("Schedule ID must not be blank"))
            return
        }
        dataSource.deleteSchedule(homeId, scheduleId, onSuccess, onFailure)
    }

    /**
     * Helper function to map a [DataSnapshot] to a list of [Schedule] models.
     *
     * @param snapshot The snapshot containing schedule children.
     * @return A list of [Schedule] objects, excluding any null results.
     */
    private fun mapSchedules(snapshot: DataSnapshot): List<Schedule> {
        return snapshot.children.mapNotNull { child ->
            child.getValue(Schedule::class.java)
        }
    }
}
