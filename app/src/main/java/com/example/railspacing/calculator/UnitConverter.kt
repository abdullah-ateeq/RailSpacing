package com.example.railspacing.calculator

import com.example.railspacing.data.MeasurementUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object UnitConverter {

    private const val CALCULATION_SCALE = 12

    fun toMillimeters(value: BigDecimal, unit: MeasurementUnit): BigDecimal {
        return value.multiply(unit.mmFactor)
    }

    fun fromMillimeters(valueMm: BigDecimal, unit: MeasurementUnit): BigDecimal {
        if (unit.mmFactor.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        return valueMm.divide(unit.mmFactor, CALCULATION_SCALE, RoundingMode.HALF_UP)
    }

    fun formatMeasurement(
        valueMm: BigDecimal,
        unit: MeasurementUnit,
        decimalPlaces: Int = 2,
        includeUnit: Boolean = true
    ): String {
        val inUnit = fromMillimeters(valueMm, unit)
        val formattedNumber = formatNumber(inUnit, decimalPlaces)
        return if (includeUnit) {
            "$formattedNumber ${unit.symbol}"
        } else {
            formattedNumber
        }
    }

    fun formatNumber(value: BigDecimal, decimalPlaces: Int = 2): String {
        val safeScale = decimalPlaces.coerceIn(1, 4)
        val rounded = value.setScale(safeScale, RoundingMode.HALF_UP)
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = buildString {
            append("#,##0.")
            repeat(safeScale) { append("0") }
        }
        val df = DecimalFormat(pattern, symbols)
        return df.format(rounded)
    }

    fun parseInput(text: String): BigDecimal? {
        val clean = text.trim().replace(',', '.')
        if (clean.isEmpty()) return null
        return try {
            BigDecimal(clean)
        } catch (_: NumberFormatException) {
            null
        }
    }
}
