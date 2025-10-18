package com.elasticrock.keepscreenon

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.elasticrock.keepscreenon.data.preferences.PreferencesRepository
import com.elasticrock.keepscreenon.di.dataStore
import com.elasticrock.keepscreenon.util.CommonUtils
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutputOrInput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutputOrInput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigNoInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class EnableKeepScreenOnHelper(config: TaskerPluginConfig<Unit>) : TaskerPluginConfigHelperNoOutputOrInput<EnableKeepScreenOnRunner>(config) {
    override val runnerClass: Class<EnableKeepScreenOnRunner> get() = EnableKeepScreenOnRunner::class.java
    override fun addToStringBlurb(input: TaskerInput<Unit>, blurbBuilder: StringBuilder) {
        blurbBuilder.append("Enables Keep Screen On")
    }
}

class ActivityConfigEnableKeepScreenOn : Activity(), TaskerPluginConfigNoInput {
    override val context: Context get() = applicationContext
    private val taskerHelper by lazy { EnableKeepScreenOnHelper(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerHelper.finishForTasker()
    }
}

class EnableKeepScreenOnRunner : TaskerPluginRunnerActionNoOutputOrInput() {
    override fun run(context: Context, input: TaskerInput<Unit>): TaskerPluginResult<Unit> {
        Handler(Looper.getMainLooper()).post {
            runBlocking {
                val maximumTimeout = PreferencesRepository(context.dataStore).maximumTimeout.first()
                val currentScreenTimeout = CommonUtils().readScreenTimeout(context.contentResolver)
                if (maximumTimeout != currentScreenTimeout) {
                    CommonUtils().setScreenTimeout(context.contentResolver, maximumTimeout)
                    PreferencesRepository(context.dataStore).savePreviousScreenTimeout(currentScreenTimeout)
                    CommonUtils().startBroadcastReceiverService(context)
                } else {
                    Toast.makeText(context, context.getString(R.string.keep_screen_on_already_enabled), Toast.LENGTH_LONG).show()
                }
            }
        }
        return TaskerPluginResultSucess()
    }
}

class DisableKeepScreenOnHelper(config: TaskerPluginConfig<Unit>) : TaskerPluginConfigHelperNoOutputOrInput<DisableKeepScreenOnRunner>(config) {
    override val runnerClass: Class<DisableKeepScreenOnRunner> get() = DisableKeepScreenOnRunner::class.java
    override fun addToStringBlurb(input: TaskerInput<Unit>, blurbBuilder: StringBuilder) {
        blurbBuilder.append("Disables Keep Screen On")
    }
}

class ActivityConfigDisableKeepScreenOn : Activity(), TaskerPluginConfigNoInput {
    override val context: Context get() = applicationContext
    private val taskerHelper by lazy { DisableKeepScreenOnHelper(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerHelper.finishForTasker()
    }
}

class DisableKeepScreenOnRunner : TaskerPluginRunnerActionNoOutputOrInput() {
    override fun run(context: Context, input: TaskerInput<Unit>): TaskerPluginResult<Unit> {
        Handler(Looper.getMainLooper()).post {
            runBlocking {
                val maximumTimeout = PreferencesRepository(context.dataStore).maximumTimeout.first()
                val currentScreenTimeout = CommonUtils().readScreenTimeout(context.contentResolver)
                if (maximumTimeout == currentScreenTimeout) {
                    val previousScreenTimeout = PreferencesRepository(context.dataStore).previousScreenTimeout.first()
                    CommonUtils().setScreenTimeout(context.contentResolver, previousScreenTimeout)
                    context.stopService(Intent(context, BroadcastReceiverService::class.java))
                    screenTimeoutState.value = CommonUtils().readScreenTimeout(context.contentResolver)
                } else {
                    Toast.makeText(context, context.getString(R.string.keep_screen_on_already_disabled), Toast.LENGTH_LONG).show()
                }
            }
        }
        return TaskerPluginResultSucess()
    }
}