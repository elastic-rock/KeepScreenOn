package com.elasticrock.keepscreenon.ui.main

import com.elasticrock.keepscreenon.data.model.KeepScreenOnState

data class MainScreenState(
    val isRestoreWhenBatteryLowEnabled: Boolean = false,
    val isRestoreWhenScreenOffEnabled: Boolean = false,
    val maxTimeout: Int = 600000,
    val isTileAdded: Boolean = false,
    val displayReviewPrompt: Boolean = false,
    val isNotificationPermissionDeniedPermanently: Boolean = false,
    val canWriteSystemSettings: Boolean = false,
    val isIgnoringBatteryOptimizations: Boolean = false,
    val isNotificationPermissionGranted: Boolean = false,
    val currentScreenTimeout: Int = 0,
    val keepScreenOnState: KeepScreenOnState = KeepScreenOnState.Disabled(0,0),
    val addTileDialogOpen: Boolean = false
)
