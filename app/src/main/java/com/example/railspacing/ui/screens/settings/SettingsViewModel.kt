package com.example.railspacing.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.railspacing.data.AppSettings
import com.example.railspacing.data.AppTheme
import com.example.railspacing.data.DataStoreManager
import com.example.railspacing.data.MeasurementUnit
import com.example.railspacing.data.Orientation
import com.example.railspacing.data.RailSpacingMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)

    val settingsState: StateFlow<AppSettings> = dataStoreManager.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun updateTheme(theme: AppTheme) {
        update { it.copy(theme = theme) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        update { it.copy(dynamicColor = enabled) }
    }

    fun updateDefaultMode(mode: RailSpacingMode) {
        update { it.copy(defaultMode = mode) }
    }

    fun updateDefaultUnit(unit: MeasurementUnit) {
        update { it.copy(defaultUnit = unit) }
    }

    fun updateDefaultSpan(span: String) {
        update { it.copy(defaultSpan = span) }
    }

    fun updateDefaultRailWidth(width: String) {
        update { it.copy(defaultRailWidth = width) }
    }

    fun updateDefaultMaxGap(gap: String) {
        update { it.copy(defaultMaxGap = gap) }
    }

    fun updateDefaultStartOffset(offset: String) {
        update { it.copy(defaultStartOffset = offset) }
    }

    fun updateDefaultEndOffset(offset: String) {
        update { it.copy(defaultEndOffset = offset) }
    }

    fun updateOrientation(orientation: Orientation) {
        update { it.copy(orientation = orientation) }
    }

    fun updateDecimalPlaces(places: Int) {
        update { it.copy(decimalPlaces = places) }
    }

    fun updateShowVisualization(show: Boolean) {
        update { it.copy(showVisualization = show) }
    }

    fun updateShowPositionList(show: Boolean) {
        update { it.copy(showPositionList = show) }
    }

    fun updateShowCenterToCenter(show: Boolean) {
        update { it.copy(showCenterToCenter = show) }
    }

    fun updateShowFormula(show: Boolean) {
        update { it.copy(showFormula = show) }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        update { it.copy(hapticFeedback = enabled) }
    }

    fun resetSettings() {
        viewModelScope.launch {
            dataStoreManager.resetSettings()
            _snackbarEvent.emit("Settings reset to defaults")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dataStoreManager.clearHistory()
            _snackbarEvent.emit("History cleared")
        }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val current = settingsState.value
            val updated = transform(current)
            dataStoreManager.updateSettings(updated)
        }
    }
}
