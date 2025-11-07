package id.nkz.nokontzzzmanager.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import android.os.Build

@Singleton
class PreferenceManager @Inject constructor(
    @field:ApplicationContext private val context: Context,
) {
    companion object {
        private const val KEY_TARGET_GAME_PACKAGES = "target_game_packages"
        private const val KEY_KGSL_SKIP_ZEROING = "kgsl_skip_zeroing"
        private const val KEY_BYPASS_CHARGING = "bypass_charging"
        private const val KEY_BATTERY_MONITOR_ENABLED = "battery_monitor_enabled"
    }

    private fun credentialPrefs(): SharedPreferences? {
        return try {
            context.getSharedPreferences("nkm_preferences", Context.MODE_PRIVATE)
        } catch (_: IllegalStateException) {
            null
        }
    }

    private fun deviceProtectedPrefs(): SharedPreferences? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val deviceContext = context.createDeviceProtectedStorageContext()
            deviceContext.getSharedPreferences("nkm_preferences", Context.MODE_PRIVATE)
        } else {
            null
        }
    }

    fun setTargetGamePackages(packages: Set<String>) {
        credentialPrefs()?.edit {
            putStringSet(KEY_TARGET_GAME_PACKAGES, packages)
        }
    }

    fun getTargetGamePackages(): Set<String> {
        credentialPrefs()?.getStringSet(KEY_TARGET_GAME_PACKAGES, null)?.let { return it }
        return deviceProtectedPrefs()?.getStringSet(KEY_TARGET_GAME_PACKAGES, emptySet()) ?: emptySet()
    }

    fun setKgslSkipZeroing(enabled: Boolean) {
        credentialPrefs()?.edit {
            putBoolean(KEY_KGSL_SKIP_ZEROING, enabled)
        }
    }

    fun getKgslSkipZeroing(): Boolean {
        credentialPrefs()?.getBoolean(KEY_KGSL_SKIP_ZEROING, false)?.let { return it }
        return deviceProtectedPrefs()?.getBoolean(KEY_KGSL_SKIP_ZEROING, false) ?: false
    }

    fun setBypassCharging(enabled: Boolean) {
        credentialPrefs()?.edit {
            putBoolean(KEY_BYPASS_CHARGING, enabled)
        }
    }

    fun getBypassCharging(): Boolean {
        credentialPrefs()?.getBoolean(KEY_BYPASS_CHARGING, false)?.let { return it }
        return deviceProtectedPrefs()?.getBoolean(KEY_BYPASS_CHARGING, false) ?: false
    }

    fun setBatteryMonitorEnabled(enabled: Boolean) {
        credentialPrefs()?.edit {
            putBoolean(KEY_BATTERY_MONITOR_ENABLED, enabled)
        }
        deviceProtectedPrefs()?.edit {
            putBoolean(KEY_BATTERY_MONITOR_ENABLED, enabled)
        }
    }

    fun isBatteryMonitorEnabled(): Boolean {
        val primary = credentialPrefs()?.getBoolean(KEY_BATTERY_MONITOR_ENABLED, false)
        if (primary == true) return true
        val fallback = deviceProtectedPrefs()?.getBoolean(KEY_BATTERY_MONITOR_ENABLED, false)
        return fallback ?: false
    }
}
