package com.elasticrock.keepscreenon.data.model

sealed class KeepScreenOnState {
    data class Enabled(val currentTimeout: Int, val previousTimeout: Int): KeepScreenOnState()
    data class Disabled(val currentTimeout: Int, val maximumTimeout: Int): KeepScreenOnState()
    object PermissionNotGranted: KeepScreenOnState()
}