package com.example.fitbalance.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.fitbalance.data.NotificationItem
import com.example.fitbalance.notification.NotificationPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class NotificationScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val notificationPrefs = NotificationPreferences(context)

    var notifications by mutableStateOf<List<NotificationItem>>(emptyList())
        private set

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        notifications = notificationPrefs.getNotifications()
        notificationPrefs.clearUnreadCount()
    }

    fun deleteNotification(notificationId: String) {
        notificationPrefs.deleteNotification(notificationId)
        loadNotifications()
    }

    fun getUnreadCount(): Int {
        return notificationPrefs.getUnreadCount()
    }
}