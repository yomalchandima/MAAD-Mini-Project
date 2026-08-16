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
     * Creates a new notification entry in Firebase.
     *
     * @param homeId Unique identifier of the home.
     * @param notification The [Notification] object to write.
     * @param onSuccess Optional callback returning the created notificationId upon success.
     * @param onFailure Optional callback invoked with an [Exception] upon failure.
     */
    fun createNotification(
        homeId: String,
        notification: Notification,
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        dataSource.createNotification(
            homeId = homeId,
            notification = notification,
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

    /**
     * Helper to quickly create and record a smart home notification.
     *
     * @param homeId Unique identifier of the home.
     * @param title Title of the alert/notification.
     * @param message Detailed description.
     * @param type Category (e.g. "SAFETY", "SCHEDULE", "SECURITY", "SYSTEM").
     * @param deviceId Associated device identifier, if applicable.
     * @param onSuccess Optional callback upon success.
     * @param onFailure Optional callback upon failure.
     */
    fun pushNotification(
        homeId: String,
        title: String,
        message: String,
        type: String = "SYSTEM",
        deviceId: String = "",
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null,
    ) {
        val notification = Notification(
            title = title,
            message = message,
            type = type.uppercase(),
            timestamp = System.currentTimeMillis(),
            isRead = false,
            deviceId = deviceId,
        )
        createNotification(homeId, notification, onSuccess, onFailure)
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
        val updates = mapOf<String, Any>("isRead" to true)
        dataSource.updateNotification(
            homeId,
            notificationId,
            updates,
            onSuccess,
            onFailure,
        )
    }

    /**
     * Marks all provided unread notifications as read.
     *
     * @param homeId Unique identifier of the home.
     * @param notifications List of notifications to process.
     * @param onComplete Optional callback when all updates are dispatched.
     */
    fun markAllAsRead(
        homeId: String,
        notifications: List<Notification>,
        onComplete: (() -> Unit)? = null,
    ) {
        val unread = notifications.filter { !it.isRead }
        if (unread.isEmpty()) {
            onComplete?.invoke()
            return
        }
        var remaining = unread.size
        unread.forEach { notif ->
            markAsRead(
                homeId = homeId,
                notificationId = notif.notificationId,
                onSuccess = {
                    remaining--
                    if (remaining == 0) onComplete?.invoke()
                },
                onFailure = {
                    remaining--
                    if (remaining == 0) onComplete?.invoke()
                }
            )
        }
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
     * Removes an active notifications listener.
     *
     * @param homeId Unique identifier of the home.
     * @param listener The listener to be removed.
     */
    fun removeNotificationsListener(
        homeId: String,
        listener: ValueEventListener,
    ) {
        dataSource.removeNotificationListener(homeId, listener)
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
     * @return A list of [Notification] objects sorted newest first.
     */
    private fun mapNotifications(snapshot: DataSnapshot): List<Notification> {
        return snapshot.children.mapNotNull { child ->
            try {
                if (child.hasChildren()) {
                    val notif = child.getValue(Notification::class.java)
                    val isReadVal = child.child("isRead").getValue(Boolean::class.java)
                        ?: child.child("read").getValue(Boolean::class.java)
                        ?: notif?.isRead
                        ?: false
                    notif?.copy(
                        notificationId = child.key ?: "",
                        isRead = isReadVal,
                    )
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }.sortedByDescending { it.timestamp }
    }
}
