package com.example.maadminiproject.ui.device

data class DeviceInfo(
    val id: String,
    val name: String,
    val type: DeviceType,
    val status: String,
    val isOn: Boolean,
    val powerUsage: String? = null,
    val temperature: String? = null
)

enum class DeviceType {
    LIGHTING, FAN, CAMERA, AC, PLUG
}