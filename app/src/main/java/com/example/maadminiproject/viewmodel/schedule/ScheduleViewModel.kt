package com.example.maadminiproject.viewmodel.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.maadminiproject.data.models.Schedule
import com.example.maadminiproject.data.repository.ScheduleRepository
import com.google.firebase.database.ValueEventListener

/**
 * ViewModel for managing schedule states in the presentation layer.
 *
 * ScheduleViewModel is responsible for observing the schedules of the smart house
 * and exposing them to the Android UI. It maintains a real-time list of schedules
 * and handles loading and error states.
 *
 * Architecture: UI -> ScheduleViewModel -> ScheduleRepository
 */
class ScheduleViewModel : ViewModel() {

    /**
     * The repository used for all schedule-related operations.
     */
    private val repository = ScheduleRepository()

    private val _schedules = MutableLiveData<List<Schedule>>(emptyList())
    /**
     * Observable LiveData containing the list of schedules for the currently observed home.
     */
    val schedules: LiveData<List<Schedule>> = _schedules

    private val _isLoading = MutableLiveData(false)
    /**
     * Observable LiveData representing the loading state of schedule data observation.
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
     * Starts observing schedules for a specific home.
     *
     * This method sets up a real-time listener that updates the [schedules] LiveData
     * whenever data changes in the Firebase Realtime Database.
     *
     * @param homeId Unique identifier of the home.
     */
    fun observeSchedules(homeId: String) {
        // Remove existing listener before starting a new one
        removeActiveListener()

        _isLoading.value = true

        activeListener = repository.observeSchedules(
            homeId = homeId,
            onSchedulesChanged = { scheduleList ->
                _schedules.value = scheduleList
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
     * Creates a new schedule for the smart home.
     *
     * @param homeId Unique identifier of the home.
     * @param schedule The schedule object to create.
     * @param onSuccess Optional callback with the generated scheduleId.
     */
    fun createSchedule(
        homeId: String,
        schedule: Schedule,
        onSuccess: ((String) -> Unit)? = null,
    ) {
        if (schedule.deviceId.isBlank()) {
            _errorMessage.value = "Failed to create schedule: Device ID must not be blank"
            return
        }
        if (schedule.action.isBlank()) {
            _errorMessage.value = "Failed to create schedule: Action must not be blank"
            return
        }
        if (schedule.startTime.isBlank()) {
            _errorMessage.value = "Failed to create schedule: Start time must not be blank"
            return
        }
        if ((schedule.repeat.equals("NONE", ignoreCase = true) || schedule.repeat.equals("ONCE", ignoreCase = true)) && schedule.startDate.isBlank()) {
            _errorMessage.value = "Failed to create schedule: Start date must not be blank for one-time schedules"
            return
        }

        repository.createSchedule(
            homeId = homeId,
            schedule = schedule,
            onSuccess = onSuccess,
            onFailure = { exception ->
                _errorMessage.value = "Failed to create schedule: ${exception.message}"
            },
        )
    }

    /**
     * Updates an existing schedule.
     *
     * @param homeId Unique identifier of the home.
     * @param schedule The schedule object with updated fields.
     * @param onSuccess Optional callback upon successful update.
     */
    fun updateSchedule(
        homeId: String,
        schedule: Schedule,
        onSuccess: (() -> Unit)? = null,
    ) {
        if (schedule.scheduleId.isBlank()) {
            _errorMessage.value = "Failed to update schedule: Schedule ID must not be blank"
            return
        }

        repository.updateSchedule(
            homeId = homeId,
            schedule = schedule,
            onSuccess = onSuccess,
            onFailure = { exception ->
                _errorMessage.value = "Failed to update schedule: ${exception.message}"
            },
        )
    }

    /**
     * Sets the enabled state of a specific schedule.
     *
     * @param homeId Unique identifier of the home.
     * @param scheduleId Unique identifier of the schedule.
     * @param enabled The new enabled state.
     * @param onSuccess Optional callback upon successful update.
     */
    fun setScheduleEnabled(
        homeId: String,
        scheduleId: String,
        enabled: Boolean,
        onSuccess: (() -> Unit)? = null,
    ) {
        if (scheduleId.isBlank()) {
            _errorMessage.value = "Failed to update schedule: Schedule ID must not be blank"
            return
        }

        repository.setScheduleEnabled(
            homeId = homeId,
            scheduleId = scheduleId,
            enabled = enabled,
            onSuccess = onSuccess,
            onFailure = { exception ->
                _errorMessage.value = "Failed to set schedule state: ${exception.message}"
            },
        )
    }

    /**
     * Toggles the enabled state of a specific schedule.
     *
     * @param homeId Unique identifier of the home.
     * @param scheduleId Unique identifier of the schedule.
     * @param onSuccess Optional callback upon successful update.
     */
    fun toggleScheduleEnabled(
        homeId: String,
        scheduleId: String,
        onSuccess: (() -> Unit)? = null,
    ) {
        val currentSchedule = _schedules.value?.find { it.scheduleId == scheduleId }
        val currentEnabled = currentSchedule?.enabled ?: true
        setScheduleEnabled(
            homeId = homeId,
            scheduleId = scheduleId,
            enabled = !currentEnabled,
            onSuccess = onSuccess,
        )
    }

    /**
     * Deletes a schedule.
     *
     * @param homeId Unique identifier of the home.
     * @param scheduleId Unique identifier of the schedule to delete.
     * @param onSuccess Optional callback upon successful deletion.
     */
    fun deleteSchedule(
        homeId: String,
        scheduleId: String,
        onSuccess: (() -> Unit)? = null,
    ) {
        if (scheduleId.isBlank()) {
            _errorMessage.value = "Failed to delete schedule: Schedule ID must not be blank"
            return
        }

        repository.deleteSchedule(
            homeId = homeId,
            scheduleId = scheduleId,
            onSuccess = onSuccess,
            onFailure = { exception ->
                _errorMessage.value = "Failed to delete schedule: ${exception.message}"
            },
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
            repository.removeSchedulesListener(homeId, listener)
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
