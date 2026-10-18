package com.example.epicinv.ui.auth

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.epicinv.BuildConfig
import com.example.epicinv.ui.InventoryViewModel
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuthRoute(viewModel: InventoryViewModel, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val authState = remember { mutableStateOf<AuthStatus>(AuthStatus.Idle) }
    val state = remember { java.util.UUID.randomUUID().toString().replace("-", "") }
    val redirectUri = "https://www.epicgames.com/redirect"
    val clientId = BuildConfig.EPIC_CLIENT_ID

    LaunchedEffect(authState.value) {
        val s = authState.value
        if (s is AuthStatus.CodeReceived) {
            viewModel.completeAuthorization(s.code, clientId)
        }
        if (s is AuthStatus.CodeReceived) {
            // 等待 ViewModel 写入 token，关闭路由
            onClose()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Epic 账号登录", fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(8.dp))
            TextButton(onClick = onClose) {
                Icon(Icons.Default.Cancel, null)
                Spacer(Modifier.width(4.dp))
                Text("取消")
            }
        }

        if (clientId == "YOUR_EPIC_CLIENT_ID") {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("尚未配置 EPIC_CLIENT_ID。\n请用 -PEPIC_CLIENT_ID=xxx 重新编译。")
            }
            return
        }

        val authUrl = Uri.parse("https://www.epicgames.com/id/api/redirect").buildUpon()
            .appendQueryParameter("clientId", clientId)
            .appendQueryParameter("responseType", "code")
            .appendQueryParameter("scope", "basic_profile")
            .appendQueryParameter("redirectUri", redirectUri)
            .appendQueryParameter("state", state)
            .build()

        BackHandler { onClose() }

        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.userAgentString = "Mozilla/5.0 EpicInventory/0.1 Android"
                    webViewClient = object : android.webkit.WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?, request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: return false
                            if (url.startsWith(redirectUri)) {
                                val code = Uri.parse(url).getQueryParameter("code")
                                val gotState = Uri.parse(url).getQueryParameter("state")
                                authState.value = if (!code.isNullOrBlank() && gotState == state) {
                                    AuthStatus.CodeReceived(code)
                                } else {
                                    AuthStatus.Failed
                                }
                                return true
                            }
                            return false
                        }
                    }
                    loadUrl(authUrl.toString())
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = {}
        )
    }
}

private sealed class AuthStatus {
    data object Idle : AuthStatus()
    data object Failed : AuthStatus()
    data class CodeReceived(val code: String) : AuthStatus()
}
