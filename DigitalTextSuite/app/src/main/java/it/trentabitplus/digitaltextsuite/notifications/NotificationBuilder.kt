package it.trentabitplus.digitaltextsuite.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.activities.MainActivity
import it.trentabitplus.digitaltextsuite.activities.PlayWithEmojiActivity

/**
 * This class manages the creation of a notification
 * @param context A Context instance
 * @author Matteo Ciccaglione
 */
class NotificationBuilder(val context: Context) {
    /**
     * Set up notification by setting an AlarmManager every 24 hours. Deletes the previous AlarmManager set
     */
     fun setNotificationOn(){
        val intent = Intent(context, PlayWithEmojiActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        createChannel()
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.square_edit_outline)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val notificationIntent = Intent(context,NotificationReceiver::class.java)
        notificationIntent.putExtra("notification",builder.build())
        val newPendingIntent = PendingIntent.getBroadcast(context,1,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
         alarmManager.cancel(newPendingIntent)
        val timeFuture = 86400000L
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeFuture,
                timeFuture, newPendingIntent
            )
    }

    /**
     * Create a new communication channel (only on android Oreo or subsequent)
     */
    private fun createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(MainActivity.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}