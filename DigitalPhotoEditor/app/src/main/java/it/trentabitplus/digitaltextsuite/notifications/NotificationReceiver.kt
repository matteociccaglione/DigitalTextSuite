package it.trentabitplus.digitaltextsuite.notifications

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("RECEIVER","RECEIVED")
        val notificationManager = NotificationManagerCompat.from(context!!)
        //val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        intent!!.getParcelableExtra<Notification>("notification")?.let {
            notificationManager.notify(1, it)
        }
    }
}