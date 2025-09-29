package id.nkz.nokontzzzmanager

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.HiltAndroidApp
import android.util.Log

@HiltAndroidApp
class NkzApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        // Initialize Superuser shell with proper flags
        Shell.enableVerboseLogging = true  // Enable during development
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )

        // Initialize theme mode - this will be managed by ThemeManager
        // The actual theme will be applied in the MainActivity based on user preference

        // Inisialisasi WorkManager secara manual
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG) // Logging untuk debug
                .build()
        )
        Log.d("NkzApp", "WorkManager initialized successfully")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG) // Logging untuk debugging
            .build()
}