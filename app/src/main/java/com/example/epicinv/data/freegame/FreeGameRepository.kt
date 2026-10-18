package com.example.epicinv.data.freegame

import com.example.epicinv.data.api.EpicApi
import com.example.epicinv.data.auth.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class FreeGameItem(
    val namespace: String,
    val offerId: String,
    val title: String,
    val description: String,
    val coverUrl: String?,
    val startDate: Long,
    val endDate: Long,
    val status: Status
) {
    enum class Status { CURRENT, UPCOMING, ENDED }
}

class FreeGameRepository(
    private val api: EpicApi,
    private val tokens: TokenStore
) {
    suspend fun load(): List<FreeGameItem> = withContext(Dispatchers.IO) {
        val token = tokens.load() ?: return@withContext emptyList()
        val now = System.currentTimeMillis()
        val resp = api.fetchFreePromotions(token.accessToken)

        // Epic 返回结构：{promotions:{promotionalOffers:[{promotionalOffers:[{...}]}]}}
        val entries = resp.promotions.promotionalOffers.flatMap { wrap ->
            wrap.promotionalOffers
        } + resp.promotions.upcomingPromotionalOffers.flatMap { wrap ->
            wrap.promotionalOffers
        }

        entries.mapNotNull { entry ->
            val s = runCatching { java.time.Instant.parse(entry.startDate).toEpochMilli() }
                .getOrNull() ?: return@mapNotNull null
            val e = runCatching { java.time.Instant.parse(entry.endDate).toEpochMilli() }
                .getOrNull() ?: return@mapNotNull null
            val status = when {
                now in s..e -> FreeGameItem.Status.CURRENT
                now < s -> FreeGameItem.Status.UPCOMING
                else -> FreeGameItem.Status.ENDED
            }
            FreeGameItem(
                namespace = "",
                offerId = "",
                title = "免费游戏（${entry.startDate.substring(0, 10)} → ${entry.endDate.substring(0, 10)}）",
                description = "",
                coverUrl = null,
                startDate = s,
                endDate = e,
                status = status
            )
        }
    }
}
