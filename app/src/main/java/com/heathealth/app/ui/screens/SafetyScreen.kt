package com.heathealth.app.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.HeatAmber
import com.heathealth.app.ui.theme.HeatOrange
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary

@Composable
fun SafetyScreen() {
    val profiles = listOf("Outdoor Worker", "Student", "Senior Citizen", "Athlete")
    var selectedProfile by remember { mutableStateOf("Outdoor Worker") }

    val hydrationGoal = when (selectedProfile) {
        "Outdoor Worker" -> "3.5 - 4.0 Liters / Day (250ml every 20 mins in heat)"
        "Athlete" -> "3.0 - 4.0 Liters / Day (Pre-hydration + electrolytes)"
        "Senior Citizen" -> "2.0 - 2.5 Liters / Day (Regular small sips)"
        else -> "2.5 - 3.0 Liters / Day (Keep water bottle accessible)"
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
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = "Safety Guide",
                tint = PrimaryTeal,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PrimaryTealLight)
                    .padding(8.dp)
            )

            Column {
                Text(
                    text = "Heat Safety & First Aid",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Text(
                    text = "Personalized protection protocols and emergency response",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Text(
            text = "Select Vulnerability Profile",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        // Profile Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profiles.forEach { prof ->
                val isSelected = prof == selectedProfile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) PrimaryTeal else PrimaryTealLight)
                        .clickable { selectedProfile = prof }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = prof,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) SurfaceWhite else PrimaryTeal
                    )
                }
            }
        }

        // Personalized Hydration Protocol Card
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
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$selectedProfile Safety Plan",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Hydration Intake Target:",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )

                Text(
                    text = hydrationGoal,
                    style = MaterialTheme.typography.titleSmall,
                    color = PrimaryTeal
                )

                Spacer(modifier = Modifier.height(4.dp))

                SafetyBulletItem("Take mandatory 10-15 min rest breaks in shaded, cool areas every hour.")
                SafetyBulletItem("Avoid heavy direct sun exposure between 11:00 AM and 4:00 PM.")
                SafetyBulletItem("Wear lightweight, light-colored, and breathable cotton clothing.")
            }
        }

        Text(
            text = "Heat Illness Emergency Signs",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        // Symptoms & First Aid Cards
        SymptomCard(
            title = "Heat Exhaustion (Early Stage)",
            symptoms = "Dizziness, heavy sweating, headache, nausea, rapid pulse, weakness.",
            action = "Move to cool shade, sip water slowly, loosen clothing, apply wet cloths.",
            color = HeatAmber
        )

        SymptomCard(
            title = "Heat Stroke (Medical Emergency)",
            symptoms = "High body temperature (>40°C), confusion, fainting, hot dry skin, vomiting.",
            action = "Call Emergency Services (108/112) immediately. Cool body with cold water/ice.",
            color = Color(0xFFC62828)
        )

        // Emergency Helplines Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryTealLight)
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Helpline",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Emergency Health Helpline",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Toll-Free Public Medical Assistance",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = "108 / 112",
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryTeal
                )
            }
        }
    }
}

@Composable
private fun SafetyBulletItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = PrimaryTeal,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary
        )
    }
}

@Composable
private fun SymptomCard(
    title: String,
    symptoms: String,
    action: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            Text(
                text = "Symptoms: $symptoms",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )

            Text(
                text = "Action: $action",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}
