package id.xms.xtrakernelmanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.xms.xtrakernelmanager.data.repository.SystemRepository
import id.xms.xtrakernelmanager.utils.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiscViewModel @Inject constructor(
    private val application: Application,
    private val preferenceManager: PreferenceManager,
    private val systemRepository: SystemRepository
) : AndroidViewModel(application) {

    private val _kgslSkipZeroingEnabled = MutableStateFlow(false)
    val kgslSkipZeroingEnabled: StateFlow<Boolean> = _kgslSkipZeroingEnabled.asStateFlow()

    private val _isKgslFeatureAvailable = MutableStateFlow(false)
    val isKgslFeatureAvailable: StateFlow<Boolean> = _isKgslFeatureAvailable.asStateFlow()

    private val _tcpCongestionAlgorithm = MutableStateFlow("")
    val tcpCongestionAlgorithm: StateFlow<String> = _tcpCongestionAlgorithm.asStateFlow()

    private val _availableTcpCongestionAlgorithms = MutableStateFlow<List<String>>(emptyList())
    val availableTcpCongestionAlgorithms: StateFlow<List<String>> = _availableTcpCongestionAlgorithms.asStateFlow()

    init {
        // Load saved preferences on init
        _kgslSkipZeroingEnabled.value = preferenceManager.getKgslSkipZeroing()
        
        // Check if KGSL feature is available
        _isKgslFeatureAvailable.value = systemRepository.isKgslFeatureAvailable()
        
        // Load TCP congestion algorithm
        loadTcpCongestionAlgorithm()
    }

    fun toggleKgslSkipZeroing(enabled: Boolean) {
        viewModelScope.launch {
            // Try to set the value in the kernel
            val success = systemRepository.setKgslSkipZeroing(enabled)
            
            if (success) {
                // Update state and save preference
                _kgslSkipZeroingEnabled.value = enabled
                preferenceManager.setKgslSkipZeroing(enabled)
            } else {
                // If failed, revert the state to the actual value
                _kgslSkipZeroingEnabled.value = systemRepository.getKgslSkipZeroing()
                preferenceManager.setKgslSkipZeroing(_kgslSkipZeroingEnabled.value)
            }
        }
    }

    private fun loadTcpCongestionAlgorithm() {
        viewModelScope.launch {
            _tcpCongestionAlgorithm.value = systemRepository.getTcpCongestionAlgorithm()
            _availableTcpCongestionAlgorithms.value = systemRepository.getAvailableTcpCongestionAlgorithmsList()
        }
    }

    fun updateTcpCongestionAlgorithm(algorithm: String) {
        viewModelScope.launch {
            val success = systemRepository.setTcpCongestionAlgorithm(algorithm)
            if (success) {
                // Update the current algorithm in state
                _tcpCongestionAlgorithm.value = algorithm
            } else {
                // If failed, reload the actual current value
                _tcpCongestionAlgorithm.value = systemRepository.getTcpCongestionAlgorithm()
            }
        }
    }
}