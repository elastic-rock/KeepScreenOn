package com.elasticrock.keepscreenon

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class CommonUtils {

    private val tag = "ScreenTimeoutUtils"

    val modifySystemSettingsIntent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply { data = Uri.parse("package:com.elasticrock.keepscreenon") }

    fun readScreenTimeout(contentResolver: ContentResolver) : Int {
        return Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
    }

    fun setScreenTimeout(contentResolver: ContentResolver, screenTimeout: Int) {
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, screenTimeout)
    }
}