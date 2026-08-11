package com.example.maadminiproject.viewmodel.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.maadminiproject.data.models.Report
import com.example.maadminiproject.data.repository.ReportRepository
import com.google.firebase.database.ValueEventListener

/**
 * ViewModel for managing report data in the presentation layer.
 *
 * ReportViewModel is responsible for observing the daily report of the smart house
 * and exposing it to the Android UI. It maintains real-time updates for energy usage
 * and device status counts.
 *
 * Architecture: UI -> ReportViewModel -> ReportRepository
 */
class ReportViewModel : ViewModel() {

    /**
     * The repository used for report-related operations.
     */
    private val repository = ReportRepository()

    private val _report = MutableLiveData<Report?>(null)
    /**
     * Observable LiveData containing the daily report.
     */
    val report: LiveData<Report?> = _report

    private val _isLoading = MutableLiveData(false)
    /**
     * Observable LiveData representing the loading state of report data observation.
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
     * Starts observing the daily report for a specific home.
     *
     * This method sets up a real-time listener that updates the [report] LiveData
     * whenever data changes in the Firebase Realtime Database.
     *
     * @param homeId Unique identifier of the home.
     */
    fun observeReport(homeId: String) {
        // Remove existing listener before starting a new one
        removeActiveListener()

        _isLoading.value = true

        activeListener = repository.observeReports(
            homeId = homeId,
            onReportChanged = { reportData ->
                _report.value = reportData
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
            repository.removeReportsListener(homeId, listener)
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
