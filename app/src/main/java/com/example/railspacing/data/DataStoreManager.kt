package com.example.railspacing.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rail_spacing_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        private const val MAX_HISTORY_RECORDS = 50

        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_DEFAULT_MODE = stringPreferencesKey("default_mode")
        private val KEY_DEFAULT_UNIT = stringPreferencesKey("default_unit")
        private val KEY_DEFAULT_SPAN = stringPreferencesKey("default_span")
        private val KEY_DEFAULT_RAIL_WIDTH = stringPreferencesKey("default_rail_width")
        private val KEY_DEFAULT_MAX_GAP = stringPreferencesKey("default_max_gap")
        private val KEY_DEFAULT_START_OFFSET = stringPreferencesKey("default_start_offset")
        private val KEY_DEFAULT_END_OFFSET = stringPreferencesKey("default_end_offset")
        private val KEY_ORIENTATION = stringPreferencesKey("orientation")
        private val KEY_DECIMAL_PLACES = intPreferencesKey("decimal_places")
        private val KEY_SHOW_VISUALIZATION = booleanPreferencesKey("show_visualization")
        private val KEY_SHOW_POSITION_LIST = booleanPreferencesKey("show_position_list")
        private val KEY_SHOW_CENTER_TO_CENTER = booleanPreferencesKey("show_center_to_center")
        private val KEY_SHOW_FORMULA = booleanPreferencesKey("show_formula")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        private val KEY_HISTORY_JSON = stringPreferencesKey("history_json")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            theme = AppTheme.fromName(prefs[KEY_THEME] ?: AppTheme.SYSTEM.name),
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
            defaultMode = RailSpacingMode.fromName(prefs[KEY_DEFAULT_MODE] ?: RailSpacingMode.MAX_GAP.name),
            defaultUnit = MeasurementUnit.fromId(prefs[KEY_DEFAULT_UNIT] ?: "cm"),
            defaultSpan = prefs[KEY_DEFAULT_SPAN] ?: "240",
            defaultRailWidth = prefs[KEY_DEFAULT_RAIL_WIDTH] ?: "4",
            defaultMaxGap = prefs[KEY_DEFAULT_MAX_GAP] ?: "10",
            defaultStartOffset = prefs[KEY_DEFAULT_START_OFFSET] ?: "0",
            defaultEndOffset = prefs[KEY_DEFAULT_END_OFFSET] ?: "0",
            orientation = Orientation.fromName(prefs[KEY_ORIENTATION] ?: Orientation.HORIZONTAL.name),
            decimalPlaces = prefs[KEY_DECIMAL_PLACES] ?: 2,
            showVisualization = prefs[KEY_SHOW_VISUALIZATION] ?: true,
            showPositionList = prefs[KEY_SHOW_POSITION_LIST] ?: true,
            showCenterToCenter = prefs[KEY_SHOW_CENTER_TO_CENTER] ?: true,
            showFormula = prefs[KEY_SHOW_FORMULA] ?: false,
            hapticFeedback = prefs[KEY_HAPTIC_FEEDBACK] ?: true
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = settings.theme.name
            prefs[KEY_DYNAMIC_COLOR] = settings.dynamicColor
            prefs[KEY_DEFAULT_MODE] = settings.defaultMode.name
            prefs[KEY_DEFAULT_UNIT] = settings.defaultUnit.id
            prefs[KEY_DEFAULT_SPAN] = settings.defaultSpan
            prefs[KEY_DEFAULT_RAIL_WIDTH] = settings.defaultRailWidth
            prefs[KEY_DEFAULT_MAX_GAP] = settings.defaultMaxGap
            prefs[KEY_DEFAULT_START_OFFSET] = settings.defaultStartOffset
            prefs[KEY_DEFAULT_END_OFFSET] = settings.defaultEndOffset
            prefs[KEY_ORIENTATION] = settings.orientation.name
            prefs[KEY_DECIMAL_PLACES] = settings.decimalPlaces
            prefs[KEY_SHOW_VISUALIZATION] = settings.showVisualization
            prefs[KEY_SHOW_POSITION_LIST] = settings.showPositionList
            prefs[KEY_SHOW_CENTER_TO_CENTER] = settings.showCenterToCenter
            prefs[KEY_SHOW_FORMULA] = settings.showFormula
            prefs[KEY_HAPTIC_FEEDBACK] = settings.hapticFeedback
        }
    }

    suspend fun resetSettings() {
        val default = AppSettings()
        updateSettings(default)
    }

    val historyFlow: Flow<List<SavedCalculation>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_HISTORY_JSON]
        SavedCalculation.listFromJsonString(json)
    }

    suspend fun saveCalculation(calculation: SavedCalculation) {
        context.dataStore.edit { prefs ->
            val existing = SavedCalculation.listFromJsonString(prefs[KEY_HISTORY_JSON]).toMutableList()
            // Add new at the beginning
            existing.add(0, calculation)
            // Limit to max records (remove oldest at the end)
            if (existing.size > MAX_HISTORY_RECORDS) {
                while (existing.size > MAX_HISTORY_RECORDS) {
                    existing.removeAt(existing.size - 1)
                }
            }
            prefs[KEY_HISTORY_JSON] = SavedCalculation.listToJsonString(existing)
        }
    }

    suspend fun deleteCalculation(id: String) {
        context.dataStore.edit { prefs ->
            val existing = SavedCalculation.listFromJsonString(prefs[KEY_HISTORY_JSON]).toMutableList()
            existing.removeAll { it.id == id }
            prefs[KEY_HISTORY_JSON] = SavedCalculation.listToJsonString(existing)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_HISTORY_JSON)
        }
    }
}
