package com.example.fitbalance.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationCleanupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationPrefs = NotificationPreferences(context)
        notificationPrefs.clearNotifications()
    }
}