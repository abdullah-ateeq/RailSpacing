package com.example.railspacing.ui.screens.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.railspacing.calculator.RailSpacingCalculator
import com.example.railspacing.calculator.UnitConverter
import com.example.railspacing.data.AppSettings
import com.example.railspacing.data.DataStoreManager
import com.example.railspacing.data.MeasurementUnit
import com.example.railspacing.data.Orientation
import com.example.railspacing.data.RailPosition
import com.example.railspacing.data.RailSpacingMode
import com.example.railspacing.data.RailSpacingResult
import com.example.railspacing.data.SavedCalculation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

data class CalculatorUiState(
    val mode: RailSpacingMode = RailSpacingMode.MAX_GAP,
    val unit: MeasurementUnit = MeasurementUnit.CENTIMETER,
    val orientation: Orientation = Orientation.HORIZONTAL,
    val spanInput: String = "240",
    val railWidthInput: String = "4",
    val railDepthInput: String = "",
    val maxGapInput: String = "10",
    val railCountInput: String = "10",
    val startOffsetInput: String = "0",
    val endOffsetInput: String = "0",
    val maxGapCheckEnabledInRailCount: Boolean = false,
    val result: RailSpacingResult = RailSpacingResult(),
    val decimalPlaces: Int = 2,
    val showVisualization: Boolean = true,
    val showPositionList: Boolean = true,
    val showCenterToCenter: Boolean = true,
    val showFormula: Boolean = false,
    val hapticFeedback: Boolean = true,
    val isBreakdownExpanded: Boolean = false,
    val inputError: String? = null
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _hapticEvent = MutableSharedFlow<Unit>()
    val hapticEvent: SharedFlow<Unit> = _hapticEvent.asSharedFlow()

    private var appSettings: AppSettings = AppSettings()

    init {
        viewModelScope.launch {
            dataStoreManager.settingsFlow.collectLatest { settings ->
                appSettings = settings
                _uiState.value = _uiState.value.copy(
                    decimalPlaces = settings.decimalPlaces,
                    showVisualization = settings.showVisualization,
                    showPositionList = settings.showPositionList,
                    showCenterToCenter = settings.showCenterToCenter,
                    showFormula = settings.showFormula,
                    hapticFeedback = settings.hapticFeedback
                )
            }
        }
        recalculate()
    }

    private fun triggerHaptic() {
        if (_uiState.value.hapticFeedback) {
            viewModelScope.launch {
                _hapticEvent.emit(Unit)
            }
        }
    }

    fun setMode(newMode: RailSpacingMode) {
        if (_uiState.value.mode != newMode) {
            _uiState.value = _uiState.value.copy(mode = newMode)
            triggerHaptic()
            recalculate()
        }
    }

    fun setUnit(newUnit: MeasurementUnit) {
        val oldUnit = _uiState.value.unit
        if (oldUnit == newUnit) return

        fun convertValue(text: String): String {
            val v = UnitConverter.parseInput(text) ?: return text
            val mm = UnitConverter.toMillimeters(v, oldUnit)
            val converted = UnitConverter.fromMillimeters(mm, newUnit)
            return converted.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        }

        _uiState.value = _uiState.value.copy(
            unit = newUnit,
            spanInput = convertValue(_uiState.value.spanInput),
            railWidthInput = convertValue(_uiState.value.railWidthInput),
            railDepthInput = if (_uiState.value.railDepthInput.isEmpty()) "" else convertValue(_uiState.value.railDepthInput),
            maxGapInput = convertValue(_uiState.value.maxGapInput),
            startOffsetInput = convertValue(_uiState.value.startOffsetInput),
            endOffsetInput = convertValue(_uiState.value.endOffsetInput)
        )
        recalculate()
    }

    fun setOrientation(orientation: Orientation) {
        _uiState.value = _uiState.value.copy(orientation = orientation)
    }

    fun updateSpan(value: String) {
        _uiState.value = _uiState.value.copy(spanInput = value)
        recalculate()
    }

    fun updateRailWidth(value: String) {
        _uiState.value = _uiState.value.copy(railWidthInput = value)
        recalculate()
    }

    fun updateRailDepth(value: String) {
        _uiState.value = _uiState.value.copy(railDepthInput = value)
        recalculate()
    }

    fun updateMaxGap(value: String) {
        _uiState.value = _uiState.value.copy(maxGapInput = value)
        recalculate()
    }

    fun updateRailCount(value: String) {
        _uiState.value = _uiState.value.copy(railCountInput = value)
        recalculate()
    }

    fun updateStartOffset(value: String) {
        _uiState.value = _uiState.value.copy(startOffsetInput = value)
        recalculate()
    }

    fun updateEndOffset(value: String) {
        _uiState.value = _uiState.value.copy(endOffsetInput = value)
        recalculate()
    }

    fun toggleMaxGapCheck(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(maxGapCheckEnabledInRailCount = enabled)
        recalculate()
    }

    fun toggleBreakdownExpanded() {
        _uiState.value = _uiState.value.copy(isBreakdownExpanded = !_uiState.value.isBreakdownExpanded)
    }

    fun applyQuickGap(gapInCm: Double) {
        val gapMm = BigDecimal(gapInCm.toString()).multiply(BigDecimal("10"))
        val inCurrentUnit = UnitConverter.fromMillimeters(gapMm, _uiState.value.unit)
        val text = inCurrentUnit.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        _uiState.value = _uiState.value.copy(maxGapInput = text)
        triggerHaptic()
        recalculate()
    }

    fun resetToDefaults() {
        _uiState.value = _uiState.value.copy(
            mode = appSettings.defaultMode,
            unit = appSettings.defaultUnit,
            orientation = appSettings.orientation,
            spanInput = appSettings.defaultSpan,
            railWidthInput = appSettings.defaultRailWidth,
            railDepthInput = "",
            maxGapInput = appSettings.defaultMaxGap,
            railCountInput = "10",
            startOffsetInput = appSettings.defaultStartOffset,
            endOffsetInput = appSettings.defaultEndOffset,
            maxGapCheckEnabledInRailCount = false
        )
        triggerHaptic()
        recalculate()
        viewModelScope.launch {
            _snackbarEvent.emit("Reset to default layout values")
        }
    }

    fun restoreCalculation(saved: SavedCalculation) {
        _uiState.value = _uiState.value.copy(
            mode = saved.mode,
            unit = saved.unit,
            spanInput = saved.span.toPlainString(),
            railWidthInput = saved.railWidth.toPlainString(),
            railDepthInput = saved.railDepth?.toPlainString() ?: "",
            maxGapInput = saved.maximumGap?.toPlainString() ?: "10",
            railCountInput = saved.railCount.toString(),
            startOffsetInput = saved.startOffset.toPlainString(),
            endOffsetInput = saved.endOffset.toPlainString(),
            maxGapCheckEnabledInRailCount = (saved.mode == RailSpacingMode.RAIL_COUNT && saved.maximumGap != null)
        )
        triggerHaptic()
        recalculate()
        viewModelScope.launch {
            _snackbarEvent.emit("Restored saved calculation")
        }
    }

    fun saveCurrentCalculation() {
        val state = _uiState.value
        val res = state.result
        if (!res.isValid || res.railCount <= 0) {
            viewModelScope.launch {
                _snackbarEvent.emit("Cannot save invalid layout calculation")
            }
            return
        }

        val spanVal = UnitConverter.parseInput(state.spanInput) ?: BigDecimal.ZERO
        val railWidthVal = UnitConverter.parseInput(state.railWidthInput) ?: BigDecimal.ZERO
        val railDepthVal = UnitConverter.parseInput(state.railDepthInput)
        val maxGapVal = UnitConverter.parseInput(state.maxGapInput)
        val startOffsetVal = UnitConverter.parseInput(state.startOffsetInput) ?: BigDecimal.ZERO
        val endOffsetVal = UnitConverter.parseInput(state.endOffsetInput) ?: BigDecimal.ZERO

        val actualGapInUnit = UnitConverter.fromMillimeters(res.actualClearGapMm, state.unit)
        val c2cInUnit = UnitConverter.fromMillimeters(res.centerToCenterMm, state.unit)

        val saved = SavedCalculation(
            mode = state.mode,
            span = spanVal,
            unit = state.unit,
            railWidth = railWidthVal,
            railDepth = railDepthVal,
            maximumGap = if (state.mode == RailSpacingMode.MAX_GAP || state.maxGapCheckEnabledInRailCount) maxGapVal else null,
            railCount = res.railCount,
            startOffset = startOffsetVal,
            endOffset = endOffsetVal,
            actualGap = actualGapInUnit,
            centerToCenter = c2cInUnit
        )

        viewModelScope.launch {
            dataStoreManager.saveCalculation(saved)
            triggerHaptic()
            _snackbarEvent.emit("Calculation saved to history")
        }
    }

    fun generateResultCopyText(): String {
        val state = _uiState.value
        val res = state.result
        val unit = state.unit
        val dec = state.decimalPlaces

        val span = UnitConverter.formatMeasurement(res.totalSpanMm, unit, dec)
        val start = UnitConverter.formatMeasurement(res.startOffsetMm, unit, dec)
        val end = UnitConverter.formatMeasurement(res.endOffsetMm, unit, dec)
        val railWidth = UnitConverter.formatMeasurement(res.railWidthMm, unit, dec)
        val clearGap = UnitConverter.formatMeasurement(res.actualClearGapMm, unit, dec)
        val c2c = UnitConverter.formatMeasurement(res.centerToCenterMm, unit, dec)
        val remaining = UnitConverter.formatMeasurement(res.remainingMm, unit, dec)

        return buildString {
            appendLine("RailSpacing")
            appendLine("Span: $span")
            appendLine("Start offset: $start")
            appendLine("End offset: $end")
            appendLine("Rail width: $railWidth")
            appendLine("Rails: ${res.railCount}")
            appendLine("Clear gap: $clearGap")
            appendLine("Center-to-center: $c2c")
            append("Remaining: $remaining")
        }
    }

    fun generatePositionsCopyText(): String {
        val state = _uiState.value
        val res = state.result
        val unit = state.unit
        val dec = state.decimalPlaces

        if (res.positions.isEmpty()) return "No positions available"

        return buildString {
            appendLine("Rail Positions (Outer Edge & Center):")
            res.positions.forEach { pos ->
                val outer = UnitConverter.formatMeasurement(pos.outerEdgePositionMm, unit, dec)
                val center = UnitConverter.formatMeasurement(pos.centerPositionMm, unit, dec)
                appendLine("Rail ${pos.index}: $outer (Center: $center)")
            }
        }.trimEnd()
    }

    private fun recalculate() {
        val state = _uiState.value
        val unit = state.unit

        val spanParsed = UnitConverter.parseInput(state.spanInput)
        val railWidthParsed = UnitConverter.parseInput(state.railWidthInput)
        val railDepthParsed = UnitConverter.parseInput(state.railDepthInput)
        val startOffsetParsed = UnitConverter.parseInput(state.startOffsetInput) ?: BigDecimal.ZERO
        val endOffsetParsed = UnitConverter.parseInput(state.endOffsetInput) ?: BigDecimal.ZERO

        if (spanParsed == null || railWidthParsed == null) {
            _uiState.value = state.copy(
                result = RailSpacingResult(isValid = false, errorMessage = "Please enter valid numeric dimensions."),
                inputError = "Invalid input"
            )
            return
        }

        val spanMm = UnitConverter.toMillimeters(spanParsed, unit)
        val railWidthMm = UnitConverter.toMillimeters(railWidthParsed, unit)
        val railDepthMm = railDepthParsed?.let { UnitConverter.toMillimeters(it, unit) }
        val startOffsetMm = UnitConverter.toMillimeters(startOffsetParsed, unit)
        val endOffsetMm = UnitConverter.toMillimeters(endOffsetParsed, unit)

        val result = if (state.mode == RailSpacingMode.MAX_GAP) {
            val maxGapParsed = UnitConverter.parseInput(state.maxGapInput)
            if (maxGapParsed == null) {
                RailSpacingResult(isValid = false, errorMessage = "Please enter a valid maximum gap.")
            } else {
                val maxGapMm = UnitConverter.toMillimeters(maxGapParsed, unit)
                RailSpacingCalculator.calculateMaxGapLayout(
                    spanMm = spanMm,
                    railWidthMm = railWidthMm,
                    maximumGapMm = maxGapMm,
                    startOffsetMm = startOffsetMm,
                    endOffsetMm = endOffsetMm,
                    railDepthMm = railDepthMm
                )
            }
        } else {
            val railCountParsed = state.railCountInput.trim().toIntOrNull()
            if (railCountParsed == null) {
                RailSpacingResult(isValid = false, errorMessage = "Please enter an integer rail count.")
            } else {
                val maxGapMm = if (state.maxGapCheckEnabledInRailCount) {
                    UnitConverter.parseInput(state.maxGapInput)?.let { UnitConverter.toMillimeters(it, unit) }
                } else null

                RailSpacingCalculator.calculateRailCountLayout(
                    spanMm = spanMm,
                    railWidthMm = railWidthMm,
                    railCount = railCountParsed,
                    startOffsetMm = startOffsetMm,
                    endOffsetMm = endOffsetMm,
                    railDepthMm = railDepthMm,
                    maxGapCheckEnabled = state.maxGapCheckEnabledInRailCount,
                    maximumGapMm = maxGapMm
                )
            }
        }

        _uiState.value = state.copy(
            result = result,
            inputError = result.errorMessage
        )
    }
}
