package com.example.maadminiproject.data.repository

import com.example.maadminiproject.data.datasource.FirebaseDataSource
import com.example.maadminiproject.data.models.Notification
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

/**
 * Repository providing all notification-related operations for the Smart Home application.
 *
 * This repository sits between the ViewModels and [FirebaseDataSource], abstracting
 * Firebase implementation details and working directly with [Notification] models.
 */
class NotificationRepository {

    /**
     * Internal data source for Firebase operations.
     */
    private val dataSource = FirebaseDataSource()

    /**
     * Observes notifications for a specific home.
     *
     * @param homeId Unique identifier of the home.
     * @param onNotificationsChanged Callback invoked with a list of [Notification] objects when data changes.
     * @param onFailure Callback invoked with a [DatabaseError] upon failure.
     * @return The [ValueEventListener] attached to the database reference.
     */
    fun observeNotifications(
        homeId: String,
        onNotificationsChanged: (List<Notification>) -> Unit,
        onFailure: (DatabaseError) -> Unit,
    ): ValueEventListener {
        return dataSource.observeNotifications(
            homeId,
            onData = { snapshot ->
                onNotificationsChanged(mapNotifications(snapshot))
            },
            onFailure = onFailure,
        )
    }

    /**
     * Marks a specific notification as read.
     *
     * @param homeId Unique identifier of the home.
     * @param notificationId Unique identifier of the notification.
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun markAsRead(
        homeId: String,
        notificationId: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val updates = mapOf("read" to true)
        dataSource.updateNotification(
            homeId,
            notificationId,
            updates,
            onSuccess,
            onFailure,
        )
    }

    /**
     * Updates multiple fields of a specific notification.
     *
     * @param homeId Unique identifier of the home.
     * @param notificationId Unique identifier of the notification.
     * @param updates A map containing field names and their new values.
     * @param onSuccess Optional callback for successful update.
     * @param onFailure Optional callback for failed update.
     */
    fun updateNotificationFields(
        homeId: String,
        notificationId: String,
        updates: Map<String, Any>,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        dataSource.updateNotification(
            homeId,
            notificationId,
            updates,
            onSuccess,
            onFailure,
        )
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
     * Helper function to map a [DataSnapshot] to a list of [Notification] models.
     *
     * @param snapshot The snapshot containing notification children.
     * @return A list of [Notification] objects, excluding any null results.
     */
    private fun mapNotifications(snapshot: DataSnapshot): List<Notification> {
        return snapshot.children.mapNotNull { child ->
            child.getValue(Notification::class.java)
        }
    }
}
