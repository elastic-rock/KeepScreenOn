package com.elasticrock.keepscreenon.tasker

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.elasticrock.keepscreenon.R
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutputOrInput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutputOrInput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigNoInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking

class EnableKeepScreenOnHelper(config: TaskerPluginConfig<Unit>) : TaskerPluginConfigHelperNoOutputOrInput<EnableKeepScreenOnRunner>(config) {
    override val runnerClass: Class<EnableKeepScreenOnRunner> get() = EnableKeepScreenOnRunner::class.java
    override fun addToStringBlurb(input: TaskerInput<Unit>, blurbBuilder: StringBuilder) {
        blurbBuilder.append(context.getString(R.string.enables_keep_screen_on))
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
            val hiltEntryPoint = EntryPointAccessors.fromApplication(context, TaskerEntryPoint::class.java)
            val keepScreenOnRepository = hiltEntryPoint.keepScreenOnRepository()
            runBlocking {
                keepScreenOnRepository.enableKeepScreenOn()
            }
        }
        return TaskerPluginResultSucess()
    }
}

class DisableKeepScreenOnHelper(config: TaskerPluginConfig<Unit>) : TaskerPluginConfigHelperNoOutputOrInput<DisableKeepScreenOnRunner>(config) {
    override val runnerClass: Class<DisableKeepScreenOnRunner> get() = DisableKeepScreenOnRunner::class.java

    override fun addToStringBlurb(input: TaskerInput<Unit>, blurbBuilder: StringBuilder) {
        blurbBuilder.append(context.getString(R.string.disables_keep_screen_on))
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
            val hiltEntryPoint = EntryPointAccessors.fromApplication(context, TaskerEntryPoint::class.java)
            val keepScreenOnRepository = hiltEntryPoint.keepScreenOnRepository()
            runBlocking {
                keepScreenOnRepository.disableKeepScreenOn()
            }
        }
        return TaskerPluginResultSucess()
    }
}