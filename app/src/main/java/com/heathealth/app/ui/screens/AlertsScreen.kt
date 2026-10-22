package com.heathealth.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.HeatAmber
import com.heathealth.app.ui.theme.HeatOrange
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary
import com.heathealth.app.worker.WorkManagerScheduler
import kotlinx.coroutines.launch

data class HeatAlertItem(
    val id: String,
    val title: String,
    val timeLabel: String,
    val description: String,
    val severityColor: Color
)

@Composable
fun AlertsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }
    val userPrefs by prefsRepo.userPreferencesFlow.collectAsState(
        initial = com.heathealth.app.data.local.datastore.UserPreferences()
    )

    val activeAlerts = listOf(
        HeatAlertItem(
            id = "1",
            title = "Peak Afternoon Extreme Heat Advisory",
            timeLabel = "12:00 PM - 4:00 PM",
            description = "Apparent temperature expected to exceed 38°C. Rest in shaded or ventilated indoor areas.",
            severityColor = HeatOrange
        ),
        HeatAlertItem(
            id = "2",
            title = "Elevated Humidity Strain Warning",
            timeLabel = "Current Observation",
            description = "High relative humidity (>=65%) restricts natural sweat evaporation.",
            severityColor = HeatAmber
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
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
                    text = "Heat Early Warning Alerts",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Text(
                    text = "Timely notifications and environmental safety advisories",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Disclaimer Note
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = "Early Warning System Notice: Background alerts check live risk levels periodically and avoid duplicate alerts for unchanged conditions. Always follow official public health warnings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Text(
            text = "Active Environmental Advisories",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        activeAlerts.forEach { alert ->
            AlertCardItem(alert = alert)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Heat Alert Notification Preferences",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

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
                PreferenceToggleRow(
                    title = "Enable Local Heat Alerts",
                    subtitle = "Notify when local thermal risk crosses your selected threshold",
                    checked = userPrefs.heatAlertsEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            prefsRepo.updateHeatAlertsEnabled(enabled)
                            if (enabled) {
                                WorkManagerScheduler.scheduleHeatAlertChecks(context)
                            } else {
                                WorkManagerScheduler.cancelHeatAlertChecks(context)
                            }
                        }
                    }
                )

                Text(
                    text = "Alert Trigger Threshold:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Moderate", "High", "Extreme").forEach { threshold ->
                        val isSelected = userPrefs.alertThreshold.equals(threshold, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryTeal else PrimaryTealLight)
                                .clickable {
                                    scope.launch {
                                        prefsRepo.updateAlertThreshold(threshold)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = threshold,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) SurfaceWhite else PrimaryTeal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertCardItem(
    alert: HeatAlertItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = alert.severityColor.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = alert.severityColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }

            Text(
                text = "Timeframe: ${alert.timeLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = alert.severityColor
            )

            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = SurfaceWhite, checkedTrackColor = PrimaryTeal)
        )
    }
}
