package com.example.epicinv.data.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.epicinv.data.model.OAuthTokenResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 多账号 token 存储
 * 主键：accountId 缺省时用 "default"
 */
class TokenStore(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "epic_secure_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun save(token: OAuthTokenResponse) {
        val id = token.accountId ?: token.clientId ?: DEFAULT_ACCOUNT
        val payload = json.encodeToString(StoredToken.serializer(),
            StoredToken(id, token))
        prefs.edit().putString("$KEY_PREFIX$id", payload).apply()
        prefs.edit().putString(KEY_ACTIVE, id).apply()
    }

    fun load(): OAuthTokenResponse? = active()?.token

    fun activeAccountId(): String? = prefs.getString(KEY_ACTIVE, null)

    fun list(): List<OAuthTokenResponse> {
        val out = mutableListOf<OAuthTokenResponse>()
        prefs.all.forEach { (k, v) ->
            if (k.startsWith(KEY_PREFIX) && v is String) {
                runCatching {
                    json.decodeFromString(StoredToken.serializer(), v)
                }.getOrNull()?.let { out.add(it.token) }
            }
        }
        return out
    }

    fun switchTo(accountId: String) {
        val all = list()
        val target = all.firstOrNull { it.accountId == accountId || it.clientId == accountId }
        if (target != null) {
            prefs.edit().putString(KEY_ACTIVE, accountId).apply()
        }
    }

    fun remove(accountId: String) {
        prefs.edit().remove("$KEY_PREFIX$accountId").apply()
        if (prefs.getString(KEY_ACTIVE, null) == accountId) {
            prefs.edit().remove(KEY_ACTIVE).apply()
            val first = list().firstOrNull()
            if (first != null) save(first)
        }
    }

    fun clear() {
        val keys = prefs.all.keys.filter { it.startsWith(KEY_PREFIX) }
        keys.forEach { prefs.edit().remove(it).apply() }
        prefs.edit().remove(KEY_ACTIVE).apply()
    }

    private fun active(): StoredToken? {
        val activeId = prefs.getString(KEY_ACTIVE, null) ?: return null
        val raw = prefs.getString("$KEY_PREFIX$activeId", null) ?: return null
        return runCatching {
            json.decodeFromString(StoredToken.serializer(), raw)
        }.getOrNull()
    }

    @Serializable
    data class StoredToken(val accountId: String, val token: OAuthTokenResponse)

    companion object {
        private const val KEY_PREFIX = "token_"
        private const val KEY_ACTIVE = "active_account"
        private const val DEFAULT_ACCOUNT = "default"
    }
}
