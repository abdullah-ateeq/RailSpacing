package com.example.railspacing.data

enum class RailSpacingMode(val label: String) {
    MAX_GAP("MAX GAP"),
    RAIL_COUNT("RAIL COUNT");

    companion object {
        fun fromName(name: String): RailSpacingMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: MAX_GAP
        }
    }
}
