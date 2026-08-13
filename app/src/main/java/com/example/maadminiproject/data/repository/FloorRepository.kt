package com.example.maadminiproject.data.repository

import com.example.maadminiproject.data.datasource.FirebaseDataSource
import com.example.maadminiproject.data.models.Floor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

/**
 * Repository providing all floor-related operations for the Smart Home application.
 *
 * This repository sits between the ViewModels and [FirebaseDataSource], abstracting
 * Firebase implementation details and working directly with [Floor] models.
 */
class FloorRepository {

    /**
     * Internal data source for Firebase operations.
     */
    private val dataSource = FirebaseDataSource()

    /**
     * Observes floors for a specific home.
     *
     * @param homeId Unique identifier of the home.
     * @param onFloorsChanged Callback invoked with a list of [Floor] objects when data changes.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] attached to the database reference.
     */
    fun observeFloors(
        homeId: String,
        onFloorsChanged: (List<Floor>) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return dataSource.observeFloors(
            homeId,
            onData = { snapshot ->
                onFloorsChanged(mapFloors(snapshot))
            },
            onFailure = onFailure,
        )
    }

    /**
     * Removes an active floors listener.
     *
     * @param homeId Unique identifier of the home.
     * @param listener The listener to be removed.
     */
    fun removeFloorsListener(
        homeId: String,
        listener: ValueEventListener,
    ) {
        dataSource.removeFloorListener(homeId, listener)
    }

    /**
     * Helper function to map a [DataSnapshot] to a list of [Floor] models.
     *
     * @param snapshot The snapshot containing floor children.
     * @return A list of [Floor] objects, excluding any null results.
     */
    private fun mapFloors(snapshot: DataSnapshot): List<Floor> {
        return snapshot.children.mapNotNull { child ->
            child.getValue(Floor::class.java)
        }
    }
}
