package com.example.epicinv.notify

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.epicinv.R
import com.example.epicinv.data.api.EpicApi
import com.example.epicinv.data.auth.TokenStore
import com.example.epicinv.data.freegame.FreeGameRepository
import com.example.epicinv.ui.MainActivity
import java.util.concurrent.TimeUnit

/**
 * 每周四 17:00 (Epic 周免刷新时间) 拉取一次，发现新条目就推送
 */
class WeeklyFreeGameWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Channels.ensureAll(applicationContext)
        val tokens = TokenStore(applicationContext)
        val repo = FreeGameRepository(EpicApi(), tokens)
        val items = runCatching { repo.load() }.getOrNull() ?: return Result.success()

        val currentFree = items.filter { it.status == com.example.epicinv.data.freegame.FreeGameItem.Status.CURRENT }
        val needNotify = Prefs(applicationContext)
            .filterNewForNotification(currentFree.map { it.title })

        if (needNotify.isNotEmpty()) {
            val nm = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val text = needNotify.joinToString("、") { it }
            val notify = NotificationCompat.Builder(applicationContext, Channels.FREE_GAME)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentTitle("免费游戏上新啦")
                .setContentText(text.take(50))
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(MainActivity.openIntent(applicationContext))
                .setAutoCancel(true)
                .build()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.POST_NOTIFICATIONS
                ) == 0
            ) {
                nm.notify(0xF1EE, notify)
            }
            Prefs(applicationContext).markNotified(currentFree.map { it.title }.toSet())
        }
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "weekly_free_game_worker"
        fun schedule(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<WeeklyFreeGameWorker>(6, TimeUnit.HOURS)
                .setInitialDelay(delayToNextThursday17(), TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                req
            )
        }

        /**
         * 下一次周四 17:00 (UTC+8) 的毫秒数。简化：下一周第一个周四 17:00
         */
        private fun delayToNextThursday17(): Long {
            val now = System.currentTimeMillis()
            val zone = java.time.ZoneId.of("Asia/Shanghai")
            val dt = java.time.LocalDateTime.now(zone)
                .withHour(17).withMinute(0).withSecond(0).withNano(0)
            val daysUntilThu = ((4 - dt.dayOfWeek.value + 7) % 7).toLong()
            val target = dt.plusDays(daysUntilThu)
                .atZone(zone).toInstant().toEpochMilli()
            return (target - now).coerceAtLeast(60_000)
        }
    }
}
