package com.example.epicinv.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.epicinv.BuildConfig

@Composable
fun AboutScreen() {
    val ctx = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null)
            Spacer(Modifier.width(8.dp))
            Text("关于", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }

        Text("Epic 库存", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text("版本 ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Text("本应用是独立第三方工具，调用 Epic Games 公开 API 展示你的游戏库存，不修改、不打包任何官方客户端。",
            fontSize = 13.sp)

        Spacer(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()
            .clickable { openUrl(ctx, "https://github.com/yourname/epic-inventory") }) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, null)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("源代码", fontSize = 14.sp)
                    Text("GitHub 仓库", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()
            .clickable { openUrl(ctx, "https://www.epicgames.com/site/en-US/tos") }) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Policy, null)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Epic 服务条款", fontSize = 14.sp)
                    Text("epicgames.com/site/en-US/tos", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun openUrl(ctx: android.content.Context, url: String) {
    val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { ctx.startActivity(i) }
}
