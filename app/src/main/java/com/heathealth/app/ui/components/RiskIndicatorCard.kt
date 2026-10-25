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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heathealth.app.R
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.model.RiskPrediction
import com.heathealth.app.ui.theme.HeatAmber
import com.heathealth.app.ui.theme.HeatOrange
import com.heathealth.app.ui.theme.HeatRiskBg
import com.heathealth.app.ui.theme.HeatRiskBorder
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary

@Composable
fun RiskIndicatorCard(
    riskPrediction: RiskPrediction,
    factors: List<String> = emptyList(),
) {
    val levelColor = when (riskPrediction.level) {
        RiskLevel.LOW -> Color(0xFF2E7D32) // Green
        RiskLevel.MODERATE -> HeatAmber      // Amber
        RiskLevel.HIGH -> HeatOrange        // Orange
        RiskLevel.VERY_HIGH, RiskLevel.EXTREME -> Color(0xFFC62828) // Red
        else -> PrimaryTeal
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HeatRiskBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HeatRiskBg),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Risk Indicator",
                        tint = levelColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.risk_level_title, riskPrediction.level.getLocalizedName()),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                // Method / Subtitle Badge
                Text(
                    text = "Heat Risk Index",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = levelColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceWhite)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Risk Level Category Display with Text + Color Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.category_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(levelColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(levelColor)
                    )
                    Text(
                        text = riskPrediction.level.getLocalizedName(),
                        style = MaterialTheme.typography.titleLarge,
                        color = levelColor
                    )
                }

                riskPrediction.score?.let { scoreVal ->
                    Text(
                        text = stringResource(R.string.score_label, scoreVal.toString()),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = riskPrediction.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            // Main Contributing Factors Section
            if (factors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Factors",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.contributing_factors),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                factors.forEach { factor ->
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryTeal
                        )
                        Text(
                            text = factor,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
