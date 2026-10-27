package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("khawla_preferences", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(
        if (prefs.getString(KEY_LANGUAGE, "ar") == "fr") AppLanguage.FRENCH else AppLanguage.ARABIC
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _theme = MutableStateFlow(prefs.getString(KEY_THEME, "light") ?: "light")
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _isOmarVisible = MutableStateFlow(prefs.getBoolean(KEY_OMAR_VISIBLE, true))
    val isOmarVisible: StateFlow<Boolean> = _isOmarVisible.asStateFlow()

    private val _isOnboardingCompleted =
        MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_DONE, false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _soundVibrateEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_SOUND_VIBRATE, true))
    val soundVibrateEnabled: StateFlow<Boolean> = _soundVibrateEnabled.asStateFlow()

    private val _focusDurationMinutes =
        MutableStateFlow(prefs.getInt(KEY_FOCUS_MINUTES, 25))
    val focusDurationMinutes: StateFlow<Int> = _focusDurationMinutes.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, lang.code).apply()
        _language.value = lang
    }

    fun setTheme(themeMode: String) { // "light", "dark", "system"
        prefs.edit().putString(KEY_THEME, themeMode).apply()
        _theme.value = themeMode
    }

    fun setOmarVisible(visible: Boolean) {
        prefs.edit().putBoolean(KEY_OMAR_VISIBLE, visible).apply()
        _isOmarVisible.value = visible
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
        _isOnboardingCompleted.value = completed
    }

    fun setSoundVibrateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_VIBRATE, enabled).apply()
        _soundVibrateEnabled.value = enabled
    }

    fun setFocusDurationMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_FOCUS_MINUTES, minutes).apply()
        _focusDurationMinutes.value = minutes
    }

    companion object {
        private const val KEY_LANGUAGE = "key_app_language"
        private const val KEY_THEME = "key_app_theme"
        private const val KEY_OMAR_VISIBLE = "key_omar_visible"
        private const val KEY_ONBOARDING_DONE = "key_onboarding_completed"
        private const val KEY_SOUND_VIBRATE = "key_sound_vibrate"
        private const val KEY_FOCUS_MINUTES = "key_focus_minutes"
    }
}
