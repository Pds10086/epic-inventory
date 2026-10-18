package com.example.epicinv.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.epicinv.data.EpicRepository
import com.example.epicinv.data.InventoryItemUi
import com.example.epicinv.data.InventoryUiState
import com.example.epicinv.data.api.EpicApi
import com.example.epicinv.data.auth.TokenStore
import com.example.epicinv.data.freegame.FreeGameItem
import com.example.epicinv.data.freegame.FreeGameRepository
import com.example.epicinv.data.stats.StatsReport
import com.example.epicinv.data.stats.StatsRepository
import com.example.epicinv.ui.theme.ThemeMode
import com.example.epicinv.ui.theme.ThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class FreeGameState {
    data object Loading : FreeGameState()
    data class Loaded(val items: List<FreeGameItem>) : FreeGameState()
    data class Error(val message: String) : FreeGameState()
}

sealed class StatsState {
    data object Loading : StatsState()
    data class Loaded(val report: StatsReport) : StatsState()
    data class Error(val message: String) : StatsState()
}

class InventoryViewModel(app: Application) : AndroidViewModel(app) {

    private val tokens = TokenStore(app)
    private val api = EpicApi()
    private val repo = EpicRepository(api, tokens)
    private val freeRepo = FreeGameRepository(api, tokens)
    private val statsRepo = StatsRepository(api, tokens)
    private val themeRepo = ThemeRepository(app)

    private val _uiState = MutableStateFlow<InventoryUiState>(InventoryUiState.NeedLogin)
    val uiState: StateFlow<InventoryUiState> = _uiState

    private val _freeState = MutableStateFlow<FreeGameState>(FreeGameState.Loading)
    val freeState: StateFlow<FreeGameState> = _freeState

    private val _statsState = MutableStateFlow<StatsState>(StatsState.Loading)
    val statsState: StateFlow<StatsState> = _statsState

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val themeMode: StateFlow<ThemeMode> = run {
        val flow = MutableStateFlow(ThemeMode.SYSTEM)
        viewModelScope.launch {
            themeRepo.flow.collect { flow.value = it }
        }
        flow
    }

    private val _accountList = MutableStateFlow<List<com.example.epicinv.data.model.OAuthTokenResponse>>(emptyList())
    val accountList: StateFlow<List<com.example.epicinv.data.model.OAuthTokenResponse>> = _accountList

    private val _activeAccountId = MutableStateFlow<String?>(null)
    val activeAccountId: StateFlow<String?> = _activeAccountId

    fun refreshAccountList() {
        _accountList.value = tokens.list()
        _activeAccountId.value = tokens.activeAccountId()
    }

    fun switchAccount(id: String) {
        tokens.switchTo(id)
        refreshAccountList()
        refresh()
    }

    fun removeAccount(id: String) {
        tokens.remove(id)
        refreshAccountList()
        if (tokens.load() == null) _uiState.value = InventoryUiState.NeedLogin else refresh()
    }

    fun onQueryChanged(q: String) {
        _query.value = q
    }

    fun isLoggedIn(): Boolean = tokens.load() != null

    fun logout() {
        tokens.clear()
        _uiState.value = InventoryUiState.NeedLogin
    }

    /**
     * WebView 拦截到 authorization code 后调用本方法
     * 1) 换 access_token
     * 2) 写入 TokenStore
     * 3) 触发一次库存刷新
     */
    fun completeAuthorization(code: String, clientId: String) {
        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    api.exchangeCodeForToken(code, clientId)
                }
                tokens.save(token)
                refresh()
            } catch (t: Throwable) {
                _uiState.value = InventoryUiState.Error("登录失败：${t.message}")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = try {
                repo.loadInventory()
            } catch (t: Throwable) {
                InventoryUiState.Error(t.message ?: "未知错误")
            }
        }
    }

    fun refreshFreeGames() {
        viewModelScope.launch {
            _freeState.value = FreeGameState.Loading
            _freeState.value = try {
                FreeGameState.Loaded(freeRepo.load())
            } catch (t: Throwable) {
                FreeGameState.Error(t.message ?: "未知错误")
            }
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            _statsState.value = StatsState.Loading
            _statsState.value = try {
                StatsState.Loaded(statsRepo.buildReport())
            } catch (t: Throwable) {
                StatsState.Error(t.message ?: "未知错误")
            }
        }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { themeRepo.set(mode) }
    }

    fun filterItems(all: List<InventoryItemUi>): List<InventoryItemUi> {
        val q = _query.value.trim().lowercase()
        if (q.isEmpty()) return all
        return all.filter {
            it.title.lowercase().contains(q) ||
                it.tags.any { t -> t.lowercase().contains(q) }
        }
    }

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer { InventoryViewModel(app) }
        }
    }
}
