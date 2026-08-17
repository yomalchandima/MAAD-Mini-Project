package com.example.maadminiproject.data.repository

import com.example.maadminiproject.data.datasource.FirebaseDataSource
import com.example.maadminiproject.data.models.Report
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

/**
 * Repository providing all report-related operations for the Smart Home application.
 *
 * This repository sits between the ViewModels and [FirebaseDataSource], abstracting
 * Firebase implementation details and working directly with [Report] models.
 */
class ReportRepository {

    /**
     * Internal data source for Firebase operations.
     */
    private val dataSource = FirebaseDataSource()

    /**
     * Observes report changes for a specific home.
     *
     * This method listens for changes at the "reports" node and extracts the "today"
     * child to provide daily report updates.
     *
     * @param homeId Unique identifier of the home.
     * @param onReportChanged Callback invoked with the daily [Report] (or null) when data changes.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] attached to the database reference.
     */
    fun observeReports(
        homeId: String,
        onReportChanged: (Report?) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return dataSource.observeReports(
            homeId,
            onData = { snapshot ->
                onReportChanged(mapReport(snapshot))
            },
            onFailure = onFailure,
        )
    }

    /**
     * Removes an active reports listener.
     *
     * @param homeId Unique identifier of the home.
     * @param listener The listener to be removed.
     */
    fun removeReportsListener(
        homeId: String,
        listener: ValueEventListener,
    ) {
        dataSource.removeReportListener(homeId, listener)
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
     * Helper function to extract and map the daily report from a snapshot.
     *
     * @param snapshot The snapshot containing report data.
     * @return A [Report] model if the "today" child exists and is valid, null otherwise.
     */
    private fun mapReport(snapshot: DataSnapshot): Report? {
        return try {
            snapshot.child("today").getValue(Report::class.java)
        } catch (_: Exception) {
            null
        }
    }
}
