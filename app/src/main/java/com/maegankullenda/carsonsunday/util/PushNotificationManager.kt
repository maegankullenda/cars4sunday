package com.maegankullenda.carsonsunday.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.maegankullenda.carsonsunday.MainActivity
import com.maegankullenda.carsonsunday.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val eventChannel = NotificationChannel(
                CHANNEL_EVENTS,
                "Event Notifications",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications for new and updated events"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(eventChannel)
        }
    }

    fun showNotification(title: String, body: String, eventId: String?, type: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            eventId?.let { putExtra("eventId", it) }
            putExtra("notificationType", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_EVENTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val CHANNEL_EVENTS = "events_channel"
    }
}
