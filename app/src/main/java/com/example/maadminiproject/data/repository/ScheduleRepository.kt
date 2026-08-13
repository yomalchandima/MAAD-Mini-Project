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
