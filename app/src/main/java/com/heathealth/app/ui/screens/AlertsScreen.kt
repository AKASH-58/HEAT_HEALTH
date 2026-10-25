package com.heathealth.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heathealth.app.R
import com.heathealth.app.data.local.entity.AlertLogEntity
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.network.dto.UpcomingForecastItemDto
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.HeatAmber
import com.heathealth.app.ui.theme.HeatGreen
import com.heathealth.app.ui.theme.HeatOrange
import com.heathealth.app.ui.theme.HeatRed
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary
import com.heathealth.app.util.NotificationHelper
import com.heathealth.app.viewmodel.AlertsViewModel

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel()
) {
    val context = LocalContext.current

    val alertsUiState by viewModel.alertsUiState.collectAsState()
    val upcomingForecastState by viewModel.upcomingForecastState.collectAsState()
    val alertHistory by viewModel.alertHistory.collectAsState()
    val userPrefs by viewModel.userPreferences.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val hasNotificationPermission = NotificationHelper.hasNotificationPermission(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar / Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alerts",
                    tint = PrimaryTeal,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryTealLight)
                        .padding(8.dp)
                )

                Column {
                    Text(
                        text = stringResource(R.string.smart_heat_alerts),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.smart_heat_alerts_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { viewModel.refreshAlerts() },
                    enabled = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = PrimaryTeal,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Data",
                            tint = PrimaryTeal
                        )
                    }
                }
            }
        }

        // Section A: Current Smart Heat Dashboard Card
        Text(
            text = stringResource(R.string.live_environmental_thermal_risk),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        SmartHeatDashboardCard(
            uiState = alertsUiState,
            onRetry = { viewModel.refreshAlerts() }
        )

        // Section B: Upcoming Heat-Risk Forecast
        Text(
            text = stringResource(R.string.upcoming_high_risk_periods),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        UpcomingForecastSection(forecastState = upcomingForecastState)

        // Section C: Alert History
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.alert_history_advisories),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            if (alertHistory.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearAlertHistory() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        AlertHistorySection(alertLogs = alertHistory)

        // Section D: Notification Preferences & System Settings
        Text(
            text = stringResource(R.string.notification_preferences_controls),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        NotificationPreferencesCard(
            hasPermission = hasNotificationPermission,
            heatAlertsEnabled = userPrefs.heatAlertsEnabled,
            alertThreshold = userPrefs.alertThreshold,
            reminderFrequency = userPrefs.reminderFrequencyHours,
            onToggleEnabled = { enabled -> viewModel.updateHeatAlertsEnabled(context, enabled) },
            onThresholdSelected = { threshold -> viewModel.updateAlertThreshold(threshold) },
            onFrequencySelected = { freq -> viewModel.updateReminderFrequencyHours(freq) },
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            },
            onTestAlert = { viewModel.triggerLocalTestNotification(context) }
        )
    }
}

@Composable
private fun SmartHeatDashboardCard(
    uiState: com.heathealth.app.data.model.DashboardUiState,
    onRetry: () -> Unit
) {
    val risk = uiState.riskPrediction.level
    val riskColor = getRiskColor(risk)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = PrimaryTeal, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Evaluating heat health metrics...", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            } else if (uiState.errorMessage != null && uiState.weatherData.temperatureCelsius == null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = HeatOrange,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Prediction Data Unavailable",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = uiState.errorMessage ?: "Unable to connect to weather & prediction backend.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedButton(onClick = onRetry) {
                        Text(text = "Retry", color = PrimaryTeal)
                    }
                }
            } else {
                // Live Risk Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(riskColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${risk.displayName} Risk",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = riskColor
                            )
                        }

                        Text(
                            text = uiState.locationLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    if (uiState.lastUpdated.isNotEmpty()) {
                        Text(
                            text = "Updated ${uiState.lastUpdated.take(16).replace("T", " ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // Weather Summary Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryTealLight)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${uiState.weatherData.feelsLikeCelsius ?: uiState.weatherData.temperatureCelsius ?: "--"}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text(text = "Feels Like", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${uiState.weatherData.humidityPercent ?: "--"}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text(text = "Humidity", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.riskPrediction.method, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PrimaryTeal)
                        Text(text = "Engine", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                // Risk Explanation / Factors
                if (uiState.factors.isNotEmpty()) {
                    Text(
                        text = "Contributing Risk Factors:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    uiState.factors.forEach { factor ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "•", style = MaterialTheme.typography.bodySmall, color = riskColor)
                            Text(text = factor, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }

                HorizontalDivider(color = BorderLight)

                // Safety Recommendations
                Text(
                    text = "Recommended Precautions:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                uiState.recommendations.take(3).forEach { rec ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(text = rec.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(text = rec.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingForecastSection(
    forecastState: com.heathealth.app.viewmodel.UpcomingForecastUiState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (forecastState.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = PrimaryTeal, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Loading hourly forecast data...", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            } else if (!forecastState.isAvailable || forecastState.items.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Text(
                        text = forecastState.errorMessage ?: "No upcoming high-risk periods detected from weather forecast provider.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            } else {
                forecastState.items.take(4).forEach { item ->
                    UpcomingForecastRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun UpcomingForecastRow(item: UpcomingForecastItemDto) {
    val riskLevel = RiskLevel.fromDisplayName(item.riskLevel)
    val riskColor = getRiskColor(riskLevel)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(riskColor.copy(alpha = 0.08f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = riskColor, modifier = Modifier.size(18.dp))
            Column {
                Text(text = item.timePeriod, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    text = "${item.temperatureC}°C (${item.apparentTemperatureC ?: item.temperatureC}°C feels-like) • ${item.humidityPercent.toInt()}% RH",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(riskColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = item.riskLevel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SurfaceWhite)
        }
    }
}

@Composable
private fun AlertHistorySection(
    alertLogs: List<AlertLogEntity>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (alertLogs.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    Text(text = "No historical heat risk alerts recorded.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            } else {
                alertLogs.forEach { alert ->
                    AlertHistoryRow(alert = alert)
                }
            }
        }
    }
}

@Composable
private fun AlertHistoryRow(alert: AlertLogEntity) {
    val riskLevel = RiskLevel.fromDisplayName(alert.riskLevel)
    val riskColor = getRiskColor(riskLevel)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(riskColor.copy(alpha = 0.06f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = alert.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(riskColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = alert.riskLevel, style = MaterialTheme.typography.labelSmall, color = SurfaceWhite)
            }
        }

        Text(text = alert.description, style = MaterialTheme.typography.bodySmall, color = TextPrimary)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Source: ${alert.triggerSource}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(text = alert.isoTimestamp.take(16).replace("T", " "), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun NotificationPreferencesCard(
    hasPermission: Boolean,
    heatAlertsEnabled: Boolean,
    alertThreshold: String,
    reminderFrequency: Int,
    onToggleEnabled: (Boolean) -> Unit,
    onThresholdSelected: (String) -> Unit,
    onFrequencySelected: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onTestAlert: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Permission Status Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (hasPermission) PrimaryTealLight else HeatOrange.copy(alpha = 0.12f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (hasPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (hasPermission) PrimaryTeal else HeatOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (hasPermission) "Notification Permission Granted" else "Notification Permission Required",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (hasPermission) "Android notifications enabled" else "Tap below to enable system permissions",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(onClick = onOpenSettings) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Open Settings", tint = PrimaryTeal)
                }
            }

            // Toggle Local Heat Alerts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Enable Smart Heat Alerts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(text = "Periodically check live risk and alert on threshold escalation", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }

                Switch(
                    checked = heatAlertsEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(checkedThumbColor = SurfaceWhite, checkedTrackColor = PrimaryTeal)
                )
            }

            HorizontalDivider(color = BorderLight)

            // Alert Threshold Selector
            Text(text = "Alert Trigger Threshold:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Moderate", "High", "Extreme").forEach { threshold ->
                    val isSelected = alertThreshold.equals(threshold, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryTeal else PrimaryTealLight)
                            .clickable { onThresholdSelected(threshold) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = threshold, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isSelected) SurfaceWhite else PrimaryTeal)
                    }
                }
            }

            // Reminder Interval Selector
            Text(text = "Check Frequency / Cooldown:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1 to "1 Hour", 2 to "2 Hours", 4 to "4 Hours").forEach { (hours, label) ->
                    val isSelected = reminderFrequency == hours
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryTeal else PrimaryTealLight)
                            .clickable { onFrequencySelected(hours) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isSelected) SurfaceWhite else PrimaryTeal)
                    }
                }
            }

            // Trigger Test Notification Button
            Button(
                onClick = onTestAlert,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Trigger Sample Heat Alert", color = SurfaceWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun getRiskColor(level: RiskLevel): Color {
    return when (level) {
        RiskLevel.LOW -> HeatGreen
        RiskLevel.MODERATE -> HeatAmber
        RiskLevel.HIGH -> HeatOrange
        RiskLevel.EXTREME -> HeatRed
        else -> PrimaryTeal
    }
}
