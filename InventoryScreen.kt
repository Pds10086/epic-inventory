package com.example.epicinv.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.epicinv.data.InventoryItemUi
import com.example.epicinv.data.InventoryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onLoginRequested: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Epic 库存", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "退出登录")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val s = state) {
                is InventoryUiState.NeedLogin -> LoginPrompt(onLogin = onLoginRequested)
                is InventoryUiState.Error -> ErrorBox(s.message)
                is InventoryUiState.Loaded -> InventoryContent(
                    items = s.items,
                    query = query,
                    onQueryChange = viewModel::onQueryChanged,
                    filter = viewModel::filterItems,
                    total = s.stats.totalGames,
                    installed = s.stats.installed,
                    mobileFriendly = s.stats.mobileFriendly,
                    totalValue = s.stats.totalValue
                )
            }
        }
    }
}

@Composable
private fun LoginPrompt(onLogin: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SportsEsports,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "登录 Epic 账号",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "登录后查看你拥有的游戏、统计收藏价值、追踪安装状态。",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onLogin) {
            Icon(Icons.Default.Login, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("使用 Epic 账号登录")
        }
    }
}

@Composable
private fun InventoryContent(
    items: List<InventoryItemUi>,
    query: String,
    onQueryChange: (String) -> Unit,
    filter: (List<InventoryItemUi>) -> List<InventoryItemUi>,
    total: Int,
    installed: Int,
    mobileFriendly: Int,
    totalValue: Int
) {
    Column(Modifier.fillMaxSize()) {
        StatsCard(total, installed, mobileFriendly, totalValue)
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("搜索游戏 / 标签…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val visible = filter(items)
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(visible, key = { it.catalogId }) { GameCard(it) }
        }
    }
}

@Composable
private fun StatsCard(total: Int, installed: Int, mobileFriendly: Int, totalValue: Int) {
    ElevatedCard(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("我的 Epic 库存", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatColumn("拥有游戏", total.toString())
                StatColumn("已安装", installed.toString())
                StatColumn("移动端支持", mobileFriendly.toString())
                StatColumn("有定价", totalValue.toString())
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GameCard(item: InventoryItemUi) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(96.dp, 56.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (item.coverUrl != null) {
                    AsyncImage(
                        model = item.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null,
                        tint = Color.Gray)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        maxLines = 1)
                    if (item.isInstalled) {
                        Spacer(Modifier.width(6.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("已安装", fontSize = 10.sp) }
                        )
                    }
                }
                if (item.tags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(item.tags.joinToString(" · "),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    item.priceText?.takeIf { it.isNotBlank() }?.let { price ->
                        AssistChip(onClick = {}, label = { Text(price, fontSize = 11.sp) })
                        Spacer(Modifier.width(6.dp))
                    }
                    val ctx = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = { openInStore(ctx, item) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("下载/打开", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

private fun openInStore(ctx: android.content.Context, item: InventoryItemUi) {
    val browser = android.content.Intent(
        android.content.Intent.ACTION_VIEW,
        android.net.Uri.parse(
            "https://store.epicgames.com/zh-CN/product/${item.tags.firstOrNull() ?: ""}/" +
                item.title.lowercase().replace("[^a-z0-9]+".toRegex(), "-").trim('-')
        )
    )
    browser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { ctx.startActivity(browser) }
}

@Composable
private fun ErrorBox(message: String) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null,
            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text("出错了：$message", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
