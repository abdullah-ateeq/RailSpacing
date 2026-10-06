package com.example.railspacing.data

enum class AppTheme(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromName(name: String): AppTheme =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SYSTEM
    }
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultMode: RailSpacingMode = RailSpacingMode.MAX_GAP,
    val defaultUnit: MeasurementUnit = MeasurementUnit.CENTIMETER,
    val defaultSpan: String = "240",
    val defaultRailWidth: String = "4",
    val defaultMaxGap: String = "10",
    val defaultStartOffset: String = "0",
    val defaultEndOffset: String = "0",
    val orientation: Orientation = Orientation.HORIZONTAL,
    val decimalPlaces: Int = 2,
    val showVisualization: Boolean = true,
    val showPositionList: Boolean = true,
    val showCenterToCenter: Boolean = true,
    val showFormula: Boolean = false,
    val hapticFeedback: Boolean = true
)
