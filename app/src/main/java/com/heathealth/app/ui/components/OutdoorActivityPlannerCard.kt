package com.heathealth.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.heathealth.app.data.model.HourlyForecast
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.HeatAmber
import com.heathealth.app.ui.theme.HeatOrange
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OutdoorActivityPlannerCard(
    hourlyForecast: List<HourlyForecast>
) {
    val activities = listOf("Walking to Class", "Outdoor Sports", "Campus Commute", "Fieldwork")
    var selectedActivity by remember { mutableStateOf("Walking to Class") }

    val defaultHour = hourlyForecast.firstOrNull()?.time ?: "14:00"
    var selectedTime by remember { mutableStateOf(defaultHour) }

    val matchedForecast = hourlyForecast.find { it.time.equals(selectedTime, ignoreCase = true) || it.time.contains(selectedTime) }

    val alternativeCoolerTimes = hourlyForecast.filter {
        it.riskLevel == RiskLevel.LOW || it.riskLevel == RiskLevel.MODERATE
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
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
                        text = "Student Outdoor Activity Planner",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Plan safe outdoor times based on hourly heat risk predictions",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "Select Planned Activity:",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activities.forEach { act ->
                    val isSelected = act == selectedActivity
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryTeal else PrimaryTealLight)
                            .clickable { selectedActivity = act }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = act,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) SurfaceWhite else PrimaryTeal
                        )
                    }
                }
            }

            Text(
                text = "Select Planned Time:",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            if (hourlyForecast.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hourlyForecast.take(8).forEach { forecast ->
                        val isSelected = forecast.time == selectedTime
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) PrimaryTeal else BorderLight,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(if (isSelected) PrimaryTealLight else SurfaceWhite)
                                .clickable { selectedTime = forecast.time }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = forecast.time,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) PrimaryTeal else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (matchedForecast != null) {
                val risk = matchedForecast.riskLevel
                val riskColor = when (risk) {
                    RiskLevel.LOW -> PrimaryTeal
                    RiskLevel.MODERATE -> HeatAmber
                    RiskLevel.HIGH, RiskLevel.VERY_HIGH, RiskLevel.EXTREME -> HeatOrange
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = riskColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Planned Time: ${matchedForecast.time}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                            }

                            Text(
                                text = risk.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = riskColor
                            )
                        }

                        Text(
                            text = "Condition: ${matchedForecast.temperatureCelsius}°C (${matchedForecast.humidityPercent}% RH, Feels like ${matchedForecast.feelsLikeCelsius}°C)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )

                        val actionRecommendation = when (risk) {
                            RiskLevel.LOW -> "Good conditions for $selectedActivity. Maintain normal hydration."
                            RiskLevel.MODERATE -> "Proceed with caution. Walk in shade, carry a water bottle, and take rest breaks."
                            RiskLevel.HIGH -> "High heat risk window. Consider rescheduling $selectedActivity to a cooler hour or reducing outdoor intensity."
                            RiskLevel.VERY_HIGH, RiskLevel.EXTREME -> "Avoid strenuous outdoor activity during this window. Reschedule to early morning or evening."
                        }

                        Text(
                            text = "Recommendation: $actionRecommendation",
                            style = MaterialTheme.typography.bodySmall,
                            color = riskColor
                        )
                    }
                }

                if (risk == RiskLevel.HIGH || risk == RiskLevel.VERY_HIGH || risk == RiskLevel.EXTREME) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Suggested Cooler Alternative Times:",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryTeal
                        )

                        if (alternativeCoolerTimes.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                alternativeCoolerTimes.take(4).forEach { alt ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryTealLight)
                                            .clickable { selectedTime = alt.time }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${alt.time} (${alt.temperatureCelsius}°C - ${alt.riskLevel.displayName})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PrimaryTeal
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No significantly cooler hours found in current 24-hour forecast.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Future risk estimate is unavailable for the selected date/time beyond the 24-hour forecast window.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
