package com.example.maadminiproject.data.repository

import com.example.maadminiproject.data.datasource.FirebaseDataSource
import com.example.maadminiproject.data.models.Settings
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

/**
 * Repository providing all settings-related operations for the Smart Home application.
 *
 * This repository sits between the ViewModels and [FirebaseDataSource], abstracting
 * Firebase implementation details and working directly with [Settings] models.
 */
class SettingsRepository {

    /**
     * Internal data source for Firebase operations.
     */
    private val dataSource = FirebaseDataSource()

    /**
     * Observes settings for a specific home.
     *
     * @param homeId Unique identifier of the home.
     * @param onSettingsChanged Callback invoked with the [Settings] object when data changes.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] attached to the database reference.
     */
    fun observeSettings(
        homeId: String,
        onSettingsChanged: (Settings) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return dataSource.observeSettings(
            homeId,
            onData = { snapshot ->
                val settings = mapSettings(snapshot) ?: Settings()
                onSettingsChanged(settings)
            },
            onFailure = onFailure,
        )
    }

    /**
     * Removes an active settings listener.
     *
     * @param homeId Unique identifier of the home.
     * @param listener The listener to be removed.
     */
    fun removeSettingsListener(
        homeId: String,
        listener: ValueEventListener,
    ) {
        dataSource.removeSettingsListener(homeId, listener)
    }

    /**
     * Helper function to map a [DataSnapshot] to a [Settings] model.
     *
     * @param snapshot The snapshot containing settings data.
     * @return A [Settings] object if valid, null otherwise.
     */
    private fun mapSettings(snapshot: DataSnapshot): Settings? {
        return snapshot.getValue(Settings::class.java)
    }
}
