package com.example.epicinv.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Context.themeStore by preferencesDataStore("epic_theme")
private val THEME_KEY = stringPreferencesKey("theme_mode")

class ThemeRepository(private val ctx: Context) {

    val flow: Flow<ThemeMode> = ctx.themeStore.data.map { p ->
        runCatching { ThemeMode.valueOf(p[THEME_KEY] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    suspend fun set(mode: ThemeMode) {
        ctx.themeStore.edit { it[THEME_KEY] = mode.name }
    }
}
