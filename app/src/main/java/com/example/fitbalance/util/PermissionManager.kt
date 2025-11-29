package com.example.fitbalance.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.example.fitbalance.R

class PermissionManager(
    private val activity: Activity,
    private val requestPermissionLauncher: ActivityResultLauncher<String>,
    private val onPermissionGranted: () -> Unit
) {


    private val prefs = activity.getSharedPreferences("permission_prefs", Context.MODE_PRIVATE)

    fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasAskedBefore = prefs.getBoolean("notification_permission_asked", false)

            when {
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
                activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPermissionRationaleDialog()
                }
                !hasAskedBefore -> {
                    prefs.edit().putBoolean("notification_permission_asked", true).apply()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            onPermissionGranted()
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.notification_permission_title))
            .setMessage(activity.getString(R.string.notification_permission_message))
            .setPositiveButton(activity.getString(R.string.notification_permission_grant)) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton(activity.getString(R.string.notification_permission_deny), null)
            .show()
    }

    fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            onPermissionGranted()
        }
    }
}