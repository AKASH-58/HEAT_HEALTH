package com.heathealth.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.HeatOrange
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary

data class HeatAlertItem(
    val id: String,
    val title: String,
    val timeLabel: String,
    val description: String,
    val severityColor: Color
)

@Composable
fun AlertsScreen() {
    var extremeHeatAlertsEnabled by remember { mutableStateOf(true) }
    var humidityAdvisoriesEnabled by remember { mutableStateOf(true) }
    var uvWarningsEnabled by remember { mutableStateOf(false) }

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
            severityColor = Color(0xFFF59E0B)
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

        // Prototype Disclaimer Note
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
                    text = "Phase 2 Early Warning Notice: Background notifications and preference toggles simulate local early warning logic. Follow official public health alerts.",
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

        // Active Alerts List
        activeAlerts.forEach { alert ->
            AlertCardItem(alert = alert)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Early Warning Notification Preferences",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        // Preferences Card
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
                    title = "Extreme Heat Warnings",
                    subtitle = "Alert when apparent temperature exceeds 35°C",
                    checked = extremeHeatAlertsEnabled,
                    onCheckedChange = { extremeHeatAlertsEnabled = it }
                )

                PreferenceToggleRow(
                    title = "High Relative Humidity Advisories",
                    subtitle = "Notify when humidity reduces sweat cooling efficiency",
                    checked = humidityAdvisoriesEnabled,
                    onCheckedChange = { humidityAdvisoriesEnabled = it }
                )

                PreferenceToggleRow(
                    title = "Peak Hours UV Radiation Warnings",
                    subtitle = "Reminders for sun protection during midday peak",
                    checked = uvWarningsEnabled,
                    onCheckedChange = { uvWarningsEnabled = it }
                )
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
