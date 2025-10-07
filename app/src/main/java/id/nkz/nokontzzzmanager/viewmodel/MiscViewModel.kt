package id.nkz.nokontzzzmanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.nkz.nokontzzzmanager.data.repository.SystemRepository
import id.nkz.nokontzzzmanager.utils.PreferenceManager
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

    private val _isKgslFeatureAvailable = MutableStateFlow<Boolean?>(null)
    val isKgslFeatureAvailable: StateFlow<Boolean?> = _isKgslFeatureAvailable.asStateFlow()

    private val _tcpCongestionAlgorithm = MutableStateFlow<String?>(null)
    val tcpCongestionAlgorithm: StateFlow<String?> = _tcpCongestionAlgorithm.asStateFlow()

    private val _availableTcpCongestionAlgorithms = MutableStateFlow<List<String>>(emptyList())
    val availableTcpCongestionAlgorithms: StateFlow<List<String>> = _availableTcpCongestionAlgorithms.asStateFlow()

    private val _ioScheduler = MutableStateFlow<String?>(null)
    val ioScheduler: StateFlow<String?> = _ioScheduler.asStateFlow()

    private val _availableIoSchedulers = MutableStateFlow<List<String>>(emptyList())
    val availableIoSchedulers: StateFlow<List<String>> = _availableIoSchedulers.asStateFlow()

    private val isDataLoaded = java.util.concurrent.atomic.AtomicBoolean(false)

    init {
        // Data is now loaded lazily by the UI by calling loadInitialData()
    }

    fun loadInitialData() {
        if (isDataLoaded.getAndSet(true)) return

        // Load saved preferences on init
        _kgslSkipZeroingEnabled.value = preferenceManager.getKgslSkipZeroing()
        
        // Check if KGSL feature is available
        _isKgslFeatureAvailable.value = systemRepository.isKgslFeatureAvailable()
        
        // Load TCP congestion algorithm
        loadTcpCongestionAlgorithm()
        
        // Load I/O scheduler
        loadIoScheduler()
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

    private fun loadIoScheduler() {
        viewModelScope.launch {
            _ioScheduler.value = systemRepository.getIoScheduler()
            _availableIoSchedulers.value = systemRepository.getAvailableIoSchedulersList()
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

    fun updateIoScheduler(scheduler: String) {
        viewModelScope.launch {
            val success = systemRepository.setIoScheduler(scheduler)
            if (success) {
                // Update the current scheduler in state
                _ioScheduler.value = scheduler
            } else {
                // If failed, reload the actual current value
                _ioScheduler.value = systemRepository.getIoScheduler()
            }
        }
    }
}