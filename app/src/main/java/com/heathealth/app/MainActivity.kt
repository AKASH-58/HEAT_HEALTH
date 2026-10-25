package com.heathealth.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.ui.screens.MainScreen
import com.heathealth.app.ui.screens.SplashScreen
import com.heathealth.app.ui.theme.HeatHealthTheme
import com.heathealth.app.util.LanguageManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val navigateTo = intent?.getStringExtra("navigate_to") ?: "home"
        val showSplashInitially = navigateTo == "home"

        val userPrefsRepository = UserPreferencesRepository(applicationContext)

        setContent {
            val userPrefs by userPrefsRepository.userPreferencesFlow.collectAsState(
                initial = com.heathealth.app.data.local.datastore.UserPreferences()
            )
            val context = LocalContext.current

            LaunchedEffect(userPrefs.selectedLanguage) {
                LanguageManager.applyLanguage(context, userPrefs.selectedLanguage)
            }

            HeatHealthTheme {
                var showSplash by remember { mutableStateOf(showSplashInitially) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    MainScreen(
                        initialRoute = navigateTo,
                        selectedLanguage = userPrefs.selectedLanguage,
                        onLanguageSelected = { langCode ->
                            LanguageManager.applyLanguage(context, langCode)
                        }
                    )
                }
            }
        }
    }
}
