package com.example.epicinv.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.epicinv.MainActivity
import com.example.epicinv.data.EpicRepository
import com.example.epicinv.data.InventoryUiState
import com.example.epicinv.data.api.EpicApi
import com.example.epicinv.data.auth.TokenStore

class InventoryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: androidx.glance.appwidget.GlanceId) {
        val state = runCatching {
            EpicRepository(EpicApi(), TokenStore(context)).loadInventory()
        }.getOrElse { InventoryUiState.Error(it.message ?: "未知错误") }

        provideContent {
            WidgetContent(state)
        }
    }

    @Composable
    private fun WidgetContent(state: InventoryUiState) {
        val openApp = actionStartActivity<MainActivity>()
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(16.dp)
                .padding(12.dp)
                .clickable(openApp)
        ) {
            when (state) {
                is InventoryUiState.NeedLogin -> Column {
                    Text("Epic 库存", fontSize = 14.sp,
                        style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer,
                            fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(8.dp))
                    Text("登录后查看你的库存",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
                }
                is InventoryUiState.Error -> Text("出错：${state.message}",
                    style = TextStyle(color = GlanceTheme.colors.error))
                is InventoryUiState.Loaded -> Column {
                    val total = state.stats.totalGames
                    val installed = state.stats.installed
                    Text("Epic 库存",
                        fontSize = 16.sp,
                        style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer,
                            fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(4.dp))
                    Text("共 $total 款游戏 · 已安装 $installed",
                        fontSize = 12.sp,
                        style = TextStyle(color = GlanceTheme.colors.onSurface))
                    Spacer(Modifier.height(6.dp))
                    val firstThree = state.items.take(3)
                    for (item in firstThree) {
                        Text("· ${item.title.take(16)}", fontSize = 12.sp,
                            style = TextStyle(color = GlanceTheme.colors.onSurface))
                    }
                }
            }
        }
    }
}

class InventoryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = InventoryWidget()
}
