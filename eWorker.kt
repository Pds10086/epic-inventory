package com.example.epicinv.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.epicinv.ui.auth.AuthRoute
import com.example.epicinv.ui.freegame.FreeGameScreen
import com.example.epicinv.ui.settings.AboutScreen
import com.example.epicinv.ui.settings.AccountScreen
import com.example.epicinv.ui.stats.StatsScreen
import com.example.epicinv.ui.theme.ThemeMode
import com.example.epicinv.ui.theme.ThemeScreen

private sealed class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Inventory : Tab("inv", "库存", Icons.Default.SportsEsports)
    data object FreeGames : Tab("free", "周免", Icons.Default.CardGiftcard)
    data object Stats : Tab("stats", "统计", Icons.Default.BarChart)
    data object Settings : Tab("settings", "设置", Icons.Default.Settings)
    data object Accounts : Tab("accounts", "账号", Icons.Default.Person)
    data object About : Tab("about", "关于", Icons.Default.Info)

    companion object {
        val main = listOf(Inventory, FreeGames, Stats, Settings)
    }
}

@Composable
fun AppShell(vm: InventoryViewModel) {
    val theme by vm.themeMode.collectAsStateWithLifecycle(
        initialValue = ThemeMode.SYSTEM
    )
    val systemDark = isSystemInDarkTheme()
    val useDark = when (theme) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    MaterialTheme(
        colorScheme = if (useDark) darkColorScheme() else lightColorScheme()
    ) {
        val nav = rememberNavController()
        AppShellContent(vm, nav)
    }
}

@Composable
private fun AppShellContent(
    vm: InventoryViewModel,
    nav: NavHostController
) {
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            val showBar = currentRoute != null && currentRoute != "auth"
            if (showBar) {
                NavigationBar {
                    Tab.main.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    nav.navigate(tab.route) {
                                        popUpTo(nav.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Tab.Inventory.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tab.Inventory.route) {
                InventoryScreen(vm, onLoginRequested = {
                    nav.navigate("auth")
                })
            }
            composable(Tab.FreeGames.route) { FreeGameScreen(vm) }
            composable(Tab.Stats.route) { StatsScreen(vm) }
            composable(Tab.Settings.route) {
                ThemeScreen(
                    vm,
                    onAccountsClicked = { nav.navigate(Tab.Accounts.route) },
                    onAboutClicked = { nav.navigate(Tab.About.route) }
                )
            }
            composable(Tab.Accounts.route) {
                AccountScreen(vm, onAddAccount = { nav.navigate("auth") })
            }
            composable(Tab.About.route) { AboutScreen() }
            composable("auth") {
                AuthRoute(
                    viewModel = vm,
                    onClose = { nav.popBackStack() }
                )
            }
        }
    }
}
