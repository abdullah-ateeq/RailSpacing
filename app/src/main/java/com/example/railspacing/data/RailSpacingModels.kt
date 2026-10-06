package com.example.railspacing.data

import java.math.BigDecimal

data class RailPosition(
    val index: Int,
    val outerEdgePositionMm: BigDecimal,
    val centerPositionMm: BigDecimal
)

enum class GapLimitStatus {
    WITHIN_MAXIMUM,
    GAP_EXCEEDS_LIMIT,
    NOT_APPLICABLE
}

data class RailSpacingInput(
    val mode: RailSpacingMode,
    val span: BigDecimal,
    val railWidth: BigDecimal,
    val railDepth: BigDecimal? = null,
    val maximumGap: BigDecimal? = null,
    val railCount: Int = 1,
    val startOffset: BigDecimal = BigDecimal.ZERO,
    val endOffset: BigDecimal = BigDecimal.ZERO,
    val unit: MeasurementUnit = MeasurementUnit.CENTIMETER
)

data class RailSpacingResult(
    val isValid: Boolean = true,
    val errorMessage: String? = null,
    val mode: RailSpacingMode = RailSpacingMode.MAX_GAP,
    val railCount: Int = 0,
    val internalGapCount: Int = 0,
    val totalSpanMm: BigDecimal = BigDecimal.ZERO,
    val startOffsetMm: BigDecimal = BigDecimal.ZERO,
    val endOffsetMm: BigDecimal = BigDecimal.ZERO,
    val usableSpanMm: BigDecimal = BigDecimal.ZERO,
    val railWidthMm: BigDecimal = BigDecimal.ZERO,
    val railDepthMm: BigDecimal? = null,
    val totalRailWidthMm: BigDecimal = BigDecimal.ZERO,
    val actualClearGapMm: BigDecimal = BigDecimal.ZERO,
    val requestedMaxGapMm: BigDecimal? = null,
    val centerToCenterMm: BigDecimal = BigDecimal.ZERO,
    val usedSpanMm: BigDecimal = BigDecimal.ZERO,
    val remainingMm: BigDecimal = BigDecimal.ZERO,
    val isExactFit: Boolean = false,
    val isSingleRail: Boolean = false,
    val gapLimitStatus: GapLimitStatus = GapLimitStatus.NOT_APPLICABLE,
    val positions: List<RailPosition> = emptyList(),
    val shortByMm: BigDecimal? = null,
    val availableMm: BigDecimal? = null,
    val requiredRailWidthMm: BigDecimal? = null
)
