package com.heathealth.app.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.heathealth.app.ui.components.DisclaimerCard
import com.heathealth.app.ui.components.HeaderComponent
import com.heathealth.app.ui.components.HourlyForecastCard
import com.heathealth.app.ui.components.RiskIndicatorCard
import com.heathealth.app.ui.components.SafetyRecommendationsCard
import com.heathealth.app.ui.components.WeatherSummaryCard
import com.heathealth.app.ui.theme.HeatHealthTheme
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary
import com.heathealth.app.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = DashboardViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderComponent(
                    appName = "HeatHealth",
                    onNotificationClick = { viewModel.refreshData() }
                )

                if (uiState.isRefreshing) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryTeal
                    )
                }

                if (uiState.isLoading && !uiState.isRefreshing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = PrimaryTeal)
                            Text(
                                text = "Fetching live weather & evaluating heat risk...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                } else if (uiState.errorMessage != null && !uiState.isRefreshing && uiState.weatherData.isDemoData) {
                    ErrorCardView(
                        errorMessage = uiState.errorMessage ?: "Unable to connect to backend server.",
                        onRetry = { viewModel.refreshData() },
                        onLoadSample = { viewModel.loadSampleData() }
                    )
                } else {
                    WeatherSummaryCard(
                        weatherData = uiState.weatherData,
                        locationLabel = uiState.locationLabel
                    )

                    HourlyForecastCard(
                        hourlyForecast = uiState.hourlyForecast
                    )

                    RiskIndicatorCard(
                        riskPrediction = uiState.riskPrediction,
                        factors = uiState.factors
                    )

                    SafetyRecommendationsCard(
                        recommendations = uiState.recommendations
                    )

                    DisclaimerCard(
                        disclaimerText = uiState.disclaimer
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ErrorCardView(
    errorMessage: String,
    onRetry: () -> Unit,
    onLoadSample: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = "Connection Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = "Backend Connection Issue",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Retry")
            }

            OutlinedButton(onClick = onLoadSample) {
                Text("View Offline Demo")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    HeatHealthTheme {
        DashboardScreen()
    }
}
