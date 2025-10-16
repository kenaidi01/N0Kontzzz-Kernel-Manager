package id.nkz.nokontzzzmanager.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class PreferenceManager @Inject constructor(
    @field:ApplicationContext private val context: Context,
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("nkm_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TARGET_GAME_PACKAGES = "target_game_packages"
        private const val KEY_KGSL_SKIP_ZEROING = "kgsl_skip_zeroing"
        private const val KEY_BYPASS_CHARGING = "bypass_charging"
    }

    fun setTargetGamePackages(packages: Set<String>) {
        sharedPreferences.edit {
            putStringSet(KEY_TARGET_GAME_PACKAGES, packages)
        }
    }

    fun getTargetGamePackages(): Set<String> {
        return sharedPreferences.getStringSet(KEY_TARGET_GAME_PACKAGES, emptySet()) ?: emptySet()
    }

    fun setKgslSkipZeroing(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_KGSL_SKIP_ZEROING, enabled)
        }
    }

    fun getKgslSkipZeroing(): Boolean {
        return sharedPreferences.getBoolean(KEY_KGSL_SKIP_ZEROING, false)
    }

    fun setBypassCharging(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_BYPASS_CHARGING, enabled)
        }
    }

    fun getBypassCharging(): Boolean {
        return sharedPreferences.getBoolean(KEY_BYPASS_CHARGING, false)
    }
}
