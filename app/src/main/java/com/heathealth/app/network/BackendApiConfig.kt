package com.heathealth.app.network

/**
 * Development Network Configuration for HeatHealth Local Backend Integration.
 *
 * For local Phase 1 development:
 * - Android Emulator Host URL: "http://10.0.2.2:8000/"
 * - Physical Phone Host URL: "http://<YOUR_COMPUTER_LAN_IP>:8000/" (e.g., http://192.168.1.100:8000/)
 *
 * Requirements for HTTP local connections:
 * - android.permission.INTERNET in AndroidManifest.xml
 * - android:usesCleartextTraffic="true" for HTTP dev testing
 * - Production deployments must mandate HTTPS endpoints and encrypted network channels.
 */
object BackendApiConfig {
    const val EMULATOR_BASE_URL = "http://10.0.2.2:8000/"
    const val HEALTH_ENDPOINT = "health"
}
