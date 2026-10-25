package com.heathealth.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LocationHelperTest {

    @Test
    fun testDeviceLocationResultDataClass() {
        val result = com.heathealth.app.util.DeviceLocationResult(
            latitude = 12.9716,
            longitude = 77.5946,
            locationLabel = "Bengaluru, India"
        )
        assertEquals(12.9716, result.latitude, 0.0001)
        assertEquals(77.5946, result.longitude, 0.0001)
        assertEquals("Bengaluru, India", result.locationLabel)
    }
}
