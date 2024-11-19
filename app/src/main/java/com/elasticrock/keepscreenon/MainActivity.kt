package com.elasticrock.keepscreenon

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.MutableLiveData
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elasticrock.keepscreenon.ui.info.InfoScreen
import com.elasticrock.keepscreenon.ui.licenses.LicensesScreen
import com.elasticrock.keepscreenon.ui.main.MainScreen
import com.elasticrock.keepscreenon.ui.theme.KeepScreenOnTheme
import com.elasticrock.keepscreenon.util.CommonUtils
import dagger.hilt.android.AndroidEntryPoint

val canWriteSettingsState = MutableLiveData(false)
val isIgnoringBatteryOptimizationState = MutableLiveData(false)
val screenTimeoutState = MutableLiveData(0)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            KeepScreenOnTheme {
                App()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        canWriteSettingsState.value = Settings.System.canWrite(applicationContext)
        isIgnoringBatteryOptimizationState.value = pm.isIgnoringBatteryOptimizations(applicationContext.packageName)
        screenTimeoutState.value = CommonUtils().readScreenTimeout(contentResolver)
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(150, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        },
        exitTransition = {
            slideOutOfContainer(
                animationSpec = tween(150, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                animationSpec = tween(150, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                animationSpec = tween(150, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        },
    ) {
        composable("main") {
            MainScreen(
                onInfoButtonClick = {
                    navController.navigate("info")
                }
            )
        }
        composable("info") {
            InfoScreen(
                onBackArrowClick = {
                    navController.navigateUp()
                },
                onLicensesOptionClick = {
                    navController.navigate("licenses")
                }
            )
        }
        composable("licenses") {
            LicensesScreen(
                onBackArrowClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}