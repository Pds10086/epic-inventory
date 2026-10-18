package com.example.epicinv.data.stats

import com.example.epicinv.data.api.EpicApi
import com.example.epicinv.data.auth.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@Serializable
data class AchievementsResponse(
    val records: List<AchievementItem> = emptyList(),
    val totalCount: Int = 0
)

@Serializable
data class AchievementItem(
    val achievementId: String = "",
    val name: String = "",
    val description: String = "",
    val unlocked: Boolean = false,
    val unlockTime: String? = null,
    val progress: AchievementProgress? = null,
    val rarityPercent: Double = 0.0
)

@Serializable
data class AchievementProgress(
    val earned: Int = 0,
    val total: Int = 0
)

data class StatsReport(
    val totalGames: Int,
    val totalAchievementsUnlocked: Int,
    val completionRate: Double,
    val topRareAchievements: List<Pair<String, Double>>,
    val earnedByYear: Map<Int, Int>,
    val recentUnlocks: List<Pair<String, Long>>
)

class StatsRepository(
    private val api: EpicApi,
    private val tokens: TokenStore
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun buildReport(): StatsReport = withContext(Dispatchers.IO) {
        val token = tokens.load() ?: return@withContext empty()
        val accountId = token.clientId ?: token.accountId ?: return@withContext empty()
        val library = api.fetchLibrary(token.accessToken, accountId)
        val mainGames = library.records
            .filter { it.mainGameItem != null && !it.vaultClosed.equals(true) }
            .distinctBy { "${it.catalogNamespace}:${it.catalogItemId}" }

        val items = mutableListOf<AchievementItem>()
        mainGames.forEach { asset ->
            val ns = asset.catalogNamespace
            val id = asset.catalogItemId
            val url = "https://achievements-public-service-prod.ol.epicgames.com" +
                "/achievements/api/v1/public/players/$accountId/$ns/items?appId=$id&start=0&count=200"
            val req = Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer ${token.accessToken}")
                .addHeader("X-Epic-Account-Id", accountId)
                .build()
            runCatching {
                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string().orEmpty()
                        if (body.isNotBlank()) {
                            val parsed = kotlinx.serialization.json.Json {
                                ignoreUnknownKeys = true
                            }.decodeFromString(AchievementsResponse.serializer(), body)
                            items.addAll(parsed.records)
                        }
                    }
                }
            }
        }

        val unlockedCount = items.count { it.unlocked }
        val rate = if (items.isEmpty()) 0.0 else unlockedCount.toDouble() / items.size

        val topRare = items
            .filter { it.unlocked }
            .sortedBy { it.rarityPercent }
            .take(5)
            .map { it.name to it.rarityPercent }

        val byYear = items
            .filter { it.unlocked && !it.unlockTime.isNullOrBlank() }
            .mapNotNull {
                val year = runCatching {
                    java.time.OffsetDateTime.parse(it.unlockTime!!).year
                }.getOrNull()
                if (year != null) year else null
            }
            .groupingBy { it }
            .eachCount()

        val recent = items.filter { it.unlocked && !it.unlockTime.isNullOrBlank() }
            .sortedByDescending {
                runCatching {
                    java.time.OffsetDateTime.parse(it.unlockTime!!)
                        .toInstant().toEpochMilli()
                }.getOrDefault(0L)
            }
            .take(5)
            .map { it.name to
                runCatching {
                    java.time.OffsetDateTime.parse(it.unlockTime!!)
                        .toInstant().toEpochMilli()
                }.getOrDefault(0L) }

        StatsReport(
            totalGames = mainGames.size,
            totalAchievementsUnlocked = unlockedCount,
            completionRate = rate,
            topRareAchievements = topRare,
            earnedByYear = byYear,
            recentUnlocks = recent
        )
    }

    fun empty() = StatsReport(0, 0, 0.0, emptyList(), emptyMap(), emptyList())
}
