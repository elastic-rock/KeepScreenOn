package com.elasticrock.keepscreenon.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elasticrock.keepscreenon.data.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainScreenState(
    val isRestoreWhenBatteryLowEnabled: Boolean = false,
    val isRestoreWhenScreenOffEnabled: Boolean = false,
    val maxTimeout: Int = Int.MAX_VALUE,
    val isTileAdded: Boolean = false
)

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
): ViewModel() {
    private val _isRestoreWhenBatteryLowEnabled = preferencesRepository.listenForBatteryLow
    private val _isRestoreWhenScreenOffEnabled = preferencesRepository.listenForScreenOff
    private val _maxTimeout = preferencesRepository.maximumTimeout
    private val _isTileAdded = preferencesRepository.isTileAdded

    private val _state = MutableStateFlow(MainScreenState())
    val state = combine(_state, _isRestoreWhenBatteryLowEnabled, _isRestoreWhenScreenOffEnabled, _maxTimeout, _isTileAdded) { state, isRestoreWhenBatteryLowEnabled, isRestoreWhenScreenOffEnabled, maxTimeout, isTileAdded ->
        state.copy(
            isRestoreWhenBatteryLowEnabled = isRestoreWhenBatteryLowEnabled,
            isRestoreWhenScreenOffEnabled = isRestoreWhenScreenOffEnabled,
            maxTimeout = maxTimeout,
            isTileAdded = isTileAdded
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenState())

    fun onRestoreWhenBatteryLowChange(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveListenForBatteryLow(value)
        }
    }

    fun onRestoreWhenScreenOffChange(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveListenForScreenOff(value)
        }
    }

    fun onMaxTimeoutChange(value: Int) {
        viewModelScope.launch {
            preferencesRepository.saveMaximumTimeout(value)
        }
    }
}