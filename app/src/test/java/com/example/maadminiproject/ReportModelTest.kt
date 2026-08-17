package com.example.maadminiproject

import com.example.maadminiproject.data.models.Device
import com.example.maadminiproject.data.models.Report
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests verifying the Report model and calculation logic for Phase 11C.
 */
class ReportModelTest {

    @Test
    fun report_defaultValues_areCorrect() {
        val report = Report()
        assertEquals(0.0, report.energyUsage, 0.001)
        assertEquals(0, report.activeDevices)
        assertEquals(0, report.onlineDevices)
        assertEquals(0, report.offlineDevices)
        assertEquals(0, report.totalDevices)
    }

    @Test
    fun report_customValues_areCorrect() {
        val report = Report(
            energyUsage = 2715.0,
            activeDevices = 4,
            onlineDevices = 8,
            offlineDevices = 1,
            totalDevices = 9,
        )

        assertEquals(2715.0, report.energyUsage, 0.001)
        assertEquals(4, report.activeDevices)
        assertEquals(8, report.onlineDevices)
        assertEquals(1, report.offlineDevices)
        assertEquals(9, report.totalDevices)
    }

    @Test
    fun report_liveActiveLoadCalculation_isAccurate() {
        val devices = listOf(
            Device(deviceId = "iron01", deviceName = "Iron", state = true, power = 1500.0, online = true),
            Device(deviceId = "ac01", deviceName = "AC", state = true, power = 1200.0, online = true),
            Device(deviceId = "light01", deviceName = "Light", state = false, power = 15.0, online = true),
            Device(deviceId = "plug01", deviceName = "Plug", state = false, power = 100.0, online = false, status = "Offline"),
        )

        val totalDevices = devices.size
        val activeDevices = devices.count { it.state }
        val onlineDevices = devices.count { it.online && it.status != "Offline" }
        val offlineDevices = devices.count { !it.online || it.status == "Offline" }
        val activePower = devices.filter { it.state }.sumOf { it.power }

        assertEquals(4, totalDevices)
        assertEquals(2, activeDevices)
        assertEquals(3, onlineDevices)
        assertEquals(1, offlineDevices)
        assertEquals(2700.0, activePower, 0.001)
    }
}
