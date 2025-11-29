package com.example.fitbalance.notification

import android.content.Context
import android.content.SharedPreferences
import com.example.fitbalance.data.NotificationItem
import com.example.fitbalance.data.NotificationType
import org.json.JSONArray
import org.json.JSONObject

class NotificationPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "notification_prefs",
        Context.MODE_PRIVATE
    )

    fun saveNotification(notification: NotificationItem) {
        val notifications = getNotifications().toMutableList()
        notifications.add(0, notification)

        val maxNotifications = 50
        if (notifications.size > maxNotifications) {
            notifications.subList(maxNotifications, notifications.size).clear()
        }

        saveNotificationsList(notifications)
    }

    fun getNotifications(): List<NotificationItem> {
        val jsonString = prefs.getString("notifications", null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(jsonString)
            val notifications = mutableListOf<NotificationItem>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                notifications.add(
                    NotificationItem(
                        id = jsonObject.getString("id"),
                        title = jsonObject.getString("title"),
                        message = jsonObject.getString("message"),
                        timestamp = jsonObject.getLong("timestamp"),
                        type = NotificationType.valueOf(jsonObject.getString("type"))
                    )
                )
            }

            notifications
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteNotification(notificationId: String) {
        val notifications = getNotifications().toMutableList()
        notifications.removeAll { it.id == notificationId }
        saveNotificationsList(notifications)

        val unreadCount = getUnreadCount()
        if (unreadCount > 0) {
            setUnreadCount(unreadCount - 1)
        }
    }

    private fun saveNotificationsList(notifications: List<NotificationItem>) {
        val jsonArray = JSONArray()
        notifications.forEach { notif ->
            val jsonObject = JSONObject().apply {
                put("id", notif.id)
                put("title", notif.title)
                put("message", notif.message)
                put("timestamp", notif.timestamp)
                put("type", notif.type.name)
            }
            jsonArray.put(jsonObject)
        }

        prefs.edit().putString("notifications", jsonArray.toString()).apply()
    }

    fun clearNotifications() {
        prefs.edit().remove("notifications").apply()
        clearUnreadCount()
    }

    fun getUnreadCount(): Int {
        return prefs.getInt("unread_count", 0)
    }

    fun setUnreadCount(count: Int) {
        prefs.edit().putInt("unread_count", count).apply()
    }

    fun incrementUnreadCount() {
        val current = getUnreadCount()
        setUnreadCount(current + 1)
    }

    fun clearUnreadCount() {
        setUnreadCount(0)
    }
}