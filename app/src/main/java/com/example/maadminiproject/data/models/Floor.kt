package com.example.maadminiproject.data.models

/**
 * Data model representing a floor in the smart house.
 *
 * @property floorId Unique identifier for the floor.
 * @property floorName User-friendly name of the floor (e.g., "Ground Floor", "First Floor").
 * @property floorPlanImage Reference/name of the floor plan template or image (e.g., "ground_floor_map").
 * @property zones A map of zones (rooms) located on this floor, keyed by their [Zone.zoneId].
 */
data class Floor(
    val floorId: String = "",
    val floorName: String = "",
    val floorPlanImage: String? = null,
    val zones: MutableMap<String, Zone> = mutableMapOf(),
)
