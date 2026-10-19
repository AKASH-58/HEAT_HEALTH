package com.heathealth.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextSecondary
import com.heathealth.app.viewmodel.DashboardViewModel

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object RiskMap : BottomNavItem("risk_map", "Risk Map", Icons.Default.Map)
    object Alerts : BottomNavItem("alerts", "Alerts", Icons.Default.Notifications)
    object Safety : BottomNavItem("safety", "Safety", Icons.Default.HealthAndSafety)
}

@Composable
fun MainScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.RiskMap,
        BottomNavItem.Alerts,
        BottomNavItem.Safety
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceWhite
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) PrimaryTeal else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                color = if (isSelected) PrimaryTeal else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = PrimaryTeal.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                DashboardScreen(viewModel = viewModel)
            }
            composable(BottomNavItem.RiskMap.route) {
                RiskMapScreen(viewModel = viewModel)
            }
            composable(BottomNavItem.Alerts.route) {
                AlertsScreen()
            }
            composable(BottomNavItem.Safety.route) {
                SafetyScreen()
            }
        }
    }
}
