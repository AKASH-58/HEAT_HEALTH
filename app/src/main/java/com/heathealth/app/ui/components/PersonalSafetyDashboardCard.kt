package com.heathealth.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.heathealth.app.R
import com.heathealth.app.data.local.entity.AlertLogEntity
import com.heathealth.app.data.local.entity.HeatRiskLogEntity
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.model.RiskPrediction
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.HeatAmber
import com.heathealth.app.ui.theme.HeatOrange
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary

@Composable
fun PersonalSafetyDashboardCard(
    latestRisk: RiskPrediction?,
    recentRiskLogs: List<HeatRiskLogEntity>,
    recentAlertLogs: List<AlertLogEntity>,
    todayWaterIntakeMl: Int,
    targetWaterIntakeMl: Int,
    weeklyWaterTotalMl: Int,
    weeklyWaterDaysCount: Int
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
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryTealLight)
                        .padding(6.dp)
                )

                Column {
                    Text(
                        text = stringResource(R.string.personal_safety_dashboard),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.personal_safety_dashboard_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Section 1: Latest Risk Status
            latestRisk?.let { risk ->
                val riskColor = when (risk.level) {
                    RiskLevel.LOW -> PrimaryTeal
                    RiskLevel.MODERATE -> HeatAmber
                    RiskLevel.HIGH, RiskLevel.VERY_HIGH, RiskLevel.EXTREME -> HeatOrange
                    else -> PrimaryTeal
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.current_heat_risk_state),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = risk.level.getLocalizedName(),
                                style = MaterialTheme.typography.titleMedium,
                                color = riskColor
                            )
                        }

                        Text(
                            text = "Score: ${risk.score ?: "--"}°C",
                            style = MaterialTheme.typography.titleSmall,
                            color = riskColor
                        )
                    }
                }
            }

            // Section 2: Hydration Overview & Weekly Summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryTealLight.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.todays_hydration_progress),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }

                val progress = if (targetWaterIntakeMl > 0) {
                    (todayWaterIntakeMl.toFloat() / targetWaterIntakeMl.toFloat()).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = PrimaryTeal,
                    trackColor = BorderLight
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$todayWaterIntakeMl / $targetWaterIntakeMl mL (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )

                    val days = if (weeklyWaterDaysCount > 0) weeklyWaterDaysCount else 1
                    val avgDaily = weeklyWaterTotalMl / days
                    Text(
                        text = "Weekly Total: ${(weeklyWaterTotalMl / 1000.0).let { "%.1f".format(it) }}L (Avg: ${avgDaily}mL/day)",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryTeal
                    )
                }
            }

            // Section 3: Recent Risk Predictions History
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.recent_recorded_risk_assessments),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }

                if (recentRiskLogs.isNotEmpty()) {
                    recentRiskLogs.take(3).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = log.locationLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${log.temperatureCelsius}°C (${log.humidityPercent}% RH)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Text(
                                text = RiskLevel.fromDisplayName(log.riskLevel).getLocalizedName(),
                                style = MaterialTheme.typography.labelLarge,
                                color = PrimaryTeal
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.no_recent_risk_predictions),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Section 4: Recent Heat Alerts History
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.recent_triggered_heat_alerts),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }

                if (recentAlertLogs.isNotEmpty()) {
                    recentAlertLogs.take(3).forEach { alert ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HeatOrange.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = HeatOrange
                            )
                            Text(
                                text = alert.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.no_local_heat_alerts),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
