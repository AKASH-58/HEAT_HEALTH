package com.heathealth.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.heathealth.app.ui.screens.DashboardScreen
import com.heathealth.app.ui.theme.HeatHealthTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HeatHealthTheme {
                DashboardScreen()
            }
        }
    }
}
