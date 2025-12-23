package com.elasticrock.keepscreenon.data.repository

import android.content.Context
import android.provider.Settings
import com.elasticrock.keepscreenon.data.source.PreferencesDataStoreSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenTimeoutRepository @Inject constructor(
    private val preferencesDataStoreSource: PreferencesDataStoreSource,
    @ApplicationContext private val context: Context
) {
    suspend fun savePreviousScreenTimeout(value: Int) {
        preferencesDataStoreSource.save(preferencesDataStoreSource.previousScreenTimeoutKey, value)
    }

    val previousScreenTimeout = preferencesDataStoreSource.getFlow(preferencesDataStoreSource.previousScreenTimeoutKey).map { value ->
        value ?: 120000
    }

    val currentScreenTimeout = flow {
        while (true) {
            emit(Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT))
        }
    }

    fun setScreenTimeout(screenTimeout: Int) {
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, screenTimeout)
    }
}