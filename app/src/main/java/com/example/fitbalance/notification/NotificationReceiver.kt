package com.example.fitbalance.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.fitbalance.R
import com.example.fitbalance.data.NotificationItem
import com.example.fitbalance.data.NotificationType

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationType = intent.getStringExtra("notification_type") ?: return
        val notificationId = intent.getIntExtra("notification_id", 0)

        val notificationHelper = NotificationHelper(context)
        val notificationPrefs = NotificationPreferences(context)

        val (title, message, type) = when (notificationType) {
            "breakfast" -> Triple(
                context.getString(R.string.notification_breakfast_title),
                context.getString(R.string.notification_breakfast_text),
                NotificationType.BREAKFAST
            )
            "lunch" -> Triple(
                context.getString(R.string.notification_lunch_title),
                context.getString(R.string.notification_lunch_text),
                NotificationType.LUNCH
            )
            "dinner" -> Triple(
                context.getString(R.string.notification_dinner_title),
                context.getString(R.string.notification_dinner_text),
                NotificationType.DINNER
            )
            "water_morning" -> Triple(
                context.getString(R.string.notification_water_morning_title),
                context.getString(R.string.notification_water_morning_text),
                NotificationType.WATER_MORNING
            )
            "water_afternoon" -> Triple(
                context.getString(R.string.notification_water_afternoon_title),
                context.getString(R.string.notification_water_afternoon_text),
                NotificationType.WATER_AFTERNOON
            )
            "water_evening" -> Triple(
                context.getString(R.string.notification_water_evening_title),
                context.getString(R.string.notification_water_evening_text),
                NotificationType.WATER_EVENING
            )
            else -> return
        }

        notificationHelper.showNotification(notificationId, title, message)

        val notificationItem = NotificationItem(
            id = "${type.name}_${System.currentTimeMillis()}",
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            type = type
        )

        notificationPrefs.saveNotification(notificationItem)
        notificationPrefs.incrementUnreadCount()
    }
}