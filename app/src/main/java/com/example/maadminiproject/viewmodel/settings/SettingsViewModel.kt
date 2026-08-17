package com.example.maadminiproject.viewmodel.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.maadminiproject.data.models.Settings
import com.example.maadminiproject.data.repository.SettingsRepository
import com.google.firebase.database.ValueEventListener

/**
 * ViewModel for managing application and home settings in the presentation layer.
 *
 * SettingsViewModel is responsible for observing settings data and exposing it
 * to the Android UI. It maintains real-time updates for user preferences and
 * handles loading and error states.
 *
 * Architecture: UI -> SettingsViewModel -> SettingsRepository
 */
class SettingsViewModel : ViewModel() {

    /**
     * The repository used for settings-related operations.
     */
    private val repository = SettingsRepository()

    private val _settings = MutableLiveData(Settings())
    /**
     * Observable LiveData containing the current application and home settings.
     */
    val settings: LiveData<Settings> = _settings

    private val _isLoading = MutableLiveData(false)
    /**
     * Observable LiveData representing the loading state of settings data observation.
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
     * Starts observing settings for a specific home.
     *
     * This method sets up a real-time listener that updates the [settings] LiveData
     * whenever data changes in the Firebase Realtime Database.
     *
     * @param homeId Unique identifier of the home.
     */
    fun observeSettings(homeId: String) {
        // Remove existing listener before starting a new one
        removeActiveListener()

        _isLoading.value = true
        activeHomeId = homeId

        activeListener = repository.observeSettings(
            homeId = homeId,
            onSettingsChanged = { updatedSettings ->
                _settings.value = updatedSettings
                _isLoading.value = false
            },
            onFailure = { databaseError ->
                _errorMessage.value = "Database Error: ${databaseError.message}"
                _isLoading.value = false
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
            repository.removeSettingsListener(homeId, listener)
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
