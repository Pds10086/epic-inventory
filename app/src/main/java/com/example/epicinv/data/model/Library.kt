package com.example.epicinv.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OAuthTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("token_type") val tokenType: String,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("account_id") val accountId: String? = null
)

@Serializable
data class LibraryAsset(
    @SerialName("appName") val appName: String = "",
    @SerialName("catalogNamespace") val catalogNamespace: String = "",
    @SerialName("catalogItemId") val catalogItemId: String = "",
    @SerialName("entitlementName") val entitlementName: String = "",
    @SerialName("entitlementPlatform") val entitlementPlatform: String? = null,
    @SerialName("formatOwned") val formatOwned: String = "",
    @SerialName("installationList") val installationList: String = "",
    @SerialName("mainGameItem") val mainGameItem: MainGameItem? = null,
    @SerialName("releaseInfo") val releaseInfo: List<ReleaseInfoEntry> = emptyList(),
    @SerialName("sandboxName") val sandboxName: String = "",
    @SerialName("vaultClosed") val vaultClosed: Boolean? = null
)

@Serializable
data class LibraryResponse(
    @SerialName("records") val records: List<LibraryAsset> = emptyList(),
    @SerialName("totalCount") val totalCount: Int = 0
)

@Serializable
data class MainGameItem(
    @SerialName("id") val id: String = "",
    @SerialName("namespace") val namespace: String = ""
)

@Serializable
data class ReleaseInfoEntry(
    @SerialName("appId") val appId: String = "",
    @SerialName("date") val date: String? = null,
    @SerialName("platform") val platform: String? = null
)
