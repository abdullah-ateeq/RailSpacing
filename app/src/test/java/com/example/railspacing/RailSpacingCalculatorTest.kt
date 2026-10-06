package com.example.railspacing

import com.example.railspacing.calculator.RailSpacingCalculator
import com.example.railspacing.calculator.UnitConverter
import com.example.railspacing.data.GapLimitStatus
import com.example.railspacing.data.MeasurementUnit
import com.example.railspacing.data.RailSpacingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class RailSpacingCalculatorTest {

    private val epsilon = BigDecimal("0.001")

    // 1. Exact fit
    @Test
    fun testExactFit() {
        // Usable span: 120 cm (1200 mm), Rail: 40 cm (400 mm), Max Gap: 40 cm (400 mm)
        // 2 rails: 800 mm + 400 mm = 1200 mm
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("1200"),
            railWidthMm = BigDecimal("400"),
            maximumGapMm = BigDecimal("400")
        )
        assertTrue(result.isValid)
        assertEquals(2, result.railCount)
        assertEquals(1, result.internalGapCount)
        assertTrue(result.isExactFit)
        assertEquals(0, result.actualClearGapMm.compareTo(BigDecimal("400")))
        assertEquals(0, result.remainingMm.compareTo(BigDecimal.ZERO))
        assertEquals(0, result.totalRailWidthMm.compareTo(BigDecimal("800")))
        assertEquals(0, result.usedSpanMm.compareTo(BigDecimal("1200")))
    }

    // 2. Maximum gap
    @Test
    fun testKnownExampleMaxGap() {
        // Span: 240 cm (2400 mm), Rail: 4 cm (40 mm), Max Gap: 10 cm (100 mm)
        // Expected: 18 rails, Actual clear gap: 9.882352... cm (98.82352... mm)
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("2400"),
            railWidthMm = BigDecimal("40"),
            maximumGapMm = BigDecimal("100")
        )
        assertTrue(result.isValid)
        assertEquals(18, result.railCount)
        assertEquals(17, result.internalGapCount)
        assertEquals(GapLimitStatus.WITHIN_MAXIMUM, result.gapLimitStatus)

        // 1680 / 17 = 98.823529... mm
        val expectedGap = BigDecimal("1680").divide(BigDecimal("17"), 6, RoundingMode.HALF_UP)
        val actualGap6 = result.actualClearGapMm.setScale(6, RoundingMode.HALF_UP)
        assertEquals(0, expectedGap.compareTo(actualGap6))

        // Center-to-center: 40 + 98.823529 = 138.823529 mm
        val expectedC2C = BigDecimal("40").add(expectedGap)
        val actualC2C = result.centerToCenterMm.setScale(6, RoundingMode.HALF_UP)
        assertEquals(0, expectedC2C.compareTo(actualC2C))

        // Actual gap must not exceed maximum gap
        assertTrue(result.actualClearGapMm <= BigDecimal("100"))
    }

    // 3. Non-exact maximum gap
    @Test
    fun testNonExactMaximumGap() {
        // Usable span 2000 mm, Rail 50 mm, Max Gap 120 mm
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("2000"),
            railWidthMm = BigDecimal("50"),
            maximumGapMm = BigDecimal("120")
        )
        assertTrue(result.isValid)
        assertTrue(result.railCount > 1)
        assertTrue(result.actualClearGapMm <= BigDecimal("120"))
    }

    // 4. Zero gap
    @Test
    fun testZeroGap() {
        // Span 200 mm, Rail 40 mm, Gap 0 mm
        // 5 rails placed edge to edge
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("200"),
            railWidthMm = BigDecimal("40"),
            maximumGapMm = BigDecimal.ZERO
        )
        assertTrue(result.isValid)
        assertEquals(5, result.railCount)
        assertEquals(0, result.actualClearGapMm.compareTo(BigDecimal.ZERO))
        assertEquals(0, result.centerToCenterMm.compareTo(BigDecimal("40")))
        assertEquals(0, result.usedSpanMm.compareTo(BigDecimal("200")))
        assertEquals(0, result.remainingMm.compareTo(BigDecimal.ZERO))
    }

    // 5. One rail
    @Test
    fun testOneRail() {
        // Span 40 mm, Rail 40 mm, Max Gap 100 mm
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("40"),
            railWidthMm = BigDecimal("40"),
            maximumGapMm = BigDecimal("100")
        )
        assertTrue(result.isValid)
        assertEquals(1, result.railCount)
        assertEquals(0, result.internalGapCount)
        assertTrue(result.isSingleRail)
        assertEquals(0, result.actualClearGapMm.compareTo(BigDecimal.ZERO))
    }

    // 6. No rail fits
    @Test
    fun testNoRailFits() {
        // Usable span: 1000 mm, Rail: 1200 mm
        // Short by: 200 mm
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("1000"),
            railWidthMm = BigDecimal("1200"),
            maximumGapMm = BigDecimal("100")
        )
        assertFalse(result.isValid)
        assertEquals("0 RAILS FIT", result.errorMessage)
        assertEquals(0, result.railCount)
        assertEquals(0, result.shortByMm?.compareTo(BigDecimal("200")))
    }

    // 7. Rail count mode
    @Test
    fun testKnownExampleRailCount() {
        // Span: 240 cm (2400 mm), Rail: 4 cm (40 mm), Rails: 10
        // Rail width total: 400 mm, Remaining gap space: 2000 mm, 9 gaps
        // Actual clear gap: 2000 / 9 = 222.222222... mm (22.222222... cm)
        val result = RailSpacingCalculator.calculateRailCountLayout(
            spanMm = BigDecimal("2400"),
            railWidthMm = BigDecimal("40"),
            railCount = 10
        )
        assertTrue(result.isValid)
        assertEquals(10, result.railCount)
        assertEquals(9, result.internalGapCount)
        assertEquals(0, result.totalRailWidthMm.compareTo(BigDecimal("400")))

        val expectedGap = BigDecimal("2000").divide(BigDecimal("9"), 6, RoundingMode.HALF_UP)
        val actualGap6 = result.actualClearGapMm.setScale(6, RoundingMode.HALF_UP)
        assertEquals(0, expectedGap.compareTo(actualGap6))

        val expectedC2C = BigDecimal("40").add(expectedGap)
        val actualC2C = result.centerToCenterMm.setScale(6, RoundingMode.HALF_UP)
        assertEquals(0, expectedC2C.compareTo(actualC2C))
    }

    // 8. Start offset
    @Test
    fun testStartOffset() {
        // Span 2400 mm, startOffset 100 mm, endOffset 0 mm, Usable: 2300 mm
        val result = RailSpacingCalculator.calculateRailCountLayout(
            spanMm = BigDecimal("2400"),
            railWidthMm = BigDecimal("50"),
            railCount = 5,
            startOffsetMm = BigDecimal("100"),
            endOffsetMm = BigDecimal.ZERO
        )
        assertTrue(result.isValid)
        assertEquals(0, result.usableSpanMm.compareTo(BigDecimal("2300")))
        assertEquals(0, result.positions.first().outerEdgePositionMm.compareTo(BigDecimal("100")))
    }

    // 9. End offset
    @Test
    fun testEndOffset() {
        // Span 2400 mm, startOffset 0, endOffset 100 mm, Usable 2300 mm
        val result = RailSpacingCalculator.calculateRailCountLayout(
            spanMm = BigDecimal("2400"),
            railWidthMm = BigDecimal("50"),
            railCount = 5,
            startOffsetMm = BigDecimal.ZERO,
            endOffsetMm = BigDecimal("100")
        )
        assertTrue(result.isValid)
        assertEquals(0, result.usableSpanMm.compareTo(BigDecimal("2300")))
        val lastPos = result.positions.last()
        val lastEnd = lastPos.outerEdgePositionMm.add(BigDecimal("50"))
        assertEquals(0, lastEnd.compareTo(BigDecimal("2300")))
    }

    // 10. Both offsets
    @Test
    fun testBothOffsets() {
        // Total span: 240 cm (2400 mm), Start: 5 cm (50 mm), End: 5 cm (50 mm), Usable: 230 cm (2300 mm)
        // Rail: 4 cm (40 mm), Max gap: 10 cm (100 mm)
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("2400"),
            railWidthMm = BigDecimal("40"),
            maximumGapMm = BigDecimal("100"),
            startOffsetMm = BigDecimal("50"),
            endOffsetMm = BigDecimal("50")
        )
        assertTrue(result.isValid)
        assertEquals(0, result.usableSpanMm.compareTo(BigDecimal("2300")))
        assertTrue(result.actualClearGapMm <= BigDecimal("100"))
        assertEquals(0, result.positions.first().outerEdgePositionMm.compareTo(BigDecimal("50")))
        val lastEnd = result.positions.last().outerEdgePositionMm.add(BigDecimal("40"))
        val diff = (lastEnd.subtract(BigDecimal("2350"))).abs()
        assertTrue("Difference $diff exceeds epsilon", diff < epsilon)
    }

    // 11. Center-to-center calculation
    @Test
    fun testCenterToCenterCalculation() {
        val c2c = RailSpacingCalculator.calculateCenterToCenter(
            railWidthMm = BigDecimal("40"),
            clearGapMm = BigDecimal("100")
        )
        assertEquals(0, c2c.compareTo(BigDecimal("140")))
    }

    // 12. Rail positions
    @Test
    fun testRailPositions() {
        val positions = RailSpacingCalculator.calculateRailPositions(
            railCount = 3,
            railWidthMm = BigDecimal("40"),
            clearGapMm = BigDecimal("100"),
            startOffsetMm = BigDecimal("50")
        )
        assertEquals(3, positions.size)
        // Rail 1: outer 50, center 70
        assertEquals(0, positions[0].outerEdgePositionMm.compareTo(BigDecimal("50")))
        assertEquals(0, positions[0].centerPositionMm.compareTo(BigDecimal("70")))
        // Rail 2: outer 50 + 140 = 190, center 210
        assertEquals(0, positions[1].outerEdgePositionMm.compareTo(BigDecimal("190")))
        assertEquals(0, positions[1].centerPositionMm.compareTo(BigDecimal("210")))
        // Rail 3: outer 190 + 140 = 330, center 350
        assertEquals(0, positions[2].outerEdgePositionMm.compareTo(BigDecimal("330")))
        assertEquals(0, positions[2].centerPositionMm.compareTo(BigDecimal("350")))
    }

    // 13. Last position accuracy
    @Test
    fun testLastPositionAccuracy() {
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("2400"),
            railWidthMm = BigDecimal("40"),
            maximumGapMm = BigDecimal("100"),
            startOffsetMm = BigDecimal("50"),
            endOffsetMm = BigDecimal("50")
        )
        val lastRail = result.positions.last()
        val lastOuterEnd = lastRail.outerEdgePositionMm.add(result.railWidthMm)
        val expectedEnd = result.totalSpanMm.subtract(result.endOffsetMm)
        val diff = (lastOuterEnd.subtract(expectedEnd)).abs()
        assertTrue("Difference $diff exceeds epsilon", diff < epsilon)
    }

    // 14. mm conversion
    @Test
    fun testMmConversion() {
        val mm = UnitConverter.toMillimeters(BigDecimal("25"), MeasurementUnit.MILLIMETER)
        assertEquals(0, mm.compareTo(BigDecimal("25")))
        val back = UnitConverter.fromMillimeters(mm, MeasurementUnit.MILLIMETER)
        assertEquals(0, back.compareTo(BigDecimal("25")))
    }

    // 15. cm conversion
    @Test
    fun testCmConversion() {
        val mm = UnitConverter.toMillimeters(BigDecimal("24"), MeasurementUnit.CENTIMETER)
        assertEquals(0, mm.compareTo(BigDecimal("240")))
        val back = UnitConverter.fromMillimeters(mm, MeasurementUnit.CENTIMETER)
        assertEquals(0, back.compareTo(BigDecimal("24")))
    }

    // 16. meter conversion
    @Test
    fun testMeterConversion() {
        val mm = UnitConverter.toMillimeters(BigDecimal("2.4"), MeasurementUnit.METER)
        assertEquals(0, mm.compareTo(BigDecimal("2400")))
        val back = UnitConverter.fromMillimeters(mm, MeasurementUnit.METER)
        assertEquals(0, back.compareTo(BigDecimal("2.4")))
    }

    // 17. inch conversion
    @Test
    fun testInchConversion() {
        val mm = UnitConverter.toMillimeters(BigDecimal("10"), MeasurementUnit.INCH)
        assertEquals(0, mm.compareTo(BigDecimal("254.0")))
        val back = UnitConverter.fromMillimeters(mm, MeasurementUnit.INCH)
        assertEquals(0, back.setScale(4, RoundingMode.HALF_UP).compareTo(BigDecimal("10.0000")))
    }

    // 18. feet conversion
    @Test
    fun testFeetConversion() {
        val mm = UnitConverter.toMillimeters(BigDecimal("2"), MeasurementUnit.FOOT)
        assertEquals(0, mm.compareTo(BigDecimal("609.6")))
        val back = UnitConverter.fromMillimeters(mm, MeasurementUnit.FOOT)
        assertEquals(0, back.setScale(4, RoundingMode.HALF_UP).compareTo(BigDecimal("2.0000")))
    }

    // 19. Decimal dimensions
    @Test
    fun testDecimalDimensions() {
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("1545.5"),
            railWidthMm = BigDecimal("38.2"),
            maximumGapMm = BigDecimal("95.5")
        )
        assertTrue(result.isValid)
        assertTrue(result.railCount > 1)
        assertTrue(result.actualClearGapMm <= BigDecimal("95.5"))
    }

    // 20. Negative values validation
    @Test
    fun testNegativeValues() {
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("-2400"),
            railWidthMm = BigDecimal("40"),
            maximumGapMm = BigDecimal("100")
        )
        assertFalse(result.isValid)
        assertEquals("Span must be greater than zero.", result.errorMessage)
    }

    // 21. Zero span
    @Test
    fun testZeroSpan() {
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal.ZERO,
            railWidthMm = BigDecimal("40"),
            maximumGapMm = BigDecimal("100")
        )
        assertFalse(result.isValid)
        assertEquals("Span must be greater than zero.", result.errorMessage)
    }

    // 22. Zero rail width
    @Test
    fun testZeroRailWidth() {
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("2400"),
            railWidthMm = BigDecimal.ZERO,
            maximumGapMm = BigDecimal("100")
        )
        assertFalse(result.isValid)
        assertEquals("Rail width must be greater than zero.", result.errorMessage)
    }

    // 23. Negative gap
    @Test
    fun testNegativeGap() {
        val result = RailSpacingCalculator.calculateMaxGapLayout(
            spanMm = BigDecimal("2400"),
            railWidthMm = BigDecimal("40"),
            maximumGapMm = BigDecimal("-10")
        )
        assertFalse(result.isValid)
        assertEquals("Gap cannot be negative.", result.errorMessage)
    }

    // 24. Invalid rail count
    @Test
    fun testInvalidRailCount() {
        val result0 = RailSpacingCalculator.calculateRailCountLayout(
            spanMm = BigDecimal("2400"),
            railWidthMm = BigDecimal("40"),
            railCount = 0
        )
        assertFalse(result0.isValid)
        assertEquals("Rail count must be at least 1.", result0.errorMessage)

        val resultOver = RailSpacingCalculator.calculateRailCountLayout(
            spanMm = BigDecimal("2400"),
            railWidthMm = BigDecimal("40"),
            railCount = 1001
        )
        assertFalse(resultOver.isValid)
        assertEquals("Rail count cannot exceed 1000.", resultOver.errorMessage)
    }

    // 25. History serialization
    @Test
    fun testHistorySerialization() {
        val calc = com.example.railspacing.data.SavedCalculation(
            id = "test-123",
            mode = RailSpacingMode.MAX_GAP,
            span = BigDecimal("240"),
            unit = MeasurementUnit.CENTIMETER,
            railWidth = BigDecimal("4"),
            railDepth = BigDecimal("2"),
            maximumGap = BigDecimal("10"),
            railCount = 18,
            startOffset = BigDecimal("5"),
            endOffset = BigDecimal("5"),
            actualGap = BigDecimal("9.88"),
            centerToCenter = BigDecimal("13.88"),
            timestamp = 1700000000000L
        )

        val json = calc.toJson()
        val restored = com.example.railspacing.data.SavedCalculation.fromJson(json)

        assertEquals(calc.id, restored.id)
        assertEquals(calc.mode, restored.mode)
        assertEquals(0, calc.span.compareTo(restored.span))
        assertEquals(calc.unit, restored.unit)
        assertEquals(0, calc.railWidth.compareTo(restored.railWidth))
        assertEquals(0, calc.railDepth?.compareTo(restored.railDepth!!))
        assertEquals(0, calc.maximumGap?.compareTo(restored.maximumGap!!))
        assertEquals(calc.railCount, restored.railCount)
        assertEquals(0, calc.startOffset.compareTo(restored.startOffset))
        assertEquals(0, calc.endOffset.compareTo(restored.endOffset))
        assertEquals(0, calc.actualGap.compareTo(restored.actualGap))
        assertEquals(0, calc.centerToCenter.compareTo(restored.centerToCenter))
        assertEquals(calc.timestamp, restored.timestamp)

        // Test list serialization with 55 items
        val list = (1..55).map { i ->
            calc.copy(id = "item-$i", railCount = i)
        }
        val listJson = com.example.railspacing.data.SavedCalculation.listToJsonString(list)
        val listRestored = com.example.railspacing.data.SavedCalculation.listFromJsonString(listJson)
        assertEquals(55, listRestored.size)
        assertEquals("item-1", listRestored.first().id)
        assertEquals(55, listRestored.last().railCount)
    }

    // 26. Settings defaults
    @Test
    fun testSettingsDefaults() {
        val settings = com.example.railspacing.data.AppSettings()
        assertEquals(com.example.railspacing.data.AppTheme.SYSTEM, settings.theme)
        assertTrue(settings.dynamicColor)
        assertEquals(RailSpacingMode.MAX_GAP, settings.defaultMode)
        assertEquals(MeasurementUnit.CENTIMETER, settings.defaultUnit)
        assertEquals("240", settings.defaultSpan)
        assertEquals("4", settings.defaultRailWidth)
        assertEquals("10", settings.defaultMaxGap)
        assertEquals("0", settings.defaultStartOffset)
        assertEquals("0", settings.defaultEndOffset)
        assertEquals(com.example.railspacing.data.Orientation.HORIZONTAL, settings.orientation)
        assertEquals(2, settings.decimalPlaces)
        assertTrue(settings.showVisualization)
        assertTrue(settings.showPositionList)
        assertTrue(settings.showCenterToCenter)
        assertFalse(settings.showFormula)
        assertTrue(settings.hapticFeedback)
    }
}
