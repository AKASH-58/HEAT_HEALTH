package com.heathealth.app.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

data class SupportedLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String
)

object LanguageManager {

    val SUPPORTED_LANGUAGES = listOf(
        SupportedLanguage("en", "English", "English"),
        SupportedLanguage("hi", "हिन्दी", "Hindi"),
        SupportedLanguage("kn", "ಕನ್ನಡ", "Kannada"),
        SupportedLanguage("te", "తెలుగు", "Telugu"),
        SupportedLanguage("ta", "தமிழ்", "Tamil"),
        SupportedLanguage("ml", "മലയാളം", "Malayalam")
    )

    fun applyLanguage(context: Context, languageCode: String) {
        val validCode = if (SUPPORTED_LANGUAGES.any { it.code == languageCode }) languageCode else "en"
        val locale = Locale.Builder().setLanguage(validCode).build()
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getCurrentLanguageCode(): String {
        val systemLocale = Locale.getDefault().language
        return if (SUPPORTED_LANGUAGES.any { it.code == systemLocale }) systemLocale else "en"
    }

    fun getFormattedLanguageName(code: String): String {
        val lang = SUPPORTED_LANGUAGES.find { it.code == code }
        return if (lang != null) "${lang.englishName} — ${lang.nativeName}" else "English — English"
    }
}
