package com.heathealth.app.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.stringResource
import com.heathealth.app.R
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextSecondary
import com.heathealth.app.viewmodel.DashboardViewModel

sealed class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", R.string.nav_home, Icons.Default.Home)
    object Hydration : BottomNavItem("hydration", R.string.nav_hydration, Icons.Default.WaterDrop)
    object RiskMap : BottomNavItem("risk_map", R.string.nav_cool_map, Icons.Default.Map)
    object Alerts : BottomNavItem("alerts", R.string.nav_alerts, Icons.Default.Notifications)
    object Safety : BottomNavItem("safety", R.string.nav_safety, Icons.Default.HealthAndSafety)
    object Profile : BottomNavItem("profile", R.string.nav_profile, Icons.Default.Person)
}

@Composable
fun MainScreen(
    viewModel: DashboardViewModel = viewModel(),
    initialRoute: String = BottomNavItem.Home.route,
    selectedLanguage: String = "en",
    onLanguageSelected: (String) -> Unit = {}
) {
    val navController = rememberNavController()
    var isFullscreenMap by remember { mutableStateOf(false) }

    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Hydration,
        BottomNavItem.RiskMap,
        BottomNavItem.Safety,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val validStartDestination = if (navItems.any { it.route == initialRoute }) initialRoute else BottomNavItem.Home.route

    Scaffold(
        bottomBar = {
            if (!isFullscreenMap) {
                NavigationBar(
                    containerColor = SurfaceWhite
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        val labelText = stringResource(item.titleResId)
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
                                    contentDescription = labelText,
                                    tint = if (isSelected) PrimaryTeal else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = labelText,
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = validStartDestination,
            modifier = Modifier.padding(if (isFullscreenMap) PaddingValues(0.dp) else innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToCoolMap = {
                        navController.navigate(BottomNavItem.RiskMap.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToRiskHistory = {
                        navController.navigate("risk_history")
                    },
                    onNavigateToAlerts = {
                        navController.navigate("alerts")
                    }
                )
            }
            composable("risk_history") {
                RiskHistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAlerts = { navController.navigate("alerts") }
                )
            }
            composable(BottomNavItem.Hydration.route) {
                WaterTrackerScreen(
                    onNavigateToAlerts = { navController.navigate("alerts") }
                )
            }
            composable(BottomNavItem.RiskMap.route) {
                CampusCoolMapScreen(
                    onFullscreenStateChanged = { isFullscreenMap = it },
                    onNavigateToAlerts = { navController.navigate("alerts") }
                )
            }
            composable("alerts") {
                AlertsScreen()
            }
            composable(BottomNavItem.Safety.route) {
                SafetyScreen(
                    onNavigateToAlerts = { navController.navigate("alerts") }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onNavigateToAlerts = { navController.navigate("alerts") }
                )
            }
        }
    }
}
