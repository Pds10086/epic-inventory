package com.example.epicinv.data.api

import com.example.epicinv.data.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class EpicApi {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    fun exchangeCodeForToken(
        code: String,
        clientId: String,
        deploymentId: String = "2520ae695d8c49ce94b60732fe20d28a"
    ): OAuthTokenResponse {
        val url = "https://api.epicgames.dev/epic/oauth/v1/token".toHttpUrl()
        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("scope", "basic_profile")
            .add("deployment_id", deploymentId)
            .add("client_id", clientId)
            .add("code", code)
            .build()

        val req = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Authorization", basicAuth(clientId))
            .build()

        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("OAuth exchange failed: ${resp.code} $text")
            return Json { ignoreUnknownKeys = true }
                .decodeFromString(OAuthTokenResponse.serializer(), text)
        }
    }

    fun fetchLibrary(accessToken: String, accountId: String): LibraryResponse {
        val url = "https://library-service.live.use1a.on.epicgames.com/library/api/public/library".toHttpUrl()
            .newBuilder()
            .addQueryParameter("start", "0")
            .addQueryParameter("count", "1000")
            .addQueryParameter("platform", "")
            .addQueryParameter("include",
                "appName,canRunOffline,canRunOnLocalCloud,cloudSaveFile," +
                "downloadKeys,entitlementName,entitlementPlatform,formatOwned,installLocation," +
                "installationList,inventoryItem,mainGameItem,sandboxName,useEvent,owned,inactive")
            .build()

        val req = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("X-Epic-Account-Id", accountId)
            .build()

        return execute(req)
    }

    fun fetchCatalog(accessToken: String, ids: List<Triple<String, String, String>>): CatalogItemsResponse {
        val url = "https://catalog-public-service-prod.ol.epicgames.com/catalog/api/shared/items".toHttpUrl()
        val req = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("X-Epic-Client-Id", CLIENT_ID_PUBLIC)
            .addHeader("X-Epic-Catalog-Item-Ids", ids.joinToString(",") {
                "${it.first}:${it.second}:${it.third}"
            })
            .build()
        return execute(req)
    }

    /**
     * 拉取所有国家/区域的免费促销，找出 free -> current 状态的 offer
     * 想限定区域可以传 country=CN
     */
    fun fetchFreePromotions(
        accessToken: String,
        country: String = "CN",
        allowCountries: String = "CN,US,JP,GB,DE,FR,KR"
    ): FreePromotionsResponse {
        val url = "https://store-public-service-prod.ol.epicgames.com/store/v2/freePromotions".toHttpUrl()
            .newBuilder()
            .addQueryParameter("locale", "zh-CN")
            .addQueryParameter("country", country)
            .addQueryParameter("allowCountries", allowCountries)
            .addQueryParameter("startDate", "2025-01-01T00:00:00.000Z")
            .addQueryParameter("endDate", "2030-12-31T00:00:00.000Z")
            .build()

        val req = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("X-Epic-Country", country)
            .build()
        return execute(req)
    }

    /**
     * 领取免费促销游戏（需要登录 token + 拿 orderId / offerId 后调用 order endpoint）
     */
    fun claimFreeGame(
        accessToken: String,
        accountId: String,
        namespace: String,
        offerId: String
    ): Boolean {
        val url = "https://store-public-service-prod.ol.epicgames.com/store/v2/" +
            "cart/$accountId/orders/free/claim/$namespace/$offerId"
        val req = Request.Builder()
            .url(url)
            .post(okhttp3.RequestBody.Companion.toRequestBody("{}",
                okhttp3.MediaType.Companion.toMediaType("application/json")))
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("X-Epic-Account-Id", accountId)
            .build()

        client.newCall(req).execute().use { resp ->
            return resp.isSuccessful
        }
    }

    private inline fun <reified T> execute(req: Request): T {
        val decoder = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("Epic API ${req.url.encodedPath} failed ${resp.code}: $body")
            return decoder.decodeFromString(jsonString = body)
        }
    }

    private fun basicAuth(clientId: String): String {
        val raw = "$clientId:".toByteArray()
        return "Basic " + android.util.Base64.encodeToString(raw, android.util.Base64.NO_WRAP)
    }

    companion object {
        const val CLIENT_ID_PUBLIC = "34a02cf8f5f54e8497c5b8e7b5e3b1f9"
    }
}

@Serializable
data class FreePromotionsResponse(
    val promotions: PromotionsData = PromotionsData()
)

@Serializable
data class PromotionsData(
    val promotionalOffers: List<PromotionalOffer> = emptyList(),
    val upcomingPromotionalOffers: List<PromotionalOffer> = emptyList()
)

@Serializable
data class PromotionalOffer(
    val promotionalOffers: List<PromotionalEntry> = emptyList()
)

@Serializable
data class PromotionalEntry(
    val startDate: String = "",
    val endDate: String = "",
    val discountSetting: DiscountSetting = DiscountSetting()
)

@Serializable
data class DiscountSetting(
    val discountPercentage: Double = 100.0,
    val discountType: String = ""
)
