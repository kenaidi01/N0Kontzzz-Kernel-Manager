package id.nkz.nokontzzzmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.nkz.nokontzzzmanager.ui.theme.ThemeMode
import id.nkz.nokontzzzmanager.util.ThemeManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {

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
