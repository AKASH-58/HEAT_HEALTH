package com.heathealth.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.HeatAmber
import com.heathealth.app.ui.theme.HeatOrange
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary
import com.heathealth.app.viewmodel.DashboardViewModel

data class QuickLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val estimatedTemp: String,
    val riskLevel: RiskLevel
)

@Composable
fun RiskMapScreen(
    viewModel: DashboardViewModel
) {
    var searchQuery by remember { mutableStateOf("") }

    val presetLocations = listOf(
        QuickLocation("New Delhi", 28.6139, 77.2090, "35°C", RiskLevel.HIGH),
        QuickLocation("Bengaluru", 12.9716, 77.5946, "29°C", RiskLevel.MODERATE),
        QuickLocation("Mumbai", 19.0760, 72.8777, "33°C", RiskLevel.HIGH),
        QuickLocation("Chennai", 13.0827, 80.2707, "37°C", RiskLevel.VERY_HIGH),
        QuickLocation("Kolkata", 22.5726, 88.3639, "36°C", RiskLevel.HIGH)
    )

    val filteredLocations = if (searchQuery.isBlank()) {
        presetLocations
    } else {
        presetLocations.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

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
                imageVector = Icons.Default.Map,
                contentDescription = "Risk Map",
                tint = PrimaryTeal,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PrimaryTealLight)
                    .padding(8.dp)
            )

            Column {
                Text(
                    text = "Regional Heat Risk Map",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Text(
                    text = "Select a region to evaluate local heat stress",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Location Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search location e.g. New Delhi, Mumbai...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = TextSecondary
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Visual Heat Zone Map Placeholder Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryTealLight.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Map Zone",
                    tint = PrimaryTeal,
                    modifier = Modifier.size(36.dp)
                )

                Text(
                    text = "Interactive Spatial Map",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Text(
                    text = "Tap a region below to fetch live Open-Meteo weather and evaluate heat risk for that area.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Text(
            text = "Select Regional Monitoring Zone",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        // List of Regional Locations
        filteredLocations.forEach { loc ->
            LocationCardItem(
                location = loc,
                onSelect = {
                    viewModel.updateCoordinates(loc.latitude, loc.longitude, loc.name)
                }
            )
        }
    }
}

@Composable
private fun LocationCardItem(
    location: QuickLocation,
    onSelect: () -> Unit
) {
    val levelColor = when (location.riskLevel) {
        RiskLevel.LOW -> Color(0xFF2E7D32)
        RiskLevel.MODERATE -> HeatAmber
        RiskLevel.HIGH -> HeatOrange
        RiskLevel.VERY_HIGH, RiskLevel.EXTREME -> Color(0xFFC62828)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = levelColor,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${location.latitude}, ${location.longitude}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = location.estimatedTemp,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(levelColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = location.riskLevel.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = levelColor
                        )
                    }
                }

                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
