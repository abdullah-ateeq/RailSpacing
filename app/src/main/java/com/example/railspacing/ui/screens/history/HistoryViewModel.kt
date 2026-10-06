package com.example.railspacing.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.railspacing.data.DataStoreManager
import com.example.railspacing.data.SavedCalculation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)

    val historyState: StateFlow<List<SavedCalculation>> = dataStoreManager.historyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun deleteCalculation(id: String) {
        viewModelScope.launch {
            dataStoreManager.deleteCalculation(id)
            _snackbarEvent.emit("Calculation deleted")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dataStoreManager.clearHistory()
            _snackbarEvent.emit("History cleared")
        }
    }
}
