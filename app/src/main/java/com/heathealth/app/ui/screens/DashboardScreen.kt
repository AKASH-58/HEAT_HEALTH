package com.heathealth.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.heathealth.app.ui.components.DisclaimerCard
import com.heathealth.app.ui.components.HeaderComponent
import com.heathealth.app.ui.components.RiskIndicatorCard
import com.heathealth.app.ui.components.SafetyRecommendationsCard
import com.heathealth.app.ui.components.WeatherSummaryCard
import com.heathealth.app.ui.theme.HeatHealthTheme
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
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "An unexpected error occurred.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeaderComponent(
                        appName = "HeatHealth",
                        onNotificationClick = { /* Visual element only for Phase 1 */ }
                    )

                    WeatherSummaryCard(
                        weatherData = uiState.weatherData
                    )

                    RiskIndicatorCard(
                        riskPrediction = uiState.riskPrediction
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

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    HeatHealthTheme {
        DashboardScreen()
    }
}
