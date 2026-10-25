package com.heathealth.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heathealth.app.R
import com.heathealth.app.data.model.DataSource
import com.heathealth.app.ui.components.CampusCoolMapShortcutCard
import com.heathealth.app.ui.components.CustomMlPredictorCard
import com.heathealth.app.ui.components.DisclaimerCard
import com.heathealth.app.ui.components.HeatHealthLogo
import com.heathealth.app.ui.components.HourlyForecastCard
import com.heathealth.app.ui.components.OutdoorActivityPlannerCard
import com.heathealth.app.ui.components.PersonalSafetyDashboardCard
import com.heathealth.app.ui.components.RiskIndicatorCard
import com.heathealth.app.ui.components.SafetyRecommendationsCard
import com.heathealth.app.ui.components.WeatherSummaryCard
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary
import com.heathealth.app.util.LocationHelper
import com.heathealth.app.viewmodel.DashboardViewModel
import java.time.LocalTime

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToCoolMap: () -> Unit = {},
    onNavigateToRiskHistory: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val studentName by viewModel.studentNickname.collectAsState()
    val recentRiskLogs by viewModel.recentRiskLogs.collectAsState()
    val recentAlertLogs by viewModel.recentAlertLogs.collectAsState()
    val todayWaterIntakeMl by viewModel.todayWaterIntakeMl.collectAsState()
    val weeklyWaterTotalMl by viewModel.weeklyWaterTotalMl.collectAsState()

    val context = LocalContext.current

    fun fetchAndApplyDeviceLocation() {
        LocationHelper.fetchDeviceLocation(
            context = context,
            onLocationRetrieved = { result ->
                viewModel.updateCoordinates(result.latitude, result.longitude, result.locationLabel)
            }
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            fetchAndApplyDeviceLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            fetchAndApplyDeviceLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time-aware Student Greeting Header
                DashboardGreetingHeader(
                    studentName = studentName,
                    onNavigateToAlerts = onNavigateToAlerts
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
                            .height(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = PrimaryTeal)
                            Text(
                                text = stringResource(R.string.fetching_weather_evaluating_risk),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                } else if (uiState.dataSource == DataSource.UNAVAILABLE || (uiState.errorMessage != null && !uiState.isRefreshing && uiState.dataSource == DataSource.DEMO && uiState.weatherData.isDemoData)) {
                    ErrorCardView(
                        errorMessage = "Unable to load current weather observations.",
                        onRetry = { viewModel.refreshData() },
                        onLoadSample = { viewModel.loadSampleData() }
                    )
                } else {
                    // Weather Summary Card
                    WeatherSummaryCard(
                        weatherData = uiState.weatherData,
                        locationLabel = uiState.locationLabel,
                        dataSource = uiState.dataSource,
                        lastUpdated = uiState.lastUpdated,
                        onMyLocationClick = {
                            if (LocationHelper.hasLocationPermission(context)) {
                                fetchAndApplyDeviceLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )

                    // Live Heat Risk Indicator Card
                    RiskIndicatorCard(
                        riskPrediction = uiState.riskPrediction,
                        factors = uiState.factors
                    )

                    // Outdoor Activity Planner Card
                    OutdoorActivityPlannerCard(
                        hourlyForecast = uiState.hourlyForecast
                    )

                    // Hourly Forecast Card
                    HourlyForecastCard(
                        hourlyForecast = uiState.hourlyForecast
                    )

                    // Campus Cool Map Shortcut Card
                    CampusCoolMapShortcutCard(
                        onClick = onNavigateToCoolMap
                    )

                    // Personal Safety Dashboard Card
                    PersonalSafetyDashboardCard(
                        latestRisk = uiState.riskPrediction,
                        recentRiskLogs = recentRiskLogs,
                        recentAlertLogs = recentAlertLogs,
                        todayWaterIntakeMl = todayWaterIntakeMl,
                        targetWaterIntakeMl = 2500,
                        weeklyWaterTotalMl = weeklyWaterTotalMl,
                        weeklyWaterDaysCount = 7
                    )

                    // Risk History & Trends Shortcut Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                            .clickable { onNavigateToRiskHistory() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = "Risk Trends",
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = stringResource(R.string.heat_risk_history_trends),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = stringResource(R.string.view_recorded_prediction_trends),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = onNavigateToRiskHistory,
                                modifier = Modifier.height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_view),
                                    color = com.heathealth.app.ui.theme.SurfaceWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Custom ML Risk Simulator
                    CustomMlPredictorCard(
                        customState = uiState.customPrediction,
                        onInputsChanged = { temp, hum, wind ->
                            viewModel.updateCustomInputs(temp, hum, wind)
                        },
                        onPredictClicked = {
                            viewModel.runCustomMlPrediction()
                        }
                    )

                    // Safety Recommendations
                    SafetyRecommendationsCard(
                        recommendations = uiState.recommendations
                    )

                    // Public Health Disclaimer
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
private fun DashboardGreetingHeader(
    studentName: String = "Student",
    onNavigateToAlerts: () -> Unit = {}
) {
    val currentHour = try { LocalTime.now().hour } catch (e: Exception) { 12 }
    val greetingText = when (currentHour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Hello"
    }

    val displayName = if (studentName.isNotBlank()) studentName else "Student"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            HeatHealthLogo(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Column {
                Text(
                    text = "$greetingText, $displayName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        IconButton(
            onClick = onNavigateToAlerts,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryTealLight)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = stringResource(R.string.smart_heat_alerts),
                tint = PrimaryTeal,
                modifier = Modifier.size(20.dp)
            )
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
            text = stringResource(R.string.backend_connection_issue),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
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
                modifier = Modifier.height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(stringResource(R.string.btn_retry), fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onLoadSample,
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.btn_view_offline_demo), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
