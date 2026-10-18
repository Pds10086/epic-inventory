package com.example.epicinv.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object Channels {
    const val FREE_GAME = "freegame_channel"

    fun ensureAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(
            FREE_GAME,
            "免费游戏提醒",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "新一周的免费游戏上架提醒" }
        nm.createNotificationChannel(ch)
    }
}
