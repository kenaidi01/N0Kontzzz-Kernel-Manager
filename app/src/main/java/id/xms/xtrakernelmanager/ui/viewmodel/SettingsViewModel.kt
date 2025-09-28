package id.xms.xtrakernelmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.xms.xtrakernelmanager.ui.theme.ThemeMode
import id.xms.xtrakernelmanager.util.Language
import id.xms.xtrakernelmanager.util.LanguageManager
import id.xms.xtrakernelmanager.util.ThemeManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val themeManager: ThemeManager
) : ViewModel() {

    val currentLanguage: StateFlow<Language> = languageManager.currentLanguage
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            Language.ENGLISH
        )

    val currentThemeMode: StateFlow<ThemeMode> = themeManager.currentThemeMode
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ThemeMode.SYSTEM_DEFAULT
        )

    val isAmoledMode: StateFlow<Boolean> = themeManager.isAmoledMode
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    val themeChanged: StateFlow<Boolean> = themeManager.themeChanged
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    fun setLanguage(language: Language) {
        viewModelScope.launch {
            languageManager.setLanguage(language)
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            themeManager.setThemeMode(themeMode)
        }
    }

    fun setAmoledMode(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setAmoledMode(enabled)
        }
    }

    fun resetThemeChangedSignal() {
        themeManager.resetThemeChangedSignal()
    }
}
