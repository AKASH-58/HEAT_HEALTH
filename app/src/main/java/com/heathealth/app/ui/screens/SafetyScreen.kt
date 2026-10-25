package com.heathealth.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heathealth.app.R
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.ui.components.getLocalizedName
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
import com.heathealth.app.viewmodel.SafetyCenterViewModel

@Composable
fun SafetyScreen(
    viewModel: SafetyCenterViewModel = viewModel(),
    onNavigateToAlerts: () -> Unit = {}
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val checklistItems by viewModel.checklistItems.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_safety_guidance),
        stringResource(R.string.tab_daily_checklist),
        stringResource(R.string.tab_first_aid),
        stringResource(R.string.tab_emergency_contacts)
    )

    // Dialer Confirm Dialog state
    var callConfirmNumber by remember { mutableStateOf<String?>(null) }
    var callConfirmName by remember { mutableStateOf<String?>(null) }

    // Edit Contacts state
    var isEditingContacts by remember { mutableStateOf(false) }
    var editCampusName by remember { mutableStateOf(emergencyContacts.campusHealthName) }
    var editCampusPhone by remember { mutableStateOf(emergencyContacts.campusHealthPhone) }
    var editPersonalName by remember { mutableStateOf(emergencyContacts.personalContactName) }
    var editPersonalPhone by remember { mutableStateOf(emergencyContacts.personalContactPhone) }
    var editError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Safety Center",
                        tint = PrimaryTeal,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryTealLight)
                            .padding(10.dp)
                    )

                    Column {
                        Text(
                            text = stringResource(R.string.student_heat_safety_center),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.student_heat_safety_center_sub),
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

            // Scrollable Tab Row with 16dp edge padding
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SurfaceWhite,
                contentColor = PrimaryTeal,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = PrimaryTeal
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) PrimaryTeal else TextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = BorderLight)

        // Main Tab Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> SafetyGuidanceTabContent(
                    uiState = uiState,
                    onSelectProfile = { viewModel.selectProfile(it) }
                )
                1 -> DailyChecklistTabContent(
                    items = checklistItems,
                    onToggleItem = { id, done -> viewModel.toggleChecklistItem(id, done) }
                )
                2 -> FirstAidTabContent(
                    onTriggerEmergencyCall = { name, phone ->
                        callConfirmName = name
                        callConfirmNumber = phone
                    }
                )
                3 -> EmergencyContactsTabContent(
                    contacts = emergencyContacts,
                    isEditing = isEditingContacts,
                    editCampusName = editCampusName,
                    editCampusPhone = editCampusPhone,
                    editPersonalName = editPersonalName,
                    editPersonalPhone = editPersonalPhone,
                    editError = editError,
                    onStartEdit = {
                        editCampusName = emergencyContacts.campusHealthName
                        editCampusPhone = emergencyContacts.campusHealthPhone
                        editPersonalName = emergencyContacts.personalContactName
                        editPersonalPhone = emergencyContacts.personalContactPhone
                        editError = null
                        isEditingContacts = true
                    },
                    onCampusNameChange = { editCampusName = it },
                    onCampusPhoneChange = { editCampusPhone = it },
                    onPersonalNameChange = { editPersonalName = it },
                    onPersonalPhoneChange = { editPersonalPhone = it },
                    onSave = {
                        val result = viewModel.saveEmergencyContacts(
                            editCampusName, editCampusPhone, editPersonalName, editPersonalPhone
                        )
                        result.fold(
                            onSuccess = {
                                editError = null
                                isEditingContacts = false
                            },
                            onFailure = { err ->
                                editError = err.localizedMessage ?: "Invalid phone number format."
                            }
                        )
                    },
                    onCancelEdit = { isEditingContacts = false },
                    onCallRequested = { name, phone ->
                        callConfirmName = name
                        callConfirmNumber = phone
                    }
                )
            }
        }
    }

    // Call Confirmation Dialog
    if (callConfirmNumber != null) {
        AlertDialog(
            onDismissRequest = { callConfirmNumber = null },
            icon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = PrimaryTeal) },
            title = { Text(text = "Confirm Phone Call", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Do you want to launch dialer to call ${callConfirmName ?: "Contact"} at $callConfirmNumber?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = callConfirmNumber ?: ""
                        callConfirmNumber = null
                        val sanitized = num.replace("[^0-9+]".toRegex(), "")
                        if (sanitized.isNotEmpty()) {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized"))
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "No phone dialer app found on this device.", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Unable to initiate call: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text(text = "Call Now", color = SurfaceWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { callConfirmNumber = null }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SafetyGuidanceTabContent(
    uiState: com.heathealth.app.viewmodel.SafetyCenterUiState,
    onSelectProfile: (String) -> Unit
) {
    val profiles = listOf("Campus Commuter", "Student Athlete", "Outdoor Class", "Regular Student")

    // Live / Cached Heat Risk Banner
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (uiState.isLiveRiskAvailable) {
                getRiskColor(uiState.guidance.riskLevel).copy(alpha = 0.10f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        imageVector = if (uiState.isLiveRiskAvailable) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (uiState.isLiveRiskAvailable) getRiskColor(uiState.guidance.riskLevel) else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (uiState.isLiveRiskAvailable) {
                            "Risk Level: ${uiState.guidance.riskLevel.getLocalizedName()}"
                        } else {
                            "Heat-Safety Guidance"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (uiState.isLiveRiskAvailable) getRiskColor(uiState.guidance.riskLevel) else PrimaryTeal)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (uiState.isLiveRiskAvailable) "Active Guidance" else "Safety Protocols",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceWhite
                    )
                }
            }

            Text(
                text = uiState.guidance.overviewMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
    }

    // Profile Context Selection Header & FlowRow Chips
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Select Student Activity Context:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            profiles.forEach { prof ->
                val isSelected = prof == uiState.selectedProfile
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            if (isSelected) PrimaryTeal else BorderLight,
                            RoundedCornerShape(10.dp)
                        )
                        .background(if (isSelected) PrimaryTeal else PrimaryTealLight)
                        .clickable { onSelectProfile(prof) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = prof,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) SurfaceWhite else PrimaryTeal
                    )
                }
            }
        }
    }

    Text(
        text = "Tailored Activity Protocols",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )

    uiState.guidance.activityGuidances.forEach { act ->
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = act.activityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                SafetyBulletItem("Recommended Action", act.recommendedAction)
                SafetyBulletItem("Hydration Tip", act.hydrationTip)
            }
        }
    }
}

@Composable
private fun DailyChecklistTabContent(
    items: List<com.heathealth.app.viewmodel.SafetyChecklistItem>,
    onToggleItem: (String, Boolean) -> Unit
) {
    val completedCount = items.count { it.isCompleted }
    val totalCount = items.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryTealLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Icon(imageVector = Icons.Default.ListAlt, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(22.dp))
                    Text(text = "Today's Safety Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Text(
                    text = "$completedCount of $totalCount completed",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal
                )
            }

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryTeal,
                trackColor = SurfaceWhite
            )

            Text(
                text = "Progress is saved automatically on your device and resets each local calendar day at midnight.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }

    Text(
        text = "Practical Safety Checklist:",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )

    items.forEach { item ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (item.isCompleted) PrimaryTealLight.copy(alpha = 0.4f) else SurfaceWhite
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { isChecked -> onToggleItem(item.id, isChecked) },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryTeal)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun FirstAidTabContent(
    onTriggerEmergencyCall: (String, String) -> Unit
) {
    // Critical Medical Warning Box
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, HeatRed, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HeatRed.copy(alpha = 0.10f))
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
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = HeatRed, modifier = Modifier.size(24.dp))
                Text(
                    text = "CRITICAL MEDICAL EMERGENCY NOTICE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HeatRed
                )
            }

            Text(
                text = "Suspected heatstroke is a life-threatening medical emergency! Seek immediate emergency medical assistance. Do NOT wait for symptoms to improve or rely on this app for treatment.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Button(
                onClick = { onTriggerEmergencyCall("National Emergency Helpline", "112") },
                colors = ButtonDefaults.buttonColors(containerColor = HeatRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Call Emergency Helpline (112 / 108)", color = SurfaceWhite, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Heat Exhaustion Protocol
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HeatAmber.copy(alpha = 0.08f))
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
                Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = HeatAmber, modifier = Modifier.size(22.dp))
                Text(text = "Heat Exhaustion (Early Warning Stage)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Text(
                text = "Warning Signs: Heavy sweating, dizziness, headache, nausea, rapid pulse, muscle cramps, weakness, or feeling faint.",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )

            Text(
                text = "Immediate First-Aid Actions:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = HeatAmber
            )
            SafetyBulletItem("Environment", "Move immediately to a cool, shaded, or air-conditioned space.")
            SafetyBulletItem("Hydration", "Sip cool water or electrolyte drinks slowly.")
            SafetyBulletItem("Cooling", "Loosen tight clothing and apply cool, damp cloths to forehead and neck.")
        }
    }

    // Heatstroke Emergency Protocol
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HeatRed.copy(alpha = 0.08f))
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
                Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = HeatRed, modifier = Modifier.size(22.dp))
                Text(text = "Suspected Heatstroke (Medical Emergency)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Text(
                text = "Warning Signs: High body temperature (>40°C / 104°F), confusion, altered mental state, slurred speech, unconsciousness, hot dry skin or heavy sweating, vomiting, or seizures.",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )

            Text(
                text = "Immediate Cooling Protocols:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = HeatRed
            )
            SafetyBulletItem("Call Help", "Call emergency services (112 / 108) or campus medical triage right away.")
            SafetyBulletItem("Cool Body", "Move person to shade and cool rapidly with cool water, wet towels, ice packs, or aggressive fanning.")
            SafetyBulletItem("SAFETY WARNING", "Do NOT give fluids (water or drinks) to anyone who is confused, unconscious, having seizures, or unable to swallow safely.")
        }
    }
}

@Composable
private fun EmergencyContactsTabContent(
    contacts: com.heathealth.app.data.repository.EmergencyContactsData,
    isEditing: Boolean,
    editCampusName: String,
    editCampusPhone: String,
    editPersonalName: String,
    editPersonalPhone: String,
    editError: String?,
    onStartEdit: () -> Unit,
    onCampusNameChange: (String) -> Unit,
    onCampusPhoneChange: (String) -> Unit,
    onPersonalNameChange: (String) -> Unit,
    onPersonalPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancelEdit: () -> Unit,
    onCallRequested: (String, String) -> Unit
) {
    if (isEditing) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Configure Emergency Contacts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

                if (editError != null) {
                    Text(text = editError, style = MaterialTheme.typography.bodySmall, color = HeatRed, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = editCampusName,
                    onValueChange = onCampusNameChange,
                    label = { Text("Campus Health Center Name") },
                    placeholder = { Text("e.g. Campus Health Center") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = editCampusPhone,
                    onValueChange = onCampusPhoneChange,
                    label = { Text("Campus Health Phone") },
                    placeholder = { Text("e.g. +91 80 1234 5678") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider(color = BorderLight)

                OutlinedTextField(
                    value = editPersonalName,
                    onValueChange = onPersonalNameChange,
                    label = { Text("Trusted Personal Contact Name") },
                    placeholder = { Text("e.g. Warden / Parent / Friend") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = editPersonalPhone,
                    onValueChange = onPersonalPhoneChange,
                    label = { Text("Trusted Personal Phone") },
                    placeholder = { Text("e.g. +91 98765 43210") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCancelEdit) {
                        Text(text = "Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save Contacts", color = SurfaceWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Emergency Support Numbers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            OutlinedButton(
                onClick = onStartEdit,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Edit", color = PrimaryTeal, fontWeight = FontWeight.Bold)
            }
        }

        // Campus Health Center Contact Card
        ContactCardItem(
            title = if (contacts.campusHealthName.isNotEmpty()) contacts.campusHealthName else "Campus Health Center",
            phoneNumber = if (contacts.campusHealthPhone.isNotEmpty()) contacts.campusHealthPhone else "Not Configured (Tap Edit to set)",
            category = "Campus Medical Facility",
            color = PrimaryTeal,
            isConfigured = contacts.campusHealthPhone.isNotEmpty(),
            onCall = { onCallRequested(contacts.campusHealthName, contacts.campusHealthPhone) }
        )

        // Trusted Personal Contact Card
        ContactCardItem(
            title = if (contacts.personalContactName.isNotEmpty()) contacts.personalContactName else "Trusted Personal Contact",
            phoneNumber = if (contacts.personalContactPhone.isNotEmpty()) contacts.personalContactPhone else "Not Configured (Tap Edit to set)",
            category = "Personal Emergency Contact",
            color = HeatOrange,
            isConfigured = contacts.personalContactPhone.isNotEmpty(),
            onCall = { onCallRequested(contacts.personalContactName, contacts.personalContactPhone) }
        )

        // Official Emergency Helpline Card
        ContactCardItem(
            title = contacts.publicHelplineName,
            phoneNumber = contacts.publicHelplinePhone,
            category = "Regional Public Emergency Services",
            color = HeatRed,
            isOfficial = true,
            isConfigured = true,
            onCall = { onCallRequested(contacts.publicHelplineName, contacts.publicHelplinePhone) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContactCardItem(
    title: String,
    phoneNumber: String,
    category: String,
    color: Color,
    isOfficial: Boolean = false,
    isConfigured: Boolean = true,
    onCall: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Badges FlowRow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = color.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (isOfficial) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = color
                    ) {
                        Text(
                            text = "PUBLIC HELPLINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Title, Phone & Call Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isConfigured) TextSecondary else HeatOrange,
                        fontWeight = if (isConfigured) FontWeight.Medium else FontWeight.Bold
                    )
                }

                Button(
                    onClick = onCall,
                    enabled = isConfigured,
                    modifier = Modifier.height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color,
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call $title",
                        tint = SurfaceWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call",
                        color = SurfaceWhite,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun SafetyBulletItem(label: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = PrimaryTeal,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryTeal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
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
