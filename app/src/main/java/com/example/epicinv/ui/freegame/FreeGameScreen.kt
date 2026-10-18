package com.example.epicinv.ui.freegame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.epicinv.data.freegame.FreeGameItem
import com.example.epicinv.ui.FreeGameState
import com.example.epicinv.ui.InventoryViewModel

@Composable
fun FreeGameScreen(vm: InventoryViewModel) {
    val state by vm.freeState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.refreshFreeGames() }

    Column(Modifier.fillMaxSize()) {
        when (val s = state) {
            is FreeGameState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            is FreeGameState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("出错：${s.message}")
            }
            is FreeGameState.Loaded -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(s.items, key = { it.startDate }) { FreeGameCard(it) }
            }
        }
    }
}

@Composable
private fun FreeGameCard(item: FreeGameItem) {
    val color = when (item.status) {
        FreeGameItem.Status.CURRENT -> MaterialTheme.colorScheme.primary
        FreeGameItem.Status.UPCOMING -> MaterialTheme.colorScheme.tertiary
        FreeGameItem.Status.ENDED -> Color.Gray
    }
    val fmt = remember {
        java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.getDefault())
    }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(80.dp, 60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.coverUrl != null) {
                    AsyncImage(model = item.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.CardGiftcard, null, tint = color)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    maxLines = 2)
                Spacer(Modifier.height(4.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            when (item.status) {
                                FreeGameItem.Status.CURRENT -> "可领取"
                                FreeGameItem.Status.UPCOMING -> "即将免费"
                                FreeGameItem.Status.ENDED -> "已结束"
                            }, fontSize = 11.sp
                        )
                    }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${fmt.format(java.util.Date(item.startDate))} → " +
                        fmt.format(java.util.Date(item.endDate)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
