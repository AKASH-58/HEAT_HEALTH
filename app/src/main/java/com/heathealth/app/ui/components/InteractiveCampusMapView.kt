package com.heathealth.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.heathealth.app.data.model.CampusFacility
import com.heathealth.app.data.model.College
import com.heathealth.app.data.model.FacilityCategory
import com.heathealth.app.ui.theme.BorderLight
import com.heathealth.app.ui.theme.PrimaryTeal
import com.heathealth.app.ui.theme.PrimaryTealLight
import com.heathealth.app.ui.theme.SurfaceWhite
import com.heathealth.app.ui.theme.TextPrimary
import com.heathealth.app.ui.theme.TextSecondary
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun InteractiveCampusMapView(
    facilities: List<CampusFacility>,
    selectedFacility: CampusFacility?,
    onFacilitySelected: (CampusFacility) -> Unit,
    modifier: Modifier = Modifier,
    selectedCollege: College? = null,
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {},
    onMyLocationClicked: () -> Unit = {},
    centerLatOverride: Double? = null,
    centerLngOverride: Double? = null
) {
    val context = LocalContext.current

    val validFacilities = facilities.filter { it.latitude != null && it.longitude != null }
    val invalidCount = facilities.size - validFacilities.size

    val defaultLat = selectedCollege?.latitude ?: 12.9716
    val defaultLng = selectedCollege?.longitude ?: 77.5946

    val centerLat = centerLatOverride ?: defaultLat
    val centerLng = centerLngOverride ?: defaultLng

    Card(
        modifier = if (isFullscreen) {
            modifier.fillMaxSize()
        } else {
            modifier
                .fillMaxWidth()
                .height(360.dp)
                .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
        },
        shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryTealLight.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val prefs = ctx.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
                    Configuration.getInstance().load(ctx, prefs)
                    Configuration.getInstance().userAgentValue = ctx.packageName

                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(16.0)
                        controller.setCenter(GeoPoint(centerLat, centerLng))
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    if (centerLatOverride != null && centerLngOverride != null) {
                        mapView.controller.animateTo(GeoPoint(centerLatOverride, centerLngOverride))
                    } else if (selectedCollege != null) {
                        mapView.controller.animateTo(GeoPoint(selectedCollege.latitude, selectedCollege.longitude))
                    }

                    // 1. College Main Campus Pin
                    selectedCollege?.let { college ->
                        val collegeMarker = Marker(mapView).apply {
                            position = GeoPoint(college.latitude, college.longitude)
                            title = college.name
                            snippet = "Campus Center • ${college.address}"
                            icon = createCollegeMarkerDrawable(context, PrimaryTeal.toArgb())
                        }
                        mapView.overlays.add(collegeMarker)
                    }

                    // 2. Campus Facility Pins
                    validFacilities.forEach { facility ->
                        val lat = facility.latitude ?: return@forEach
                        val lng = facility.longitude ?: return@forEach

                        val isSelected = facility.id == selectedFacility?.id
                        val categoryColor = when (facility.category) {
                            FacilityCategory.DRINKING_WATER -> PrimaryTeal
                            FacilityCategory.SHADED_AREA -> Color(0xFF2E7D32)
                            FacilityCategory.COOL_INDOOR_SPACE -> Color(0xFF0288D1)
                            FacilityCategory.FIRST_AID -> Color(0xFFD32F2F)
                        }

                        val marker = Marker(mapView).apply {
                            position = GeoPoint(lat, lng)
                            title = facility.name
                            snippet = "${facility.category.displayName} • ${facility.buildingLocation}"
                            icon = createCategoryMarkerDrawable(context, categoryColor.toArgb(), isSelected)
                            setOnMarkerClickListener { m, _ ->
                                onFacilitySelected(facility)
                                m.showInfoWindow()
                                true
                            }
                        }

                        mapView.overlays.add(marker)

                        if (isSelected) {
                            marker.showInfoWindow()
                            mapView.controller.animateTo(GeoPoint(lat, lng))
                        }
                    }

                    mapView.invalidate()
                }
            )

            // OpenStreetMap Attribution Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceWhite.copy(alpha = 0.92f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Geographic Campus Map", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Text(selectedCollege?.name ?: "Bengaluru, Karnataka", style = MaterialTheme.typography.labelSmall, color = PrimaryTeal)
                Text("© OpenStreetMap contributors", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }

            // Controls Overlay (Full-screen expand & My Location buttons)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fullscreen Expand / Exit Toggle Button
                IconButton(
                    onClick = onToggleFullscreen,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite.copy(alpha = 0.95f))
                        .border(1.dp, BorderLight, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.CloseFullscreen else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Expand Fullscreen Map",
                        tint = PrimaryTeal
                    )
                }

                // Current Location Button
                IconButton(
                    onClick = onMyLocationClicked,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite.copy(alpha = 0.95f))
                        .border(1.dp, BorderLight, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = PrimaryTeal
                    )
                }
            }

            if (invalidCount > 0 && !isFullscreen) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceWhite.copy(alpha = 0.92f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Text("$invalidCount building-only facility in Directory List", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

private fun createCategoryMarkerDrawable(context: Context, colorArgb: Int, isSelected: Boolean): Drawable {
    val size = if (isSelected) 56 else 44
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        isAntiAlias = true
        color = colorArgb
        style = Paint.Style.FILL
    }

    val strokePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    val rect = RectF(6f, 6f, (size - 6).toFloat(), (size - 6).toFloat())
    canvas.drawOval(rect, paint)
    canvas.drawOval(rect, strokePaint)

    return BitmapDrawable(context.resources, bitmap)
}

private fun createCollegeMarkerDrawable(context: Context, colorArgb: Int): Drawable {
    val size = 64
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        isAntiAlias = true
        color = colorArgb
        style = Paint.Style.FILL
    }

    val strokePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    val rect = RectF(8f, 8f, (size - 8).toFloat(), (size - 8).toFloat())
    canvas.drawRoundRect(rect, 16f, 16f, paint)
    canvas.drawRoundRect(rect, 16f, 16f, strokePaint)

    return BitmapDrawable(context.resources, bitmap)
}
