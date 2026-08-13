package com.example.maadminiproject.viewmodel.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.maadminiproject.data.models.Notification
import com.example.maadminiproject.data.repository.NotificationRepository
import com.google.firebase.database.ValueEventListener

/**
 * ViewModel for managing notification states in the presentation layer.
 *
 * NotificationViewModel is responsible for observing the notifications of the
 * smart house and exposing them to the Android UI. It maintains a real-time
 * list of notifications and handles loading and error states.
 *
 * Architecture: UI -> NotificationViewModel -> NotificationRepository
 */
class NotificationViewModel : ViewModel() {

    /**
     * The repository used for all notification-related operations.
     */
    private val repository = NotificationRepository()

    private val _notifications = MutableLiveData<List<Notification>>(emptyList())
    /**
     * Observable LiveData containing the list of notifications for the currently observed home.
     */
    val notifications: LiveData<List<Notification>> = _notifications

    private val _isLoading = MutableLiveData(false)
    /**
     * Observable LiveData representing the loading state of notification data observation.
     */
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    /**
     * Observable LiveData containing the current error message, if any.
     */
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Holds the active Firebase listener to allow for cleanup when the ViewModel is cleared.
     */
    private var activeListener: ValueEventListener? = null

    /**
     * Holds the homeId associated with the active listener for cleanup.
     */
    private var activeHomeId: String? = null

    /**
     * Starts observing notifications for a specific home.
     *
     * This method sets up a real-time listener that updates the [notifications] LiveData
     * whenever data changes in the Firebase Realtime Database.
     *
     * @param homeId Unique identifier of the home.
     */
    fun observeNotifications(homeId: String) {
        // Remove existing listener before starting a new one
        removeActiveListener()

        _isLoading.value = true

        activeListener = repository.observeNotifications(
            homeId = homeId,
            onNotificationsChanged = { notificationList ->
                _notifications.value = notificationList
                _isLoading.value = false
            },
            onFailure = { databaseError ->
                _errorMessage.value = "Database Error: ${databaseError.message}"
                _isLoading.value = false
            },
        )

        // Store identifiers for cleanup
        activeHomeId = homeId
    }

    /**
     * Marks a specific notification as read.
     *
     * @param homeId Unique identifier of the home.
     * @param notificationId Unique identifier of the notification.
     */
    fun markAsRead(homeId: String, notificationId: String) {
        repository.markAsRead(
            homeId = homeId,
            notificationId = notificationId,
            onFailure = { exception ->
                _errorMessage.value = "Failed to mark notification as read: ${exception.message}"
            }
        )
    }

    /**
     * Updates multiple fields of a specific notification.
     *
     * @param homeId Unique identifier of the home.
     * @param notificationId Unique identifier of the notification.
     * @param updates A map containing the fields to update and their new values.
     */
    fun updateNotificationFields(
        homeId: String,
        notificationId: String,
        updates: Map<String, Any>
    ) {
        repository.updateNotificationFields(
            homeId = homeId,
            notificationId = notificationId,
            updates = updates,
            onFailure = { exception ->
                _errorMessage.value = "Failed to update notification: ${exception.message}"
            }
        )
    }

    /**
     * Clears the current error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Internal helper to remove the active listener through the repository.
     */
    private fun removeActiveListener() {
        val listener = activeListener
        val homeId = activeHomeId

        if ((listener != null) && (homeId != null)) {
            repository.removeNotificationsListener(homeId, listener)
        }

        activeListener = null
        activeHomeId = null
    }

    /**
     * Cleans up resources when the ViewModel is no longer in use.
     * Removes the active Firebase listener to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        removeActiveListener()
    }
}
