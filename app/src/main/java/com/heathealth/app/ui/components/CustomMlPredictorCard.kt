package com.heathealth.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.heathealth.app.R
import com.heathealth.app.data.model.CustomPredictionState
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.HeatAmber
import com.heathealth.app.ui.theme.HeatOrange
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun CustomMlPredictorCard(
    customState: CustomPredictionState,
    onInputsChanged: (Double, Double, Double) -> Unit,
    onPredictClicked: () -> Unit
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
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
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "ML Predictor",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.custom_heat_risk_simulator),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryTealLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Interactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryTeal
                    )
                }
            }

            Text(
                text = stringResource(R.string.custom_heat_risk_sub),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            // Sliders for Temperature, Humidity, Wind
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Temperature Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.temperature_label), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text(text = "${customState.temperatureC.roundToInt()}°C", style = MaterialTheme.typography.labelLarge, color = HeatOrange)
                }
                Slider(
                    value = customState.temperatureC.toFloat(),
                    onValueChange = { onInputsChanged(it.toDouble(), customState.humidityPercent, customState.windSpeedKmh) },
                    valueRange = 15f..50f,
                    colors = SliderDefaults.colors(thumbColor = HeatOrange, activeTrackColor = HeatOrange)
                )

                // Humidity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.relative_humidity), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text(text = "${customState.humidityPercent.roundToInt()}%", style = MaterialTheme.typography.labelLarge, color = PrimaryTeal)
                }
                Slider(
                    value = customState.humidityPercent.toFloat(),
                    onValueChange = { onInputsChanged(customState.temperatureC, it.toDouble(), customState.windSpeedKmh) },
                    valueRange = 10f..100f,
                    colors = SliderDefaults.colors(thumbColor = PrimaryTeal, activeTrackColor = PrimaryTeal)
                )

                // Wind Speed Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.wind_speed), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text(text = "${customState.windSpeedKmh.roundToInt()} km/h", style = MaterialTheme.typography.labelLarge, color = PrimaryTeal)
                }
                Slider(
                    value = customState.windSpeedKmh.toFloat(),
                    onValueChange = { onInputsChanged(customState.temperatureC, customState.humidityPercent, it.toDouble()) },
                    valueRange = 0f..50f,
                    colors = SliderDefaults.colors(thumbColor = PrimaryTeal, activeTrackColor = PrimaryTeal)
                )
            }

            // Predict Button
            Button(
                onClick = onPredictClicked,
                enabled = !customState.isPredicting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (customState.isPredicting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = SurfaceWhite,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.running_ml_inference))
                } else {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.btn_predict_custom_heat_risk))
                }
            }

            // Error Display
            customState.errorMessage?.let { err ->
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // ML Prediction Result View
            customState.predictionResult?.let { result ->
                val riskEnum = RiskLevel.fromDisplayName(result.riskLevel)
                val levelColor = when (riskEnum) {
                    RiskLevel.LOW -> Color(0xFF2E7D32)
                    RiskLevel.MODERATE -> HeatAmber
                    RiskLevel.HIGH -> HeatOrange
                    RiskLevel.VERY_HIGH, RiskLevel.EXTREME -> Color(0xFFC62828)
                    else -> PrimaryTeal
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, levelColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = levelColor.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(levelColor)
                            )
                            Text(
                                text = "Assessment: ${riskEnum.displayName}",
                                style = MaterialTheme.typography.titleMedium,
                                color = levelColor
                            )
                        }

                        result.riskScore?.let { score ->
                            Text(
                                text = "Heat Index Score: ${score}°C",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = result.predictionMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
