package it.trentabitplus.digitaltextsuite.notifications

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

/**
 * BroadCastReceiver for alarm manager
 * This class send a new notification when an intent is received
 * @author Matteo Ciccaglione
 */
class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = NotificationManagerCompat.from(context!!)
        //val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        intent!!.getParcelableExtra<Notification>("notification")?.let {
            notificationManager.notify(1, it)
        }
    }
}