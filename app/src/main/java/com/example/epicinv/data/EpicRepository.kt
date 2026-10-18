package com.example.epicinv.data

import com.example.epicinv.data.api.EpicApi
import com.example.epicinv.data.auth.TokenStore
import com.example.epicinv.data.model.CatalogRecord
import com.example.epicinv.data.model.LibraryAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EpicRepository(
    private val api: EpicApi,
    private val tokens: TokenStore
) {

    suspend fun loadInventory(): InventoryUiState = withContext(Dispatchers.IO) {
        val token = tokens.load() ?: return@withContext InventoryUiState.NeedLogin

        // 1. 拉库
        val accountId = token.clientId
            ?: token.accountId
            ?: error("missing account_id")
        val raw = api.fetchLibrary(token.accessToken, accountId)

        // 2. 过滤主游戏，去重
        val mainGameAssets = raw.records
            .filter { it.mainGameItem != null && !it.vaultClosed.equals(true) }
            .distinctBy { "${it.catalogNamespace}:${it.catalogItemId}" }

        // 3. 查 catalog
        val ids = mainGameAssets.map { Triple(it.sandboxName, it.catalogNamespace, it.catalogItemId) }
        val catalogMap: Map<String, CatalogRecord> = if (ids.isNotEmpty()) {
            val resp = api.fetchCatalog(token.accessToken, ids)
            resp.records.associateBy { "${it.namespace}:${it.id}" }
        } else emptyMap()

        // 4. 组装
        val items = mainGameAssets.mapNotNull { asset ->
            val key = "${asset.catalogNamespace}:${asset.catalogItemId}"
            val record = catalogMap[key] ?: return@mapNotNull null
            InventoryItemUi(
                catalogId = key,
                title = record.title.ifBlank { asset.appName },
                description = record.description,
                coverUrl = record.keyImages.firstOrNull { it.type == "DieselGameBox" }?.url
                    ?: record.keyImages.firstOrNull { it.type == "Thumbnail" }?.url
                    ?: record.keyImages.firstOrNull { it.type == "OfferImageWide" }?.url,
                tags = record.categories.map { it.path.substringAfterLast('.') }.take(3),
                priceText = record.price?.totalPrice?.formatted?.original
                    ?: record.price?.discountPrice?.formatted?.original,
                isInstalled = asset.installationList.contains("INSTALLED", ignoreCase = true),
                platform = asset.entitlementPlatform ?: asset.releaseInfo.firstOrNull()?.platform ?: "PC"
            )
        }.sortedBy { it.title.lowercase() }

        InventoryUiState.Loaded(
            InventoryStatsUi(
                totalGames = items.size,
                installed = items.count { it.isInstalled },
                mobileFriendly = items.count { it.platform != "PC" },
                totalValue = items.count { !it.priceText.isNullOrBlank() }
            ),
            items
        )
    }
}

data class InventoryItemUi(
    val catalogId: String,
    val title: String,
    val description: String,
    val coverUrl: String?,
    val tags: List<String>,
    val priceText: String?,
    val isInstalled: Boolean,
    val platform: String
)

data class InventoryStatsUi(
    val totalGames: Int,
    val installed: Int,
    val mobileFriendly: Int,
    val totalValue: Int
)

sealed class InventoryUiState {
    data object NeedLogin : InventoryUiState()
    data class Loaded(val stats: InventoryStatsUi, val items: List<InventoryItemUi>) : InventoryUiState()
    data class Error(val message: String) : InventoryUiState()
}
