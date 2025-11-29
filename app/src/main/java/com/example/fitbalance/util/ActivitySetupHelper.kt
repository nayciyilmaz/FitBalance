package com.example.fitbalance.util

import android.app.Activity
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import com.example.fitbalance.notification.NotificationScheduler

object ActivitySetupHelper {

    fun setupWindowInsets(activity: Activity) {
        activity.window.statusBarColor = Color.Transparent.toArgb()
        activity.window.navigationBarColor = Color.Transparent.toArgb()
        activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    fun setupPermissionManager(
        activity: Activity,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ): PermissionManager {
        return PermissionManager(
            activity = activity,
            requestPermissionLauncher = requestPermissionLauncher,
            onPermissionGranted = {
                val scheduler = NotificationScheduler(activity)
                scheduler.scheduleAllNotifications()
            }
        ).apply {
            checkAndRequestNotificationPermission()
        }
    }
}