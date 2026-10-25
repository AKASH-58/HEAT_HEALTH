package com.heathealth.app.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

data class DeviceLocationResult(
    val latitude: Double,
    val longitude: Double,
    val locationLabel: String
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun fetchDeviceLocation(
        context: Context,
        onLocationRetrieved: (DeviceLocationResult) -> Unit,
        onFailed: (String) -> Unit = {}
    ) {
        if (!hasLocationPermission(context)) {
            onFailed("Location permission not granted.")
            return
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cancelSource = CancellationTokenSource()

            fusedClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancelSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val result = buildLocationResult(context, location.latitude, location.longitude)
                    onLocationRetrieved(result)
                } else {
                    fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        if (lastLoc != null) {
                            val result = buildLocationResult(context, lastLoc.latitude, lastLoc.longitude)
                            onLocationRetrieved(result)
                        } else {
                            fetchLocationViaSystemManager(context, onLocationRetrieved, onFailed)
                        }
                    }.addOnFailureListener {
                        fetchLocationViaSystemManager(context, onLocationRetrieved, onFailed)
                    }
                }
            }.addOnFailureListener {
                fetchLocationViaSystemManager(context, onLocationRetrieved, onFailed)
            }
        } catch (e: Throwable) {
            fetchLocationViaSystemManager(context, onLocationRetrieved, onFailed)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationViaSystemManager(
        context: Context,
        onLocationRetrieved: (DeviceLocationResult) -> Unit,
        onFailed: (String) -> Unit
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onFailed("Location service unavailable.")
            return
        }

        var bestLocation: Location? = null

        try {
            val gpsLocation = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else null

            val networkLocation = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else null

            bestLocation = when {
                gpsLocation != null && networkLocation != null -> {
                    if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
                }
                gpsLocation != null -> gpsLocation
                networkLocation != null -> networkLocation
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (bestLocation != null) {
            val result = buildLocationResult(context, bestLocation.latitude, bestLocation.longitude)
            onLocationRetrieved(result)
            return
        }

        try {
            val provider = when {
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                else -> null
            }

            if (provider != null) {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        try {
                            locationManager.removeUpdates(this)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        val result = buildLocationResult(context, location.latitude, location.longitude)
                        onLocationRetrieved(result)
                    }

                    @Deprecated("Deprecated in API 29")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                locationManager.requestSingleUpdate(provider, listener, null)
            } else {
                onFailed("Location providers disabled.")
            }
        } catch (e: Exception) {
            onFailed(e.localizedMessage ?: "Unable to fetch location.")
        }
    }

    fun buildLocationResult(context: Context, lat: Double, lng: Double): DeviceLocationResult {
        var label = "Current Location (${String.format(Locale.US, "%.2f", lat)}, ${String.format(Locale.US, "%.2f", lng)})"
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                val country = addr.countryName
                if (!city.isNullOrEmpty() && !country.isNullOrEmpty()) {
                    label = "$city, $country"
                } else if (!city.isNullOrEmpty()) {
                    label = city
                } else if (!addr.adminArea.isNullOrEmpty()) {
                    label = "${addr.adminArea}, ${addr.countryName ?: ""}".trimEnd(',', ' ')
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return DeviceLocationResult(lat, lng, label)
    }
}
