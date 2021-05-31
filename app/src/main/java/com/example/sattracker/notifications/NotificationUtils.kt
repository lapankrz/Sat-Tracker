package com.example.sattracker.notifications

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import com.example.sattracker.model.Satellite
import java.util.*


class NotificationUtils {
    fun setNotification(timeInMilliSeconds: Long, activity: Activity, satellite: Satellite?) {
        if (timeInMilliSeconds > 0) {
            val alarmManager = activity.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(activity.applicationContext, AlarmReceiver::class.java) // AlarmReceiver1 = broadcast receiver

            alarmIntent.putExtra("reason", "notification")
            alarmIntent.putExtra("timestamp", timeInMilliSeconds)
            alarmIntent.putExtra("title", satellite?.name + " will be visible in 1 minute")
            alarmIntent.putExtra("message", "There will be a visual pass of satellite " + satellite?.name + " (ID: " + satellite?.id + ") in 1 minute")

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMilliSeconds


            val pendingIntent = PendingIntent.getBroadcast(activity, satellite?.id ?: 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancelNotification(activity: Activity, satId : Int)
    {
        val intent = Intent(activity.applicationContext, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(activity, satId, intent, PendingIntent.FLAG_NO_CREATE)
        val alarmManager = activity.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}