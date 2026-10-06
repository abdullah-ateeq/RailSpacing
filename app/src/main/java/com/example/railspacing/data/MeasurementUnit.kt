package com.example.railspacing.data

import java.math.BigDecimal

enum class MeasurementUnit(
    val id: String,
    val symbol: String,
    val displayName: String,
    val mmFactor: BigDecimal
) {
    MILLIMETER("mm", "mm", "Millimeters (mm)", BigDecimal("1")),
    CENTIMETER("cm", "cm", "Centimeters (cm)", BigDecimal("10")),
    METER("m", "m", "Meters (m)", BigDecimal("1000")),
    INCH("in", "in", "Inches (in)", BigDecimal("25.4")),
    FOOT("ft", "ft", "Feet (ft)", BigDecimal("304.8"));

    companion object {
        fun fromId(id: String): MeasurementUnit {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CENTIMETER
        }
    }
}
