package id.nkz.nokontzzzmanager.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    private const val PREFS_NAME = "settings"
    private const val PREF_KEY_APP_LOCALE = "app_locale"

    // Force in-app language picker for all versions due to system intent issues on some devices.
    val useSystemLanguageSettings: Boolean
        get() = false

    fun applyLanguage(context: Context): Context {
        if (useSystemLanguageSettings) {
            // This block is now unused but kept for reference.
            // On Android 13+, the system handles the locale persistence and resource configuration.
            return context
        }
        // For older versions (and now all versions), we read from prefs and apply it to the context.
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val localeTag = prefs.getString(PREF_KEY_APP_LOCALE, "system") ?: "system"
        
        val locale = when (localeTag) {
            "system" -> null
            else -> parseLocaleTag(localeTag)
        }

        return updateResources(context, locale)
    }

    fun setLocale(context: Context, localeTag: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY_APP_LOCALE, localeTag).apply()

        val localeList = if (localeTag == "system") {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            val appLocale = parseLocaleTag(localeTag)
            LocaleListCompat.create(appLocale)
        }
        
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    // This function is now unused but kept for reference.
    fun launchSystemLanguageSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open language settings: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getCurrentLocaleTag(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_KEY_APP_LOCALE, "system") ?: "system"
    }

    fun getLocaleDisplayName(context: Context, localeTag: String): String {
        return when (localeTag) {
            "system" -> context.getString(id.nkz.nokontzzzmanager.R.string.theme_system)
            "en" -> "English"
            "in" -> "Indonesia"
            else -> parseLocaleTag(localeTag).displayName
        }
    }

    private fun parseLocaleTag(tag: String): Locale {
        return try {
            if (tag.contains("_")) {
                val parts = tag.split("_")
                Locale(parts[0], parts.getOrNull(1) ?: "")
            } else {
                Locale(tag)
            }
        } catch (e: Exception) {
            Locale.getDefault()
        }
    }

    private fun updateResources(context: Context, locale: Locale?): Context {
        val newConfig = Configuration(context.resources.configuration)
        val targetLocale = locale ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
        
        newConfig.setLocale(targetLocale)
        Locale.setDefault(targetLocale)
        
        return context.createConfigurationContext(newConfig)
    }

    fun restartActivity(context: Context) {
        if (context is Activity) {
            context.recreate()
        }
    }
}
