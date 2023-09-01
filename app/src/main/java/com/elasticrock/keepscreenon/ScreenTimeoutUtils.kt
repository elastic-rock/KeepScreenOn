package com.elasticrock.keepscreenon

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import android.util.Log
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.Exception

data class ScreenTimeout(val screenTimeout: Int)

class ScreenTimeoutUtils {
    private val tag = "ScreenTimeoutUtils"
    private val fileName = "screentimeout.json"

    fun isScreenTimeoutDisabled(contentResolver: ContentResolver) : Boolean {
        return readScreenTimeout(contentResolver) == 2147483647
    }

    fun readScreenTimeout(contentResolver: ContentResolver) : Int {
        return Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
    }

    fun restoreScreenTimeout(context: Context, contentResolver: ContentResolver) {
        setScreenTimeout(contentResolver, readPreviousScreenTimeout(context))
    }

    fun disableScreenTimeout(context: Context, contentResolver: ContentResolver) {
        val previousScreenTimeout = readScreenTimeout(contentResolver)
        runBlocking {
            launch { setScreenTimeout(contentResolver, 2147483647) }
            launch { saveScreenTimeout(context, previousScreenTimeout) }
        }
    }



    private fun setScreenTimeout(contentResolver: ContentResolver, screenTimeout: Int) {
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, screenTimeout)
    }

    private fun saveScreenTimeout(context: Context, screenTimeout: Int) {
        val json = Gson().toJson(ScreenTimeout(screenTimeout))
        File(context.filesDir,fileName).writeText(json)
    }

    private fun readPreviousScreenTimeout(context: Context) : Int {
        return try {
            val json = File(context.filesDir,fileName).readText()
            val output = Gson().fromJson(json, ScreenTimeout::class.java)
            output.screenTimeout
        } catch (e: Exception) {
            Log.e(tag, "Error reading previous screen timeout")
            120000
        }
    }
}