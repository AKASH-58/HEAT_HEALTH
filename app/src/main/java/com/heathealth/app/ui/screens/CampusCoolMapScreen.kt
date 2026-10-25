package com.heathealth.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heathealth.app.R
import com.heathealth.app.data.model.CampusFacility
import com.heathealth.app.data.model.DataSource
import com.heathealth.app.data.model.FacilityCategory
import com.heathealth.app.ui.components.CampusFacilityDetailsCard
import com.heathealth.app.ui.components.InteractiveCampusMapView
import com.heathealth.app.ui.components.getLocalizedName
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.HeatAmber
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary
import androidx.compose.material.icons.filled.NotificationsActive
import com.heathealth.app.viewmodel.CampusCoolMapViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CampusCoolMapScreen(
    viewModel: CampusCoolMapViewModel = viewModel(),
    onFullscreenStateChanged: (Boolean) -> Unit = {},
    onNavigateToAlerts: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
        onFullscreenStateChanged(false)
    }

    val filteredFacilities = uiState.facilities.filter { fac ->
        val matchesCategory = uiState.selectedCategory == null || fac.category == uiState.selectedCategory
        val matchesQuery = uiState.searchQuery.isBlank() ||
                fac.name.contains(uiState.searchQuery, ignoreCase = true) ||
                fac.buildingLocation.contains(uiState.searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    if (isFullscreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            InteractiveCampusMapView(
                facilities = filteredFacilities,
                selectedFacility = uiState.selectedFacility,
                onFacilitySelected = { viewModel.selectFacility(it) },
                selectedCollege = uiState.selectedCollege,
                isFullscreen = true,
                onToggleFullscreen = {
                    isFullscreen = false
                    onFullscreenStateChanged(false)
                },
                onMyLocationClicked = {
                    Toast.makeText(context, "Centered on ${uiState.selectedCollege.name}", Toast.LENGTH_SHORT).show()
                },
                centerLatOverride = uiState.selectedCollege.latitude,
                centerLngOverride = uiState.selectedCollege.longitude
            )

            // Fullscreen Top Overlay Header Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceWhite.copy(alpha = 0.95f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        isFullscreen = false
                        onFullscreenStateChanged(false)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Fullscreen",
                            tint = PrimaryTeal
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.selectedCollege.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Full-Screen Collaborative Campus Map",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    OutlinedButton(onClick = { viewModel.openCollegeSearchDialog() }) {
                        Text("Switch", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Floating Action Button in Fullscreen Mode
            FloatingActionButton(
                onClick = { viewModel.openAddFacilityDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (uiState.selectedFacility != null) 220.dp else 24.dp, end = 16.dp),
                containerColor = PrimaryTeal,
                contentColor = SurfaceWhite
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Facility")
            }

            // Bottom Panel for Selected Facility in Fullscreen Mode
            uiState.selectedFacility?.let { facility ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    CampusFacilityDetailsCard(
                        facility = facility,
                        onDismiss = { viewModel.selectFacility(null) },
                        onReportProblemClicked = { viewModel.openReportStatusDialog() }
                    )
                }
            }
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.openAddFacilityDialog() },
                    containerColor = PrimaryTeal,
                    contentColor = SurfaceWhite
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Add Facility", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header & Real-time Sync Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Cool Map",
                            tint = PrimaryTeal,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PrimaryTealLight)
                                .padding(8.dp)
                        )

                        Column {
                            Text(
                                text = stringResource(R.string.campus_cool_map),
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                                Text(
                                    text = stringResource(R.string.real_time_synced),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onNavigateToAlerts,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PrimaryTealLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = stringResource(R.string.smart_heat_alerts),
                                tint = PrimaryTeal
                            )
                        }

                        // Map vs List View Toggle
                        IconButton(
                            onClick = { viewModel.toggleViewMode() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PrimaryTealLight)
                        ) {
                            Icon(
                                imageVector = if (uiState.isMapView) Icons.Default.FormatListBulleted else Icons.Default.Map,
                                contentDescription = "Toggle View Mode",
                                tint = PrimaryTeal
                            )
                        }
                    }
                }

                // Active College Selection Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryTeal, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryTealLight.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(24.dp))
                            Column {
                                Text(
                                    text = uiState.selectedCollege.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${uiState.selectedCollege.city} • ${uiState.selectedCollege.address}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.openCollegeSearchDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(R.string.switch_college), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (uiState.isRefreshing) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = PrimaryTeal
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_facility_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Filter Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isAllSelected = uiState.selectedCategory == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAllSelected) PrimaryTeal else PrimaryTealLight)
                            .clickable { viewModel.selectCategory(null) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.all_facilities),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAllSelected) SurfaceWhite else PrimaryTeal
                        )
                    }

                    FacilityCategory.entries.forEach { cat ->
                        val isSelected = uiState.selectedCategory == cat
                        val catName = when (cat) {
                            FacilityCategory.DRINKING_WATER -> stringResource(R.string.cat_drinking_water)
                            FacilityCategory.SHADED_AREA -> stringResource(R.string.cat_shaded_area)
                            FacilityCategory.COOL_INDOOR_SPACE -> stringResource(R.string.cat_cool_indoor)
                            FacilityCategory.FIRST_AID -> stringResource(R.string.cat_first_aid)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryTeal else PrimaryTealLight)
                                .clickable { viewModel.selectCategory(cat) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = catName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) SurfaceWhite else PrimaryTeal
                            )
                        }
                    }
                }

                // Stale / Cached Data Banner if offline
                if (uiState.dataSource == DataSource.CACHED && uiState.errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HeatAmber, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HeatAmber.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline",
                                    tint = HeatAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Campus Facilities Directory",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (uiState.lastUpdated.isNotEmpty()) "Updated ${uiState.lastUpdated}" else "Saved Facilities",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { viewModel.refreshFacilities() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Retry")
                            }
                        }
                    }
                }

                if (uiState.isLoading && !uiState.isRefreshing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = PrimaryTeal)
                            Text("Fetching campus facilities...", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                } else if (uiState.dataSource == DataSource.UNAVAILABLE || (uiState.errorMessage != null && uiState.facilities.isEmpty())) {
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(44.dp))
                            Text("Directory Offline", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text("Unable to fetch live facilities. Tap to retry.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { viewModel.refreshFacilities() }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text("Retry")
                                }

                                OutlinedButton(onClick = { viewModel.loadSampleFacilities() }) {
                                    Text("View Saved Directory")
                                }
                            }
                        }
                    }
                } else {
                    // Interactive Geographic OpenStreetMap View OR Directory List View
                    if (uiState.isMapView) {
                        InteractiveCampusMapView(
                            facilities = filteredFacilities,
                            selectedFacility = uiState.selectedFacility,
                            onFacilitySelected = { viewModel.selectFacility(it) },
                            selectedCollege = uiState.selectedCollege,
                            isFullscreen = false,
                            onToggleFullscreen = {
                                isFullscreen = true
                                onFullscreenStateChanged(true)
                            },
                            onMyLocationClicked = {
                                Toast.makeText(context, "Centered on ${uiState.selectedCollege.name}", Toast.LENGTH_SHORT).show()
                            },
                            centerLatOverride = uiState.selectedCollege.latitude,
                            centerLngOverride = uiState.selectedCollege.longitude
                        )
                    }

                    // Selected Facility Details Card (if selected)
                    uiState.selectedFacility?.let { facility ->
                        CampusFacilityDetailsCard(
                            facility = facility,
                            onDismiss = { viewModel.selectFacility(null) },
                            onReportProblemClicked = { viewModel.openReportStatusDialog() }
                        )
                    }

                    Text(
                        text = "Campus Directory (${filteredFacilities.size} Facilities)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    if (filteredFacilities.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                                Text("No Facilities Found for ${uiState.selectedCollege.name}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text("Be the first student to contribute a water point or shaded area!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Button(
                                    onClick = { viewModel.openAddFacilityDialog() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                                ) {
                                    Text("+ Add Facility to Campus")
                                }
                            }
                        }
                    } else {
                        filteredFacilities.forEach { facility ->
                            CampusFacilityListItem(
                                facility = facility,
                                isSelected = facility.id == uiState.selectedFacility?.id,
                                onClick = { viewModel.selectFacility(facility) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog 1: College Search & Switch Dialog
    if (uiState.isCollegeSearchOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeCollegeSearchDialog() },
            title = { Text("Select / Switch College") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.collegeSearchQuery,
                        onValueChange = { viewModel.searchColleges(it) },
                        placeholder = { Text("Search college, city (Bengaluru, New Delhi...)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.collegesList.forEach { college ->
                            val isSelected = college.id == uiState.selectedCollege.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectCollege(college) }
                                    .border(1.dp, if (isSelected) PrimaryTeal else BorderLight, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryTealLight else SurfaceWhite)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(college.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                        if (college.isVerified) {
                                            Text("✓ Verified", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                                        }
                                    }
                                    Text("${college.city} • ${college.address}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { viewModel.closeCollegeSearchDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog 2: Add Facility Dialog
    if (uiState.isAddFacilityDialogOpen) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Drinking Water") }
        var description by remember { mutableStateOf("") }
        var buildingLocation by remember { mutableStateOf("") }
        var openingHours by remember { mutableStateOf("") }
        var accessibilityInfo by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.closeAddFacilityDialog() },
            title = { Text("Add Facility to ${uiState.selectedCollege.name}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Facility Name *") },
                        placeholder = { Text("e.g. RO Chilled Water Station") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Category *", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Drinking Water", "Shaded Area", "Cool Indoor Space", "First Aid").forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (category == cat) PrimaryTeal else PrimaryTealLight)
                                    .clickable { category = cat }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(cat, style = MaterialTheme.typography.labelSmall, color = if (category == cat) SurfaceWhite else PrimaryTeal)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description *") },
                        placeholder = { Text("Details, e.g., Purified water, AC maintained at 23°C") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = buildingLocation,
                        onValueChange = { buildingLocation = it },
                        label = { Text("Building Location / Floor *") },
                        placeholder = { Text("e.g. Central Library Ground Floor Entrance") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = openingHours,
                        onValueChange = { openingHours = it },
                        label = { Text("Opening Hours (Optional)") },
                        placeholder = { Text("e.g. 08:00 AM - 08:00 PM") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accessibilityInfo,
                        onValueChange = { accessibilityInfo = it },
                        label = { Text("Accessibility Info (Optional)") },
                        placeholder = { Text("e.g. Wheelchair ramp, Elevator access") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || description.isBlank() || buildingLocation.isBlank()) {
                            Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.submitNewFacility(
                                name = name,
                                category = category,
                                description = description,
                                latitude = uiState.selectedCollege.latitude,
                                longitude = uiState.selectedCollege.longitude,
                                buildingLocation = buildingLocation,
                                openingHours = openingHours.ifBlank { null },
                                accessibilityInfo = accessibilityInfo.ifBlank { null },
                                contactInfo = null
                            )
                            Toast.makeText(context, "Facility added and synced across clients!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Submit Facility")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.closeAddFacilityDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog 3: Report Status / Problem Dialog
    if (uiState.isReportStatusDialogOpen && uiState.selectedFacility != null) {
        val facility = uiState.selectedFacility!!
        var newStatus by remember { mutableStateOf(facility.status) }
        var statusNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.closeReportStatusDialog() },
            title = { Text("Report Issue / Status: ${facility.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Update Current Status:", style = MaterialTheme.typography.labelMedium)

                    listOf("Operational", "Maintenance / Broken", "Closed").forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (newStatus == status) PrimaryTealLight else SurfaceWhite)
                                .clickable { newStatus = status }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(status, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }

                    OutlinedTextField(
                        value = statusNote,
                        onValueChange = { statusNote = it },
                        label = { Text("Problem Note (Optional)") },
                        placeholder = { Text("e.g. Water dispenser empty, AC under repair") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reportFacilityStatus(facility.id, newStatus, statusNote.ifBlank { null })
                        Toast.makeText(context, "Status update synced across clients!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.closeReportStatusDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CampusFacilityListItem(
    facility: CampusFacility,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = when (facility.category) {
        FacilityCategory.DRINKING_WATER -> PrimaryTeal
        FacilityCategory.SHADED_AREA -> Color(0xFF2E7D32)
        FacilityCategory.COOL_INDOOR_SPACE -> Color(0xFF0288D1)
        FacilityCategory.FIRST_AID -> Color(0xFFD32F2F)
    }

    val statusColor = when (facility.status.lowercase()) {
        "operational" -> Color(0xFF2E7D32)
        "closed" -> Color(0xFFD32F2F)
        else -> HeatAmber
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                1.dp,
                if (isSelected) PrimaryTeal else BorderLight,
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryTealLight.copy(alpha = 0.3f) else SurfaceWhite
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = facility.category.getLocalizedName(),
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = facility.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }

                    if (facility.isVerified) {
                        Text(
                            text = "✓ Verified",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = facility.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )

                Text(
                    text = facility.buildingLocation,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
