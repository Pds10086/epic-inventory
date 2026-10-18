package com.example.epicinv.notify

import android.content.Context
import androidx.work.*
import com.example.epicinv.data.api.EpicApi
import com.example.epicinv.data.auth.TokenStore
import com.example.epicinv.data.freegame.FreeGameRepository
import java.util.concurrent.TimeUnit

/**
 * 每周四 17:01 自动领取当前在售的免费游戏
 */
class AutoClaimWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val token = TokenStore(applicationContext).load()
            ?: return Result.success()
        val accountId = token.clientId ?: token.accountId
            ?: return Result.success()

        val items = FreeGameRepository(EpicApi(), TokenStore(applicationContext))
            .load()
            .filter { it.status == com.example.epicinv.data.freegame.FreeGameItem.Status.CURRENT }

        val api = EpicApi()
        var success = 0
        items.forEach { item ->
            if (item.namespace.isBlank() || item.offerId.isBlank()) return@forEach
            if (api.claimFreeGame(token.accessToken, accountId, item.namespace, item.offerId)) {
                success++
            }
        }
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "auto_claim_worker"
        fun schedule(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<AutoClaimWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(2, TimeUnit.HOURS) // 不必精准追求 17:01：token 有效期 8h 之内执行过即可
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                req
            )
        }
    }
}
