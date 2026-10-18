package com.example.epicinv.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.epicinv.data.stats.StatsReport
import com.example.epicinv.ui.InventoryViewModel

@Composable
fun StatsScreen(vm: InventoryViewModel) {
    val state by vm.statsState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.refreshStats() }

    when (val s = state) {
        is StatsState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is StatsState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("出错了：${s.message}")
        }
        is StatsState.Loaded -> StatsContent(s.report)
    }
}

@Composable
private fun StatsContent(report: StatsReport) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OverviewCard(report)
        RarityCard(report)
        RecentUnlocksCard(report)
        YearlyCard(report)
    }
}

@Composable
private fun OverviewCard(r: StatsReport) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("成就概览", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatBox("已解锁", r.totalAchievementsUnlocked.toString())
                StatBox("总解锁率", "${(r.completionRate * 100).toInt()}%")
                StatBox("库游戏数", r.totalGames.toString())
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RarityCard(r: StatsReport) {
    if (r.topRareAchievements.isEmpty()) return
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, null)
                Spacer(Modifier.width(8.dp))
                Text("最稀有的成就", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            r.topRareAchievements.forEach { (name, rarity) ->
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text("少于 ${"%.1f".format(rarity)}% 的玩家解锁",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LinearProgressIndicator(
                    progress = { rarity.toFloat() / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentUnlocksCard(r: StatsReport) {
    if (r.recentUnlocks.isEmpty()) return
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null)
                Spacer(Modifier.width(8.dp))
                Text("最近解锁", fontWeight = FontWeight.SemiBold)
            }
            r.recentUnlocks.forEach { (name, ts) ->
                Spacer(Modifier.height(8.dp))
                val text = if (ts > 0)
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",
                        java.util.Locale.getDefault())
                        .format(java.util.Date(ts)) else "—"
                Column {
                    Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun YearlyCard(r: StatsReport) {
    if (r.earnedByYear.isEmpty()) return
    val maxCount = r.earnedByYear.values.max()
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("年度解锁分布", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            r.earnedByYear.toSortedMap().forEach { (year, count) ->
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(year.toString(), fontSize = 12.sp, modifier = Modifier.width(64.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(count.toFloat() / maxCount.toFloat())
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(count.toString(), fontSize = 11.sp)
                }
            }
        }
    }
}
