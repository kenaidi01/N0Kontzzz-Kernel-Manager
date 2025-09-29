package id.nkz.nokontzzzmanager.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.nkz.nokontzzzmanager.util.LanguageManager
import id.nkz.nokontzzzmanager.util.ThemeManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LanguageModule {

    @Provides
    @Singleton
    fun provideLanguageManager(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>
    ): LanguageManager {
        return LanguageManager(context, dataStore)
    }

    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>
    ): ThemeManager {
        return ThemeManager(context, dataStore)
    }
}
