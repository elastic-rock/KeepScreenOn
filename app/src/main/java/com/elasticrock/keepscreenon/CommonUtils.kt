package com.elasticrock.keepscreenon

import android.content.ContentResolver
import android.provider.Settings

class CommonUtils {

    private val tag = "ScreenTimeoutUtils"

    val timeoutDisabled = 2147483647

    fun readScreenTimeout(contentResolver: ContentResolver) : Int {
        return Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
    }

    fun setScreenTimeout(contentResolver: ContentResolver, screenTimeout: Int) {
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, screenTimeout)
    }
}