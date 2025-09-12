package com.elasticrock.keepscreenon

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elasticrock.keepscreenon.ui.donate.DonateScreen
import com.elasticrock.keepscreenon.ui.info.InfoScreen
import com.elasticrock.keepscreenon.ui.licenses.LicensesScreen
import com.elasticrock.keepscreenon.ui.main.MainScreen
import com.elasticrock.keepscreenon.ui.theme.KeepScreenOnTheme
import com.elasticrock.keepscreenon.util.CommonUtils
import com.elasticrock.keepscreenon.util.notificationPermission
import dagger.hilt.android.AndroidEntryPoint

val canWriteSettingsState = MutableLiveData(false)
val isIgnoringBatteryOptimizationState = MutableLiveData(false)
val screenTimeoutState = MutableLiveData(0)
val isNotificationPermissionGranted = MutableLiveData(false)

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
        val pm = applicationContext.getSystemService(POWER_SERVICE) as PowerManager
        canWriteSettingsState.value = Settings.System.canWrite(applicationContext)
        isIgnoringBatteryOptimizationState.value = pm.isIgnoringBatteryOptimizations(applicationContext.packageName)
        screenTimeoutState.value = CommonUtils().readScreenTimeout(contentResolver)
        isNotificationPermissionGranted.value = ContextCompat.checkSelfPermission(this, notificationPermission) == PERMISSION_GRANTED
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    150, easing = LinearEasing
                )
            ) + slideIntoContainer(
                animationSpec = tween(150, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        },
        exitTransition = {
            ExitTransition.None
        },
        popEnterTransition = {
            EnterTransition.None
        },
        popExitTransition = {
            fadeOut(
                animationSpec = tween(
                    150, easing = LinearEasing
                )
            ) + slideOutOfContainer(
                animationSpec = tween(150, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        }
        ) {
        composable("main") {
            MainScreen(
                onInfoButtonClick = {
                    navController.navigate("info")
                },
                onDonateButtonClick = {
                    navController.navigate("donate")
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
        composable("donate") {
            DonateScreen(
                onBackArrowClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}