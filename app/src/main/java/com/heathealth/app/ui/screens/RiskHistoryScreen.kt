package com.heathealth.app.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heathealth.app.R
import com.heathealth.app.data.local.entity.HeatRiskLogEntity
import com.heathealth.app.data.model.RiskLevel
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
import com.heathealth.app.viewmodel.RiskHistoryViewModel
import com.heathealth.app.viewmodel.TimePeriod
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.material.icons.filled.NotificationsActive

@Composable
fun RiskHistoryScreen(
    viewModel: RiskHistoryViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {}
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val historyLogs by viewModel.historyLogs.collectAsState()
    val summary by viewModel.trendsSummary.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }
    var deleteCandidateId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate Back", tint = PrimaryTeal)
                }

                Column {
                    Text(
                        text = stringResource(R.string.historical_analytics_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.historical_analytics_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onNavigateToAlerts) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = stringResource(R.string.smart_heat_alerts),
                        tint = PrimaryTeal
                    )
                }

                if (historyLogs.isNotEmpty()) {
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear History", tint = TextSecondary)
                    }
                }
            }
        }

        HorizontalDivider(color = BorderLight)

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimePeriod.entries.forEach { period ->
                    val isSelected = period == selectedPeriod
                    val periodText = when (period) {
                        TimePeriod.LAST_7_DAYS -> stringResource(R.string.time_period_week)
                        TimePeriod.LAST_30_DAYS -> stringResource(R.string.time_period_month)
                        TimePeriod.ALL_TIME -> stringResource(R.string.time_period_all)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PrimaryTeal else PrimaryTealLight)
                            .clickable { viewModel.selectPeriod(period) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = periodText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SurfaceWhite else PrimaryTeal
                        )
                    }
                }
            }

            // Trends Summary Dashboard Card
            TrendsDashboardCard(summary = summary, periodName = selectedPeriod.displayName, historyLogs = historyLogs)

            // Insights Card
            StudentInsightsCard(insights = summary.insights)

            // History Records Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historical Prediction Records",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${historyLogs.size} records",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // History Records List
            if (historyLogs.isEmpty()) {
                EmptyHistoryCard(periodName = selectedPeriod.displayName)
            } else {
                historyLogs.forEach { log ->
                    HistoryRecordCard(
                        log = log,
                        onDelete = { deleteCandidateId = log.id }
                    )
                }
            }
        }
    }

    // Clear History Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = HeatRed) },
            title = { Text(text = "Clear Risk History?", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Are you sure you want to delete all recorded heat-risk prediction history? This action cannot be undone.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        viewModel.clearAllLogs()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HeatRed)
                ) {
                    Text(text = "Clear All", color = SurfaceWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Delete Single Log Dialog
    if (deleteCandidateId != null) {
        val candidateId = deleteCandidateId!!
        AlertDialog(
            onDismissRequest = { deleteCandidateId = null },
            title = { Text(text = "Delete Record?", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Remove this prediction record from history?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        deleteCandidateId = null
                        viewModel.deleteLog(candidateId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HeatRed)
                ) {
                    Text(text = "Delete", color = SurfaceWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidateId = null }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun TrendsDashboardCard(
    summary: com.heathealth.app.viewmodel.RiskTrendsSummary,
    periodName: String,
    historyLogs: List<HeatRiskLogEntity>
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(22.dp))
                Text(
                    text = "Risk Trends ($periodName)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Key Metrics Row
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
                    Text(text = "${summary.totalPredictions}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryTeal)
                    Text(text = "Predictions", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val highest = summary.highestRiskCategory
                    val riskColor = if (highest != null) getRiskColor(highest) else TextSecondary
                    Text(text = highest?.displayName ?: "--", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = riskColor)
                    Text(text = "Peak Risk", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val avgFeels = summary.averageFeelsLike
                    val formatted = if (avgFeels != null) String.format(Locale.US, "%.1f°C", avgFeels) else "--"
                    Text(text = formatted, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Avg Feels-Like", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            // Risk Distribution Bars
            if (summary.totalPredictions > 0) {
                Text(text = "Risk Category Distribution:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

                listOf(RiskLevel.LOW, RiskLevel.MODERATE, RiskLevel.HIGH, RiskLevel.EXTREME).forEach { level ->
                    val count = summary.riskDistribution[level] ?: 0
                    val fraction = if (summary.totalPredictions > 0) count.toFloat() / summary.totalPredictions else 0f
                    val color = getRiskColor(level)

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${level.displayName} Risk", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            Text(text = "$count (${(fraction * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
                        }

                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = BorderLight
                        )
                    }
                }

                // Time-Series Trend Line Chart
                if (historyLogs.size >= 2) {
                    HorizontalDivider(color = BorderLight)
                    Text(text = "Timeline Risk Trend:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    TimeSeriesTrendChart(logs = historyLogs)
                }
            }
        }
    }
}

@Composable
private fun TimeSeriesTrendChart(logs: List<HeatRiskLogEntity>) {
    val sorted = remember(logs) { logs.sortedBy { it.timestamp } }
    val primaryColor = PrimaryTeal

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryTealLight.copy(alpha = 0.4f))
                .padding(12.dp)
        ) {
            val width = size.width
            val height = size.height

            if (sorted.size >= 2) {
                val stepX = width / (sorted.size - 1)
                val points = sorted.mapIndexed { index, log ->
                    val riskLevel = RiskLevel.fromDisplayName(log.riskLevel)
                    val value = when (riskLevel) {
                        RiskLevel.LOW -> 1f
                        RiskLevel.MODERATE -> 2f
                        RiskLevel.HIGH -> 3f
                        RiskLevel.VERY_HIGH, RiskLevel.EXTREME -> 4f
                        else -> 1f
                    }
                    val x = index * stepX
                    val y = height - ((value - 1f) / 3f * height)
                    Offset(x, y)
                }

                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                drawPath(path = path, color = primaryColor, style = Stroke(width = 4.0f))

                points.forEach { pt ->
                    drawCircle(color = primaryColor, radius = 5.0f, center = pt)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Oldest", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(text = "Timeline Trend", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(text = "Latest", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun StudentInsightsCard(insights: List<String>) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = HeatAmber, modifier = Modifier.size(22.dp))
                Text(
                    text = "Student-Friendly Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            insights.forEach { insight ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                    Text(text = insight, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun HistoryRecordCard(
    log: HeatRiskLogEntity,
    onDelete: () -> Unit
) {
    val riskLevel = RiskLevel.fromDisplayName(log.riskLevel)
    val riskColor = getRiskColor(riskLevel)

    val formattedDate = remember(log.timestamp) {
        try {
            val instant = Instant.ofEpochMilli(log.timestamp)
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a", Locale.US)
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (e: Exception) {
            log.isoTimestamp.take(16).replace("T", " ")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(riskColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "${riskLevel.displayName} Risk", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SurfaceWhite)
                    }

                    if (log.riskScore != null) {
                        Text(text = "Score: ${log.riskScore}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete record", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            // Location & Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                    Text(text = log.locationLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Text(text = formattedDate, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            // Weather Observations Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryTealLight.copy(alpha = 0.5f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(14.dp))
                    Text(text = "Temp: ${log.temperatureCelsius}°C", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(14.dp))
                    Text(text = "Humidity: ${log.humidityPercent}%", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                }

                Text(text = "Feels Like: ${log.feelsLikeCelsius}°C", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PrimaryTeal)
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard(periodName: String) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
            Text(
                text = "No Predictions Recorded for $periodName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Fetch live risk predictions on the Home dashboard to begin building your personal heat-health prediction history.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
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
