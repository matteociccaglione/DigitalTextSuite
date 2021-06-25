package it.trentabitplus.digitaltextsuite.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.activities.PlayWithEmojiActivity
import it.trentabitplus.digitaltextsuite.activities.RealMainActivity

class MyAppWidget : AppWidgetProvider() {
    @SuppressLint("RemoteViewLayout")
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        // There may be multiple widgets active, so update all of them
        if (appWidgetIds != null) {
            for (appWidgetId in appWidgetIds) {
                val remoteViews = RemoteViews(context?.packageName, R.layout.layout_my_app_widget)
                val thisWidget = context?.let { ComponentName(it, MyAppWidget::class.java) }

                remoteViews.setOnClickPendingIntent(R.id.btnWhiteboard, getPendingSelfIntent(context, "b1"))
                remoteViews.setOnClickPendingIntent(R.id.btnAllFiles, getPendingSelfIntent(context, "b2"))
                remoteViews.setOnClickPendingIntent(R.id.btnTranslate, getPendingSelfIntent(context, "b3"))
                remoteViews.setOnClickPendingIntent(R.id.btnRecognizeText, getPendingSelfIntent(context, "b4"))
                remoteViews.setOnClickPendingIntent(R.id.btnPlayWithEmoji, getPendingSelfIntent(context, "b5"))
                appWidgetManager?.updateAppWidget(thisWidget, remoteViews)
            }
        }
    }

    private fun getPendingSelfIntent(context: Context?, action: String): PendingIntent? {
        val intent = Intent(context, MyAppWidget::class.java)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        // Real-time translation
        when (intent?.action) {
            "b3" -> {
                val i = Intent(context,RealMainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.putExtra("fragment",3)
                context?.startActivity(i)

            }
            //all files
            "b2" -> {
                val i = Intent(context,RealMainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.putExtra("fragment",0)
                context?.startActivity(i)
            }
            // Digital ink whiteboard
            "b1" -> {
                val i = Intent(context,RealMainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.putExtra("fragment",1)
                context?.startActivity(i)
            }
            // Text recognition
            "b4" -> {
                val i = Intent(context,RealMainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.putExtra("fragment",4)
                context?.startActivity(i)
            }
            // Play with emoji activity
            "b5" -> {
                val i = Intent(context,PlayWithEmojiActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context?.startActivity(i)
            }
        }
    }


    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}