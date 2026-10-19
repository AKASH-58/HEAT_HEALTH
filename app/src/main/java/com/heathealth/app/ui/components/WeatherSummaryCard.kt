package com.heathealth.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.heathealth.app.data.model.WeatherData
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.HeatOrange
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary

@Composable
fun WeatherSummaryCard(
    weatherData: WeatherData,
    locationLabel: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weather Summary",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    if (locationLabel.isNotEmpty()) {
                        Text(
                            text = locationLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                if (weatherData.isDemoData) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryTealLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Demo Badge",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "DEMONSTRATION DATA",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryTeal
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryTeal.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Live Data Badge",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "LIVE DATA",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherMetricItem(
                    icon = Icons.Default.Thermostat,
                    label = "Temp",
                    value = if (weatherData.temperatureCelsius != null) "${weatherData.temperatureCelsius}°C" else "N/A",
                    iconColor = HeatOrange,
                    modifier = Modifier.weight(1f)
                )

                WeatherMetricItem(
                    icon = Icons.Default.WbSunny,
                    label = "Feels Like",
                    value = if (weatherData.feelsLikeCelsius != null) "${weatherData.feelsLikeCelsius}°C" else "N/A",
                    iconColor = HeatOrange,
                    modifier = Modifier.weight(1f)
                )

                WeatherMetricItem(
                    icon = Icons.Default.WaterDrop,
                    label = "Humidity",
                    value = if (weatherData.humidityPercent != null) "${weatherData.humidityPercent}%" else "N/A",
                    iconColor = PrimaryTeal,
                    modifier = Modifier.weight(1f)
                )

                WeatherMetricItem(
                    icon = Icons.Default.Air,
                    label = "Wind",
                    value = if (weatherData.windSpeedKmh != null) "${weatherData.windSpeedKmh} km/h" else "N/A",
                    iconColor = PrimaryTeal,
                    modifier = Modifier.weight(1f)
                )
            }

            if (weatherData.source.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Source: ${weatherData.source}${if (weatherData.timestamp.isNotEmpty()) " • " + weatherData.timestamp else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun WeatherMetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
