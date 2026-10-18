package com.example.epicinv.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
fun AccountScreen(vm: InventoryViewModel, onAddAccount: () -> Unit) {
    val accounts by vm.accountList.collectAsStateWithLifecycle()
    val activeId by vm.activeAccountId.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.refreshAccountList() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("已登录的账号", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Button(onClick = onAddAccount) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(4.dp))
                Text("添加账号")
            }
        }
        Spacer(Modifier.height(8.dp))
        if (accounts.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("还没有账号，点击右上角添加")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts, key = { it.accountId ?: it.clientId ?: it.refreshToken.orEmpty() }) { acc ->
                    val id = acc.accountId ?: acc.clientId ?: return@items
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, null)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("账号 ${id.take(10)}…",
                                    fontWeight = FontWeight.Medium)
                                Text(
                                    "过期：${(System.currentTimeMillis() + acc.expiresIn * 1000L)
                                        .let { java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
                                            .format(java.util.Date(it)) }}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (activeId == id) {
                                Icon(Icons.Default.Check, null,
                                    tint = MaterialTheme.colorScheme.primary)
                            } else {
                                TextButton(onClick = { vm.switchAccount(id) }) {
                                    Text("切换")
                                }
                            }
                            IconButton(onClick = { vm.removeAccount(id) }) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                    }
                }
            }
        }
    }
}
