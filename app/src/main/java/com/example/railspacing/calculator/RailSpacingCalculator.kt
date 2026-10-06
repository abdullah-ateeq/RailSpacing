package com.example.railspacing.calculator

import com.example.railspacing.data.GapLimitStatus
import com.example.railspacing.data.RailPosition
import com.example.railspacing.data.RailSpacingMode
import com.example.railspacing.data.RailSpacingResult
import java.math.BigDecimal
import java.math.RoundingMode

object RailSpacingCalculator {

    private const val CALCULATION_SCALE = 12
    private val EPSILON = BigDecimal("0.0001")

    fun calculateMaxGapLayout(
        spanMm: BigDecimal,
        railWidthMm: BigDecimal,
        maximumGapMm: BigDecimal,
        startOffsetMm: BigDecimal = BigDecimal.ZERO,
        endOffsetMm: BigDecimal = BigDecimal.ZERO,
        railDepthMm: BigDecimal? = null
    ): RailSpacingResult {
        // Validation checks
        val validationError = validateBasicInputs(spanMm, railWidthMm, startOffsetMm, endOffsetMm)
        if (validationError != null) {
            return RailSpacingResult(
                isValid = false,
                errorMessage = validationError,
                mode = RailSpacingMode.MAX_GAP
            )
        }

        if (maximumGapMm < BigDecimal.ZERO) {
            return RailSpacingResult(
                isValid = false,
                errorMessage = "Gap cannot be negative.",
                mode = RailSpacingMode.MAX_GAP
            )
        }

        val usableSpanMm = spanMm.subtract(startOffsetMm).subtract(endOffsetMm)

        // If not even a single rail fits in the usable span
        if (usableSpanMm < railWidthMm) {
            val shortBy = railWidthMm.subtract(usableSpanMm)
            return RailSpacingResult(
                isValid = false,
                errorMessage = "0 RAILS FIT",
                mode = RailSpacingMode.MAX_GAP,
                railCount = 0,
                totalSpanMm = spanMm,
                startOffsetMm = startOffsetMm,
                endOffsetMm = endOffsetMm,
                usableSpanMm = usableSpanMm,
                railWidthMm = railWidthMm,
                railDepthMm = railDepthMm,
                shortByMm = shortBy,
                availableMm = usableSpanMm,
                requiredRailWidthMm = railWidthMm
            )
        }

        val railCount = calculateRailCount(usableSpanMm, railWidthMm, maximumGapMm)

        return if (railCount <= 1) {
            val totalRailWidth = railWidthMm
            val remainingMm = usableSpanMm.subtract(totalRailWidth)
            val isExact = remainingMm.abs() <= EPSILON
            val cleanRemaining = if (isExact) BigDecimal.ZERO else remainingMm
            val usedSpan = startOffsetMm.add(totalRailWidth).add(endOffsetMm)
            val positions = calculateRailPositions(1, railWidthMm, BigDecimal.ZERO, startOffsetMm)

            RailSpacingResult(
                isValid = true,
                mode = RailSpacingMode.MAX_GAP,
                railCount = 1,
                internalGapCount = 0,
                totalSpanMm = spanMm,
                startOffsetMm = startOffsetMm,
                endOffsetMm = endOffsetMm,
                usableSpanMm = usableSpanMm,
                railWidthMm = railWidthMm,
                railDepthMm = railDepthMm,
                totalRailWidthMm = totalRailWidth,
                actualClearGapMm = BigDecimal.ZERO,
                requestedMaxGapMm = maximumGapMm,
                centerToCenterMm = railWidthMm,
                usedSpanMm = usedSpan,
                remainingMm = cleanRemaining,
                isExactFit = isExact,
                isSingleRail = true,
                gapLimitStatus = GapLimitStatus.WITHIN_MAXIMUM,
                positions = positions
            )
        } else {
            val internalGaps = railCount - 1
            val totalRailWidth = railWidthMm.multiply(BigDecimal(railCount))
            val actualClearGap = calculateClearGap(usableSpanMm, railWidthMm, railCount)
            val centerToCenter = calculateCenterToCenter(railWidthMm, actualClearGap)
            val usedSpan = calculateUsedSpan(railCount, railWidthMm, actualClearGap, startOffsetMm, endOffsetMm)
            val remaining = usableSpanMm.subtract(totalRailWidth.add(actualClearGap.multiply(BigDecimal(internalGaps))))
            val isExact = remaining.abs() <= EPSILON
            val cleanRemaining = if (isExact) BigDecimal.ZERO else remaining
            val positions = calculateRailPositions(railCount, railWidthMm, actualClearGap, startOffsetMm)

            RailSpacingResult(
                isValid = true,
                mode = RailSpacingMode.MAX_GAP,
                railCount = railCount,
                internalGapCount = internalGaps,
                totalSpanMm = spanMm,
                startOffsetMm = startOffsetMm,
                endOffsetMm = endOffsetMm,
                usableSpanMm = usableSpanMm,
                railWidthMm = railWidthMm,
                railDepthMm = railDepthMm,
                totalRailWidthMm = totalRailWidth,
                actualClearGapMm = actualClearGap,
                requestedMaxGapMm = maximumGapMm,
                centerToCenterMm = centerToCenter,
                usedSpanMm = usedSpan,
                remainingMm = cleanRemaining,
                isExactFit = isExact,
                isSingleRail = false,
                gapLimitStatus = GapLimitStatus.WITHIN_MAXIMUM,
                positions = positions
            )
        }
    }

    fun calculateRailCountLayout(
        spanMm: BigDecimal,
        railWidthMm: BigDecimal,
        railCount: Int,
        startOffsetMm: BigDecimal = BigDecimal.ZERO,
        endOffsetMm: BigDecimal = BigDecimal.ZERO,
        railDepthMm: BigDecimal? = null,
        maxGapCheckEnabled: Boolean = false,
        maximumGapMm: BigDecimal? = null
    ): RailSpacingResult {
        val validationError = validateBasicInputs(spanMm, railWidthMm, startOffsetMm, endOffsetMm)
        if (validationError != null) {
            return RailSpacingResult(
                isValid = false,
                errorMessage = validationError,
                mode = RailSpacingMode.RAIL_COUNT
            )
        }

        if (railCount < 1) {
            return RailSpacingResult(
                isValid = false,
                errorMessage = "Rail count must be at least 1.",
                mode = RailSpacingMode.RAIL_COUNT
            )
        }

        if (railCount > 1000) {
            return RailSpacingResult(
                isValid = false,
                errorMessage = "Rail count cannot exceed 1000.",
                mode = RailSpacingMode.RAIL_COUNT
            )
        }

        val usableSpanMm = spanMm.subtract(startOffsetMm).subtract(endOffsetMm)
        val totalRailWidthMm = railWidthMm.multiply(BigDecimal(railCount))

        if (totalRailWidthMm > usableSpanMm) {
            val shortBy = totalRailWidthMm.subtract(usableSpanMm)
            return RailSpacingResult(
                isValid = false,
                errorMessage = "These rails do not fit within the available span.",
                mode = RailSpacingMode.RAIL_COUNT,
                railCount = railCount,
                totalSpanMm = spanMm,
                startOffsetMm = startOffsetMm,
                endOffsetMm = endOffsetMm,
                usableSpanMm = usableSpanMm,
                railWidthMm = railWidthMm,
                railDepthMm = railDepthMm,
                totalRailWidthMm = totalRailWidthMm,
                shortByMm = shortBy,
                availableMm = usableSpanMm,
                requiredRailWidthMm = totalRailWidthMm
            )
        }

        val internalGaps = if (railCount > 1) railCount - 1 else 0
        val actualClearGap = if (railCount > 1) {
            calculateClearGap(usableSpanMm, railWidthMm, railCount)
        } else {
            BigDecimal.ZERO
        }

        val centerToCenter = calculateCenterToCenter(railWidthMm, actualClearGap)
        val usedSpan = calculateUsedSpan(railCount, railWidthMm, actualClearGap, startOffsetMm, endOffsetMm)
        val remaining = usableSpanMm.subtract(totalRailWidthMm.add(actualClearGap.multiply(BigDecimal(internalGaps))))
        val isExact = remaining.abs() <= EPSILON
        val cleanRemaining = if (isExact) BigDecimal.ZERO else remaining

        val gapStatus = if (maxGapCheckEnabled && maximumGapMm != null) {
            if (actualClearGap <= maximumGapMm) {
                GapLimitStatus.WITHIN_MAXIMUM
            } else {
                GapLimitStatus.GAP_EXCEEDS_LIMIT
            }
        } else {
            GapLimitStatus.NOT_APPLICABLE
        }

        val positions = calculateRailPositions(railCount, railWidthMm, actualClearGap, startOffsetMm)

        return RailSpacingResult(
            isValid = true,
            mode = RailSpacingMode.RAIL_COUNT,
            railCount = railCount,
            internalGapCount = internalGaps,
            totalSpanMm = spanMm,
            startOffsetMm = startOffsetMm,
            endOffsetMm = endOffsetMm,
            usableSpanMm = usableSpanMm,
            railWidthMm = railWidthMm,
            railDepthMm = railDepthMm,
            totalRailWidthMm = totalRailWidthMm,
            actualClearGapMm = actualClearGap,
            requestedMaxGapMm = maximumGapMm,
            centerToCenterMm = centerToCenter,
            usedSpanMm = usedSpan,
            remainingMm = cleanRemaining,
            isExactFit = isExact,
            isSingleRail = (railCount == 1),
            gapLimitStatus = gapStatus,
            positions = positions
        )
    }

    /**
     * Calculates the minimum number of rails N such that actual clear gap <= maximumGap.
     */
    fun calculateRailCount(
        usableSpanMm: BigDecimal,
        railWidthMm: BigDecimal,
        maximumGapMm: BigDecimal
    ): Int {
        if (usableSpanMm < railWidthMm) return 0

        // If maximumGap is 0, rails are packed edge-to-edge
        if (maximumGapMm.compareTo(BigDecimal.ZERO) == 0) {
            return usableSpanMm.divideToIntegralValue(railWidthMm).toInt()
        }

        // N = ceil((usableSpan + maximumGap) / (railWidth + maximumGap))
        val numerator = usableSpanMm.add(maximumGapMm)
        val denominator = railWidthMm.add(maximumGapMm)
        val ratio = numerator.divide(denominator, CALCULATION_SCALE, RoundingMode.HALF_UP)
        var n = ratio.setScale(0, RoundingMode.CEILING).toInt()

        // Physical constraint: N * railWidth must be <= usableSpan
        val maxPhysicalRails = usableSpanMm.divideToIntegralValue(railWidthMm).toInt()
        if (n > maxPhysicalRails) {
            n = maxPhysicalRails
        }

        return n.coerceAtLeast(1)
    }

    fun calculateClearGap(
        usableSpanMm: BigDecimal,
        railWidthMm: BigDecimal,
        railCount: Int
    ): BigDecimal {
        if (railCount <= 1) return BigDecimal.ZERO
        val gaps = BigDecimal(railCount - 1)
        val totalRailWidth = railWidthMm.multiply(BigDecimal(railCount))
        val remainingSpace = usableSpanMm.subtract(totalRailWidth)
        return remainingSpace.divide(gaps, CALCULATION_SCALE, RoundingMode.HALF_UP)
    }

    fun calculateCenterToCenter(
        railWidthMm: BigDecimal,
        clearGapMm: BigDecimal
    ): BigDecimal {
        return railWidthMm.add(clearGapMm)
    }

    fun calculateRailPositions(
        railCount: Int,
        railWidthMm: BigDecimal,
        clearGapMm: BigDecimal,
        startOffsetMm: BigDecimal
    ): List<RailPosition> {
        if (railCount <= 0) return emptyList()

        val centerToCenter = calculateCenterToCenter(railWidthMm, clearGapMm)
        val halfRail = railWidthMm.divide(BigDecimal("2"), CALCULATION_SCALE, RoundingMode.HALF_UP)
        val positions = ArrayList<RailPosition>(railCount)

        for (i in 0 until railCount) {
            val outerEdge = startOffsetMm.add(BigDecimal(i).multiply(centerToCenter))
            val center = outerEdge.add(halfRail)
            positions.add(
                RailPosition(
                    index = i + 1,
                    outerEdgePositionMm = outerEdge,
                    centerPositionMm = center
                )
            )
        }
        return positions
    }

    fun calculateUsedSpan(
        railCount: Int,
        railWidthMm: BigDecimal,
        clearGapMm: BigDecimal,
        startOffsetMm: BigDecimal,
        endOffsetMm: BigDecimal
    ): BigDecimal {
        if (railCount <= 0) return startOffsetMm.add(endOffsetMm)
        val gaps = if (railCount > 1) railCount - 1 else 0
        val railTotal = railWidthMm.multiply(BigDecimal(railCount))
        val gapTotal = clearGapMm.multiply(BigDecimal(gaps))
        return railTotal.add(gapTotal).add(startOffsetMm).add(endOffsetMm)
    }

    private fun validateBasicInputs(
        spanMm: BigDecimal,
        railWidthMm: BigDecimal,
        startOffsetMm: BigDecimal,
        endOffsetMm: BigDecimal
    ): String? {
        if (spanMm <= BigDecimal.ZERO) {
            return "Span must be greater than zero."
        }
        if (railWidthMm <= BigDecimal.ZERO) {
            return "Rail width must be greater than zero."
        }
        if (startOffsetMm < BigDecimal.ZERO || endOffsetMm < BigDecimal.ZERO) {
            return "Offsets cannot be negative."
        }
        val totalOffsets = startOffsetMm.add(endOffsetMm)
        if (totalOffsets >= spanMm) {
            return "Offsets must be smaller than the span."
        }
        return null
    }
}
