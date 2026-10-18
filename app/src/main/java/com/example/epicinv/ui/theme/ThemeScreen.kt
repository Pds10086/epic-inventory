package com.example.epicinv.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.epicinv.ui.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    vm: InventoryViewModel,
    onAccountsClicked: () -> Unit,
    onAboutClicked: () -> Unit
) {
    val mode by vm.themeMode.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Brightness6, null)
            Spacer(Modifier.width(8.dp))
            Text("外观主题", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }
        Text("明暗主题可在系统内自动跟随，也可锁定为浅色/深色。",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        listOf(
            ThemeMode.SYSTEM to "跟随系统（推荐）",
            ThemeMode.LIGHT to "始终浅色",
            ThemeMode.DARK to "始终深色"
        ).forEach { (m, label) ->
            val selected = mode == m
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { vm.setTheme(m) }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontSize = 14.sp)
                    RadioButton(selected = selected, onClick = { vm.setTheme(m) })
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAccountsClicked)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null)
                    Spacer(Modifier.width(12.dp))
                    Text("账号管理", fontSize = 14.sp)
                }
                Icon(Icons.Default.ChevronRight, null)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAboutClicked)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null)
                    Spacer(Modifier.width(12.dp))
                    Text("关于", fontSize = 14.sp)
                }
                Icon(Icons.Default.ChevronRight, null)
            }
        }
    }
}
