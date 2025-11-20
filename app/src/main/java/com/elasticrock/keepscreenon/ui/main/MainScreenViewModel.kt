package com.elasticrock.keepscreenon.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elasticrock.keepscreenon.data.repository.PermissionsRepository
import com.elasticrock.keepscreenon.data.repository.PreferencesRepository
import com.elasticrock.keepscreenon.data.repository.ScreenTimeoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val permissionsRepository: PermissionsRepository,
    private val screenTimeoutRepository: ScreenTimeoutRepository
): ViewModel() {
    private val _isRestoreWhenBatteryLowEnabled = preferencesRepository.listenForBatteryLow
    private val _isRestoreWhenScreenOffEnabled = preferencesRepository.listenForScreenOff
    private val _maxTimeout = preferencesRepository.maximumTimeout
    private val _isTileAdded = preferencesRepository.isTileAdded
    private val _openCount = preferencesRepository.openCount
    private val _displayReviewPrompt = MutableStateFlow(false)
    private val _isNotificationPermissionDeniedPermanently = permissionsRepository.isNotificationPermissionDeniedPermanently
    private val _previousScreenTimeout = screenTimeoutRepository.previousScreenTimeout
    private val _canWriteSystemSettings = permissionsRepository.canWriteSystemSettings
    private val _isIgnoringBatteryOptimizations = permissionsRepository.isIgnoringBatteryOptimizations
    private val _isNotificationPermissionGranted = permissionsRepository.isNotificationPermissionGranted
    private val _currentScreenTimeout = screenTimeoutRepository.currentScreenTimeout

    init {
        viewModelScope.launch {
            preferencesRepository.incrementOpenCount()
            _openCount.collect { openCount ->
                if (openCount >= 10) {
                    _displayReviewPrompt.value = true
                }
            }
        }
    }

    private val _state = MutableStateFlow(MainScreenState())
    val state = combine(
        _state,
        _isRestoreWhenBatteryLowEnabled,
        _isRestoreWhenScreenOffEnabled,
        _maxTimeout,
        _isTileAdded,
        _displayReviewPrompt,
        _isNotificationPermissionDeniedPermanently,
        _previousScreenTimeout,
        _canWriteSystemSettings,
        _isIgnoringBatteryOptimizations,
        _isNotificationPermissionGranted,
        _currentScreenTimeout
    ) { flowArray ->
        val state = flowArray[0] as MainScreenState
        val isRestoreWhenBatteryLowEnabled = flowArray[1] as Boolean
        val isRestoreWhenScreenOffEnabled = flowArray[2] as Boolean
        val maxTimeout = flowArray[3] as Int
        val isTileAdded = flowArray[4] as Boolean
        val displayReviewPrompt = flowArray[5] as Boolean
        val isNotificationPermissionDeniedPermanently = flowArray[6] as Boolean
        val previousScreenTimeout = flowArray[7] as Int
        val canWriteSystemSettings = flowArray[8] as Boolean
        val isIgnoringBatteryOptimizations = flowArray[9] as Boolean
        val isNotificationPermissionGranted = flowArray[10] as Boolean
        val currentScreenTimeout = flowArray[11] as Int
        state.copy(
            isRestoreWhenBatteryLowEnabled = isRestoreWhenBatteryLowEnabled,
            isRestoreWhenScreenOffEnabled = isRestoreWhenScreenOffEnabled,
            maxTimeout = maxTimeout,
            isTileAdded = isTileAdded,
            displayReviewPrompt = displayReviewPrompt,
            isNotificationPermissionDeniedPermanently = isNotificationPermissionDeniedPermanently,
            previousScreenTimeout = previousScreenTimeout,
            canWriteSystemSettings = canWriteSystemSettings,
            isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
            isNotificationPermissionGranted = isNotificationPermissionGranted,
            currentScreenTimeout = currentScreenTimeout
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

    fun onNotificationDeniedPermanentlyChange(value: Boolean) {
        viewModelScope.launch {
            permissionsRepository.saveIsNotificationPermissionDeniedPermanently(value)
        }
    }

    fun onNotificationPermissionGranted(value: Boolean) {
        permissionsRepository.updateIsNotificationPermissionGranted(value)
    }

    fun onKeepScreenOnDisabled(context: Context) {
        viewModelScope.launch {
            screenTimeoutRepository.disableKeepScreenOn(context)
        }
    }

    fun onKeepScreenOnEnabled(context: Context) {
        viewModelScope.launch {
            screenTimeoutRepository.enableKeepScreenOn(context)
        }
    }
}