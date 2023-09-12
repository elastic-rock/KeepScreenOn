package com.elasticrock.keepscreenon

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.lang.Exception

data class ListenForBatteryLow(val listenForBatteryLow: Boolean)
data class ListenForScreenOff(val listenForScreenOff: Boolean)

class UserPreferencesRepository {

    private val tag = "UserPreferencesRepository"
    private val batteryFileName = "listenforbatterylow.json"
    private val screenFileName = "listenforscreenoff.json"

    fun saveListenForBatteryLow(context: Context, listenForBatteryLow: Boolean) {
        val json = Gson().toJson(ListenForBatteryLow(listenForBatteryLow))
        File(context.filesDir, batteryFileName).writeText(json)
    }

    fun readListenForBatteryLow(context: Context) : Boolean {
        return try {
            val json = File(context.filesDir, batteryFileName).readText()
            val output = Gson().fromJson(json, ListenForBatteryLow::class.java)
            output.listenForBatteryLow
        } catch (e: Exception) {
            Log.e(tag, "Error reading listenForBatteryLow property")
            false
        }
    }

    fun saveListenForScreenOff(context: Context, listenForScreenOff: Boolean) {
        val json = Gson().toJson(ListenForScreenOff(listenForScreenOff))
        File(context.filesDir, screenFileName).writeText(json)
    }

    fun readListenForScreenOff(context: Context) : Boolean {
        return try {
            val json = File(context.filesDir, screenFileName).readText()
            val output = Gson().fromJson(json, ListenForScreenOff::class.java)
            output.listenForScreenOff
        } catch (e: Exception) {
            Log.e(tag, "Error reading listenForScreenOff property")
            false
        }
    }
}