package com.example.maadminiproject.viewmodel.floor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.maadminiproject.data.models.Floor
import com.example.maadminiproject.data.repository.FloorRepository
import com.google.firebase.database.ValueEventListener

/**
 * ViewModel for managing floor states in the presentation layer.
 *
 * FloorViewModel is responsible for observing the floors of the smart house
 * and exposing them to the Android UI. It maintains a real-time list of floors
 * and handles loading and error states.
 *
 * Architecture: UI -> FloorViewModel -> FloorRepository
 */
class FloorViewModel : ViewModel() {

    /**
     * The repository used for all floor-related operations.
     */
    private val repository = FloorRepository()

    private val _floors = MutableLiveData<List<Floor>>(emptyList())
    /**
     * Observable LiveData containing the list of floors for the currently observed home.
     */
    val floors: LiveData<List<Floor>> = _floors

    private val _isLoading = MutableLiveData(false)
    /**
     * Observable LiveData representing the loading state of floor data observation.
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
     * Starts observing floors for a specific home.
     *
     * This method sets up a real-time listener that updates the [floors] LiveData
     * whenever data changes in the Firebase Realtime Database.
     *
     * @param homeId Unique identifier of the home.
     */
    fun observeFloors(homeId: String) {
        // Remove existing listener before starting a new one
        removeActiveListener()

        _isLoading.value = true

        activeListener = repository.observeFloors(
            homeId = homeId,
            onFloorsChanged = { floorList ->
                _floors.value = floorList
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
            repository.removeFloorsListener(homeId, listener)
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
