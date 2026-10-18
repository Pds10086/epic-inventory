package com.example.epicinv

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.epicinv.ui.AppShell
import com.example.epicinv.ui.InventoryViewModel

class MainActivity : ComponentActivity() {

    private val vm: InventoryViewModel by viewModels {
        InventoryViewModel.factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (vm.isLoggedIn()) vm.refresh()
        handleDeepLink(intent)

        setContent {
            Surface(Modifier) { AppShell(vm) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(i: Intent?) {
        val data = i?.data ?: return
        val code = data.getQueryParameter("code") ?: return
        vm.completeAuthorization(code, BuildConfig.EPIC_CLIENT_ID)
    }

    companion object {
        fun openIntent(ctx: Context): android.app.PendingIntent {
            val i = Intent(ctx, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return android.app.PendingIntent.getActivity(
                ctx, 0, i,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
