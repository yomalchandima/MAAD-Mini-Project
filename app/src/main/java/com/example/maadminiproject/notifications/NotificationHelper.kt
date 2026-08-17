package com.example.maadminiproject.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.ui.notification.NotificationsActivity

/**
 * Helper class for managing and displaying Android system/status-bar notifications.
 *
 * It creates notification channels on Android Oreo+ (API 26+) and dispatches high-priority
 * alerts for safety events and standard notifications for automations.
 */
object NotificationHelper {

    private const val CHANNEL_ID_SAFETY = "safety_alerts_channel"
    private const val CHANNEL_NAME_SAFETY = "Safety Alerts"
    private const val CHANNEL_DESC_SAFETY = "Critical smart home safety alerts and automatic device shutdowns"

    private const val CHANNEL_ID_GENERAL = "general_notifications_channel"
    private const val CHANNEL_NAME_GENERAL = "General Notifications"
    private const val CHANNEL_DESC_GENERAL = "Smart home automation, schedule, and system notifications"

    /**
     * Initializes notification channels required for Android Oreo and above.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // High Priority Channel for Safety Alerts
            val safetyChannel = NotificationChannel(
                CHANNEL_ID_SAFETY,
                CHANNEL_NAME_SAFETY,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_SAFETY
                enableVibration(true)
                setShowBadge(true)
            }

            // Default Priority Channel for General Notifications
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC_GENERAL
                enableVibration(false)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(safetyChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    /**
     * Shows a local system notification in the Android status bar.
     *
     * @param context Android context.
     * @param notificationId Unique integer ID for the Android notification.
     * @param title Title of the notification.
     * @param message Message body.
     * @param type Category of the notification ("SAFETY", "SCHEDULE", "SECURITY", "SYSTEM").
     */
    fun showLocalNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        type: String = "SYSTEM"
    ) {
        createNotificationChannels(context)

        val isSafety = type.equals("SAFETY", ignoreCase = true)
        val channelId = if (isSafety) CHANNEL_ID_SAFETY else CHANNEL_ID_GENERAL
        val priority = if (isSafety) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT

        val intent = Intent(context, NotificationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.my_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(notificationId, builder.build())
    }
}
