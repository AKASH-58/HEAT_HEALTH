package com.heathealth.app

import com.heathealth.app.data.local.datastore.UserPreferences
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.util.LanguageManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MultilingualSupportTest {

    @Test
    fun testAllSixSupportedLanguagesAreDefined() {
        val languages = LanguageManager.SUPPORTED_LANGUAGES
        assertEquals(6, languages.size)

        val codes = languages.map { it.code }
        assertTrue(codes.contains("en"))
        assertTrue(codes.contains("hi"))
        assertTrue(codes.contains("kn"))
        assertTrue(codes.contains("te"))
        assertTrue(codes.contains("ta"))
        assertTrue(codes.contains("ml"))
    }

    @Test
    fun testNativeLanguageNamesAreCorrect() {
        val langMap = LanguageManager.SUPPORTED_LANGUAGES.associateBy { it.code }

        assertEquals("English", langMap["en"]?.nativeName)
        assertEquals("हिन्दी", langMap["hi"]?.nativeName)
        assertEquals("ಕನ್ನಡ", langMap["kn"]?.nativeName)
        assertEquals("తెలుగు", langMap["te"]?.nativeName)
        assertEquals("தமிழ்", langMap["ta"]?.nativeName)
        assertEquals("മലയാളം", langMap["ml"]?.nativeName)
    }

    @Test
    fun testFormattedLanguageNamesDisplayBothEnglishAndNative() {
        assertEquals("English — English", LanguageManager.getFormattedLanguageName("en"))
        assertEquals("Hindi — हिन्दी", LanguageManager.getFormattedLanguageName("hi"))
        assertEquals("Kannada — ಕನ್ನಡ", LanguageManager.getFormattedLanguageName("kn"))
        assertEquals("Telugu — తెలుగు", LanguageManager.getFormattedLanguageName("te"))
        assertEquals("Tamil — தமிழ்", LanguageManager.getFormattedLanguageName("ta"))
        assertEquals("Malayalam — മലയാളം", LanguageManager.getFormattedLanguageName("ml"))
    }

    @Test
    fun testUserPreferencesDefaultLanguageIsEnglish() {
        val prefs = UserPreferences()
        assertEquals("en", prefs.selectedLanguage)
    }

    @Test
    fun testNullContextUserPreferencesRepositoryEmitsDefaultLanguage() = runTest {
        val repo = UserPreferencesRepository(null)
        val prefs = repo.userPreferencesFlow.first()
        assertNotNull(prefs)
        assertEquals("en", prefs.selectedLanguage)
    }
}
