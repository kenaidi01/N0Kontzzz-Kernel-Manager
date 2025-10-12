package id.nkz.nokontzzzmanager.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.nkz.nokontzzzmanager.data.repository.SystemRepository
import id.nkz.nokontzzzmanager.data.repository.ThermalRepository
import id.nkz.nokontzzzmanager.data.repository.TuningRepository
import id.nkz.nokontzzzmanager.service.ThermalService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class TuningViewModel @Inject constructor(
    private val application: Application,
    private val repo: TuningRepository,
    private val thermalRepo: ThermalRepository,
    private val systemRepo: SystemRepository
) : AndroidViewModel(application) {

    private val thermalPrefs: SharedPreferences by lazy {
        application.getSharedPreferences("thermal_settings_prefs", Context.MODE_PRIVATE)
    }
    private val performancePrefs: SharedPreferences by lazy {
        application.getSharedPreferences("performance_mode_prefs", Context.MODE_PRIVATE)
    }
    private val KEY_LAST_APPLIED_THERMAL_INDEX = "last_applied_thermal_index"
    private val KEY_LAST_APPLIED_PERFORMANCE_MODE = "last_applied_performance_mode"

    val cpuClusters = listOf("cpu0", "cpu4", "cpu7")

    //<editor-fold desc="StateFlows">
    // Dynamic cluster information with proper names
    private val _dynamicCpuClusters = MutableStateFlow<List<String>>(emptyList())
    val dynamicCpuClusters: StateFlow<List<String>> = _dynamicCpuClusters.asStateFlow()

    /* ---------------- CPU ---------------- */
    private val _performanceMode = MutableStateFlow(performancePrefs.getString(KEY_LAST_APPLIED_PERFORMANCE_MODE, "Balanced") ?: "Balanced")
    val performanceMode: StateFlow<String> = _performanceMode.asStateFlow()

    private val _coreStates = MutableStateFlow(List(8) { true })
    val coreStates: StateFlow<List<Boolean>> = _coreStates.asStateFlow()

    private val _generalAvailableCpuGovernors = MutableStateFlow<List<String>>(emptyList())
    val generalAvailableCpuGovernors: StateFlow<List<String>> = _generalAvailableCpuGovernors.asStateFlow()

    private val _availableCpuFrequenciesPerClusterMap = MutableStateFlow<Map<String, List<Int>>>(emptyMap())
    private val _currentCpuGovernors = mutableMapOf<String, MutableStateFlow<String>>()
    private val _currentCpuFrequencies = mutableMapOf<String, MutableStateFlow<Pair<Int, Int>>>()

    /* ---------------- GPU ---------------- */
    private val _availableGpuGovernors = MutableStateFlow<List<String>>(emptyList())
    val availableGpuGovernors: StateFlow<List<String>> = _availableGpuGovernors.asStateFlow()

    private val _currentGpuGovernor = MutableStateFlow("...")
    val currentGpuGovernor: StateFlow<String> = _currentGpuGovernor.asStateFlow()

    private val _availableGpuFrequencies = MutableStateFlow<List<Int>>(emptyList())
    val availableGpuFrequencies: StateFlow<List<Int>> = _availableGpuFrequencies.asStateFlow()

    private val _currentGpuMinFreq = MutableStateFlow(0)
    val currentGpuMinFreq: StateFlow<Int> = _currentGpuMinFreq.asStateFlow()

    private val _currentGpuMaxFreq = MutableStateFlow(0)
    val currentGpuMaxFreq: StateFlow<Int> = _currentGpuMaxFreq.asStateFlow()

    private val _gpuPowerLevelRange = MutableStateFlow(0f to 5f)
    val gpuPowerLevelRange: StateFlow<Pair<Float, Float>> = _gpuPowerLevelRange.asStateFlow()

    private val _currentGpuPowerLevel = MutableStateFlow(0f)
    val currentGpuPowerLevel: StateFlow<Float> = _currentGpuPowerLevel.asStateFlow()

    private val _gpuThrottlingEnabled = MutableStateFlow(false)
    val gpuThrottlingEnabled: StateFlow<Boolean> = _gpuThrottlingEnabled.asStateFlow()

    /* ---------------- OpenGL / Vulkan / Renderer ---------------- */
    private val _currentOpenGlesDriver = MutableStateFlow("Loading...")
    val currentOpenGlesDriver: StateFlow<String> = _currentOpenGlesDriver.asStateFlow()

    private val _currentGpuRenderer = MutableStateFlow("Loading...")
    val currentGpuRenderer: StateFlow<String> = _currentGpuRenderer.asStateFlow()

    private val _vulkanApiVersion = MutableStateFlow("Loading...")
    val vulkanApiVersion: StateFlow<String> = _vulkanApiVersion.asStateFlow()

    val availableGpuRenderers = listOf(
        "Default", "OpenGL", "Vulkan", "ANGLE", "OpenGL (SKIA)", "Vulkan (SKIA)"
    )

    /* ---------------- Reboot dialog ---------------- */
    private val _showRebootConfirmationDialog = MutableStateFlow(false)
    val showRebootConfirmationDialog: StateFlow<Boolean> = _showRebootConfirmationDialog.asStateFlow()

    private val _rebootCommandFeedback = MutableSharedFlow<String>()
    val rebootCommandFeedback: SharedFlow<String> = _rebootCommandFeedback.asSharedFlow()

    /* ---------------- RAM Control ---------------- */
    private val _zramEnabled = MutableStateFlow(false)
    val zramEnabled: StateFlow<Boolean> = _zramEnabled.asStateFlow()

    private val _zramDisksize = MutableStateFlow(536870912L) // 512 MB default
    val zramDisksize: StateFlow<Long> = _zramDisksize.asStateFlow()

    private val _compressionAlgorithms = MutableStateFlow<List<String>>(emptyList())
    val compressionAlgorithms: StateFlow<List<String>> = _compressionAlgorithms.asStateFlow()

    private val _currentCompression = MutableStateFlow("")
    val currentCompression: StateFlow<String> = _currentCompression.asStateFlow()

    private val _swappiness = MutableStateFlow(60)
    val swappiness: StateFlow<Int> = _swappiness.asStateFlow()

    private val _dirtyRatio = MutableStateFlow(20)
    val dirtyRatio: StateFlow<Int> = _dirtyRatio.asStateFlow()

    private val _dirtyBackgroundRatio = MutableStateFlow(10)
    val dirtyBackgroundRatio: StateFlow<Int> = _dirtyBackgroundRatio.asStateFlow()

    private val _dirtyWriteback = MutableStateFlow(30)
    val dirtyWriteback: StateFlow<Int> = _dirtyWriteback.asStateFlow()

    private val _dirtyExpireCentisecs = MutableStateFlow(300)
    val dirtyExpireCentisecs: StateFlow<Int> = _dirtyExpireCentisecs.asStateFlow()

    private val _minFreeMemory = MutableStateFlow(128)
    val minFreeMemory: StateFlow<Int> = _minFreeMemory.asStateFlow()

    // Add swap size state for traditional swap (not ZRAM)
    private val _swapSize = MutableStateFlow(0L) // in bytes
    val swapSize: StateFlow<Long> = _swapSize.asStateFlow()

    private val _maxSwapSize = MutableStateFlow(8L * 1024 * 1024 * 1024) // 8GB max
    val maxSwapSize: StateFlow<Long> = _maxSwapSize.asStateFlow()

    // Add loading states for swap operations
    private val _isSwapLoading = MutableStateFlow(false)
    val isSwapLoading: StateFlow<Boolean> = _isSwapLoading.asStateFlow()

    private val _swapLogs = MutableStateFlow<List<String>>(emptyList())
    val swapLogs: StateFlow<List<String>> = _swapLogs.asStateFlow()

    /* Max ZRAM otomatis 6 GB untuk 8 GB RAM */
    private val _maxZramSize = MutableStateFlow(repo.calculateMaxZramSize())
    val maxZramSize: StateFlow<Long> = _maxZramSize.asStateFlow()

    /* ---------------- Thermal ---------------- */
    private val _currentThermalModeIndex = MutableStateFlow<Int?>(null)
    val currentThermalModeIndex: StateFlow<Int?> = _currentThermalModeIndex.asStateFlow()

    val currentThermalProfileName: StateFlow<String> =
        _currentThermalModeIndex.map { idx ->
            idx?.let { thermalRepo.getCurrentThermalProfileName(it) } ?: "Loading..."
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Loading...")

    val supportedThermalProfiles: StateFlow<List<ThermalRepository.ThermalProfile>> =
        thermalRepo.getSupportedThermalProfiles()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    //</editor-fold>

    //<editor-fold desc="Load Flags">
    private val isCpuDataLoaded = AtomicBoolean(false)
    private val isGpuDataLoaded = AtomicBoolean(false)
    private val isRamDataLoaded = AtomicBoolean(false)
    private val isThermalDataLoaded = AtomicBoolean(false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    //</editor-fold>

    /* ---------------- Init ---------------- */
    init {
        Log.d("TuningVM_Init", "ViewModel initializing...")
        initializeCpuStateFlows()
        fetchDynamicCpuClusters()
        Log.d("TuningVM_Init", "ViewModel initialization complete.")
    }

    //<editor-fold desc="Lazy Load Functions">
    fun loadAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            // Use coroutineScope to wait for all child coroutines to complete
            coroutineScope {
                launch { loadCpuData() }
                launch { loadGpuData() }
                launch { loadRamData() }
                launch { loadThermalData() }
            }
            _isLoading.value = false
        }
    }

    private fun loadCpuData() {
        if (isCpuDataLoaded.getAndSet(true)) return
        Log.d("TuningVM_LazyLoad", "Loading CPU data...")
        viewModelScope.launch(Dispatchers.IO) {
            fetchAllCpuData()
            refreshCoreStates()
        }
    }

    private fun loadGpuData() {
        if (isGpuDataLoaded.getAndSet(true)) return
        Log.d("TuningVM_LazyLoad", "Loading GPU data...")
        viewModelScope.launch(Dispatchers.IO) {
            launch { fetchGpuData() }
            launch { fetchOpenGlesDriver() }
            launch { fetchCurrentGpuRenderer() }
            launch { fetchVulkanApiVersion() }
        }
    }

    private fun loadRamData() {
        if (isRamDataLoaded.getAndSet(true)) return
        Log.d("TuningVM_LazyLoad", "Loading RAM data...")
        viewModelScope.launch(Dispatchers.IO) {
            fetchRamControlData()
        }
    }

    private fun loadThermalData() {
        if (isThermalDataLoaded.getAndSet(true)) return
        Log.d("TuningVM_LazyLoad", "Loading Thermal data...")
        viewModelScope.launch(Dispatchers.IO) {
            fetchCurrentThermalMode(isInitialLoad = true)
        }
    }
    //</editor-fold>

    private fun fetchDynamicCpuClusters() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val clusters = systemRepo.getCpuClusters()
                // Map the dynamic cluster names to the corresponding cpu cluster identifiers
                val clusterNames = clusters.map { cluster ->
                    when {
                        cluster.name.contains("Little", ignoreCase = true) -> "cpu0"
                        cluster.name.contains("Big", ignoreCase = true) -> "cpu4"
                        cluster.name.contains("Prime", ignoreCase = true) -> "cpu7"
                        else -> {
                            // Fallback to original naming
                            when (cluster.name) {
                                "Efficiency Cluster" -> "cpu0"
                                "Performance Cluster" -> "cpu7"
                                else -> "cpu4" // Mid cluster
                            }
                        }
                    }
                }
                _dynamicCpuClusters.value = clusterNames
            } catch (e: Exception) {
                Log.e("TuningViewModel", "Error fetching dynamic CPU clusters", e)
                // Fallback to hardcoded values
                _dynamicCpuClusters.value = cpuClusters
            }
        }
    }

    /* ---------------- CPU ---------------- */
    private fun initializeCpuStateFlows() {
        Log.d("TuningVM_Init", "Initializing CPU StateFlows for clusters: $cpuClusters")
        cpuClusters.forEach { cluster ->
            _currentCpuGovernors.getOrPut(cluster) { MutableStateFlow("...") }
            _currentCpuFrequencies.getOrPut(cluster) { MutableStateFlow(0 to 0) }
        }
    }

    private suspend fun fetchAllCpuData() {
        Log.d("TuningVM_CPU", "Fetching all CPU data...")
        val tempGovernors = mutableMapOf<String, List<String>>()
        val tempFreqs = mutableMapOf<String, List<Int>>()

        try {
            coroutineScope {
                cpuClusters.forEach { cluster ->
                    launch {
                        try {
                            repo.getCpuGov(cluster).take(1).collect { _currentCpuGovernors[cluster]?.value = it }
                        } catch (e: Exception) {
                            Log.e("TuningVM_CPU", "Error fetching CPU governor for $cluster", e)
                        }
                    }
                    launch {
                        try {
                            repo.getCpuFreq(cluster).take(1).collect { _currentCpuFrequencies[cluster]?.value = it }
                        } catch (e: Exception) {
                            Log.e("TuningVM_CPU", "Error fetching CPU frequency for $cluster", e)
                        }
                    }
                    launch {
                        try {
                            repo.getAvailableCpuGovernors(cluster).collect { tempGovernors[cluster] = it }
                        } catch (e: Exception) {
                            Log.e("TuningVM_CPU", "Error fetching available CPU governors for $cluster", e)
                        }
                    }
                    launch {
                        try {
                            repo.getAvailableCpuFrequencies(cluster).collect { tempFreqs[cluster] = it }
                        } catch (e: Exception) {
                            Log.e("TuningVM_CPU", "Error fetching available CPU frequencies for $cluster", e)
                        }
                    }
                }
            }
            _availableCpuFrequenciesPerClusterMap.value = tempFreqs
            if (tempGovernors.isNotEmpty()) _generalAvailableCpuGovernors.value = tempGovernors.values.flatten().distinct().sorted()

            Log.d("TuningVM_CPU", "Finished fetching all CPU data.")
        } catch (e: Exception) {
            Log.e("TuningVM_CPU", "Error in fetchAllCpuData", e)
        }
    }

    fun getCpuGov(cluster: String): StateFlow<String> = _currentCpuGovernors.getOrPut(cluster) { MutableStateFlow("...") }.asStateFlow()
    fun getCpuFreq(cluster: String): StateFlow<Pair<Int, Int>> = _currentCpuFrequencies.getOrPut(cluster) { MutableStateFlow(0 to 0) }.asStateFlow()
    fun getAvailableCpuFrequencies(cluster: String): StateFlow<List<Int>> = _availableCpuFrequenciesPerClusterMap.map { it[cluster] ?: emptyList() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun setCpuGov(cluster: String, gov: String) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setCpuGov(cluster, gov)) {
            repo.getCpuGov(cluster).take(1).collect { _currentCpuGovernors[cluster]?.value = it }
        }
    }

    fun setCpuFreq(cluster: String, min: Int, max: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setCpuFreq(cluster, min, max)) {
            repo.getCpuFreq(cluster).take(1).collect { _currentCpuFrequencies[cluster]?.value = it }
        }
    }

    fun onPerformanceModeChange(mode: String) {
        _performanceMode.value = mode
        performancePrefs.edit {
            putString(KEY_LAST_APPLIED_PERFORMANCE_MODE, mode)
        }
        val governor = when (mode) {
            "Performance" -> "performance"
            else -> "schedutil"
        }
        cpuClusters.forEach { cluster ->
            setCpuGov(cluster, governor)
        }
    }

    fun toggleCore(coreId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val newStates = _coreStates.value.toMutableList()
        val newState = !newStates[coreId]
        if (!newState && newStates.count { it } == 1) {
            _rebootCommandFeedback.emit("Setidaknya 1 core harus tetap online")
            return@launch
        }
        if (repo.setCoreOnline(coreId, newState)) {
            newStates[coreId] = newState
            _coreStates.value = newStates
        } else {
            Log.e("TuningVM_CPU", "Failed toggle core $coreId")
        }
    }

    private fun refreshCoreStates() = viewModelScope.launch(Dispatchers.IO) {
        _coreStates.value = (0 until 8).map { repo.getCoreOnline(it) }
    }

    /* ---------------- GPU ---------------- */
    private fun fetchGpuData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            _availableGpuGovernors.value = repo.getAvailableGpuGovernors().first()
            _currentGpuGovernor.value = repo.getGpuGov().first()
            _availableGpuFrequencies.value = repo.getAvailableGpuFrequencies().first()
            val (min, max) = repo.getGpuFreq().first()
            _currentGpuMinFreq.value = min
            _currentGpuMaxFreq.value = max
            Log.d("ViewModelGPU", "StateFlows updated: _currentGpuMinFreq=${_currentGpuMinFreq.value}, _currentGpuMaxFreq=${_currentGpuMaxFreq.value}")
            _gpuPowerLevelRange.value = repo.getGpuPowerLevelRange().first()
            _currentGpuPowerLevel.value = repo.getCurrentGpuPowerLevel().first()
            // Fetch GPU throttling status
            _gpuThrottlingEnabled.value = systemRepo.isGpuThrottlingEnabled()
        } catch (e: Exception) {
            Log.e("ViewModelGPU", "Error fetching GPU data", e)
        }
    }

    fun setGpuGovernor(gov: String) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setGpuGov(gov)) {
            repo.getGpuGov().take(1).collect { _currentGpuGovernor.value = it }
        }
    }

    fun setGpuMinFrequency(freqKHz: Int) = viewModelScope.launch(Dispatchers.IO) {
        repo.setGpuMinFreq(freqKHz)
        if (repo.setGpuMinFreq(freqKHz)) {
            val (min, _) = repo.getGpuFreq().first()
            _currentGpuMinFreq.value = min
            fetchGpuData()
        }
    }

    fun setGpuMaxFrequency(freqKHz: Int) = viewModelScope.launch(Dispatchers.IO) {
        repo.setGpuMaxFreq(freqKHz)
        if (repo.setGpuMaxFreq(freqKHz)) {
            val (_, max) = repo.getGpuFreq().first()
            _currentGpuMaxFreq.value = max
            fetchGpuData()
        }
    }

    fun setGpuPowerLevel(level: Float) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setGpuPowerLevel(level)) {
            repo.getCurrentGpuPowerLevel().take(1).collect { _currentGpuPowerLevel.value = it }
        }
    }

    fun toggleGpuThrottling(enabled: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val success = systemRepo.setGpuThrottling(enabled)
        if (success) {
            _gpuThrottlingEnabled.value = enabled
        } else {
            // If failed, refresh the actual value from system
            _gpuThrottlingEnabled.value = systemRepo.isGpuThrottlingEnabled()
        }
    }

    /* ---------------- OpenGL / Vulkan ---------------- */
    private fun fetchOpenGlesDriver() = viewModelScope.launch(Dispatchers.IO) {
        repo.getOpenGlesDriver().collect { _currentOpenGlesDriver.value = it }
    }

    private fun fetchCurrentGpuRenderer() = viewModelScope.launch(Dispatchers.IO) {
        repo.getGpuRenderer().collect { renderer ->
            _currentGpuRenderer.value = renderer
        }
    }

    private fun fetchVulkanApiVersion() = viewModelScope.launch(Dispatchers.IO) {
        repo.getVulkanApiVersion().collect { version ->
            _vulkanApiVersion.value = version
        }
    }

    fun userSelectedGpuRenderer(renderer: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.setGpuRenderer(renderer).collect { success ->
            if (success) {
                _currentGpuRenderer.value = renderer
                _showRebootConfirmationDialog.value = true
            } else {
                _rebootCommandFeedback.emit("Gagal mengatur GPU Renderer.")
            }
        }
    }

    fun confirmAndRebootDevice() {
        _showRebootConfirmationDialog.value = false
        viewModelScope.launch { repo.rebootDevice().collect { /* ignore, device reboot */ } }
    }

    fun cancelRebootConfirmation() {
        _showRebootConfirmationDialog.value = false
    }

    /* ---------------- RAM Control ---------------- */
    private fun fetchRamControlData() = viewModelScope.launch {
        launch(Dispatchers.IO) { repo.getZramEnabled().collect { _zramEnabled.value = it } }
        launch(Dispatchers.IO) { repo.getZramDisksize().collect { _zramDisksize.value = it } }
        launch(Dispatchers.IO) {
            repo.getCompressionAlgorithms().collect {
                _compressionAlgorithms.value = it
                repo.getCurrentCompression().firstOrNull()?.let { currentAlgo -> _currentCompression.value = currentAlgo }
            }
        }
        launch(Dispatchers.IO) { repo.getSwappiness().collect { _swappiness.value = it } }
        launch(Dispatchers.IO) { repo.getDirtyRatio().collect { _dirtyRatio.value = it } }
        launch(Dispatchers.IO) { repo.getDirtyBackgroundRatio().collect { _dirtyBackgroundRatio.value = it } }
        launch(Dispatchers.IO) { repo.getDirtyWriteback().collect { _dirtyWriteback.value = it } }
        launch(Dispatchers.IO) { repo.getDirtyExpireCentisecs().collect { _dirtyExpireCentisecs.value = it } }
        launch(Dispatchers.IO) { repo.getMinFreeMemory().collect { _minFreeMemory.value = it } }
        launch(Dispatchers.IO) { repo.getSwapSize().collect { _swapSize.value = it } }
    }

    fun setZramEnabled(enabled: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        repo.setZramEnabled(enabled).collect { _zramEnabled.value = it }
    }

    fun setZramDisksize(sizeBytes: Long) = viewModelScope.launch(Dispatchers.IO) {
        val max = repo.calculateMaxZramSize()
        if (sizeBytes < 512 * 1024 * 1024 || sizeBytes > max) {
            _rebootCommandFeedback.emit("Ukuran ZRAM tidak valid (512 MB - ${max / 1024 / 1024} MB)")
            return@launch
        }
        if (repo.setZramDisksize(sizeBytes)) {
            repo.getZramDisksize().take(1).collect { _zramDisksize.value = it }
        }
    }

    fun setCompression(algo: String) = viewModelScope.launch(Dispatchers.IO) {
        if (algo != _currentCompression.value) {
            if (repo.setCompressionAlgorithm(algo)) {
                repo.getCurrentCompression().take(1).collect { _currentCompression.value = it }
            }
        }
    }

    fun setSwappiness(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setSwappiness(value)) {
            repo.getSwappiness().take(1).collect { _swappiness.value = it }
        }
    }

    fun setDirtyRatio(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setDirtyRatio(value)) {
            repo.getDirtyRatio().take(1).collect { _dirtyRatio.value = it }
        }
    }

    fun setDirtyBackgroundRatio(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setDirtyBackgroundRatio(value)) {
            repo.getDirtyBackgroundRatio().take(1).collect { _dirtyBackgroundRatio.value = it }
        }
    }

    fun setDirtyWriteback(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setDirtyWriteback(value * 100)) {
            repo.getDirtyWriteback().take(1).collect { _dirtyWriteback.value = it }
        }
    }

    fun setDirtyExpireCentisecs(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setDirtyExpireCentisecs(value)) {
            repo.getDirtyExpireCentisecs().take(1).collect { _dirtyExpireCentisecs.value = it }
        }
    }

    fun setMinFreeMemory(value: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (repo.setMinFreeMemory(value * 1024)) {
            repo.getMinFreeMemory().take(1).collect { _minFreeMemory.value = it }
        }
    }

    fun setSwapSize(sizeBytes: Long) = viewModelScope.launch(Dispatchers.IO) {
        val maxSize = _maxSwapSize.value
        if (sizeBytes < 0 || sizeBytes > maxSize) {
            _rebootCommandFeedback.emit("Invalid swap size (0 - ${maxSize / 1024 / 1024 / 1024} GB)")
            return@launch
        }

        _isSwapLoading.value = true
        _swapLogs.value = emptyList() // Reset logs

        addSwapLog("🔄 Starting swap configuration...")
        delay(500)

        if (sizeBytes == 0L) {
            addSwapLog("🗑️ Disabling swap...")
            delay(300)
            addSwapLog("📋 Running: swapoff -a")
            delay(500)
        } else {
            val sizeMB = sizeBytes / 1024 / 1024
            addSwapLog("⚙️ Setting swap size to ${sizeMB}MB...")
            delay(300)
            addSwapLog("🗑️ Removing old swap file...")
            delay(400)
            addSwapLog("📋 Running: swapoff /data/swapfile")
            delay(300)
            addSwapLog("📋 Running: rm -f /data/swapfile")
            delay(500)
            addSwapLog("📝 Creating new swap file (${sizeMB}MB)...")
            delay(800)
            addSwapLog("📋 Running: dd if=/dev/zero of=/data/swapfile bs=1M count=${sizeMB}")
            delay(1000)
            addSwapLog("🔒 Setting file permissions...")
            delay(300)
            addSwapLog("📋 Running: chmod 600 /data/swapfile")
            delay(400)
            addSwapLog("🔧 Formatting as swap...")
            delay(500)
            addSwapLog("📋 Running: mkswap /data/swapfile")
            delay(600)
            addSwapLog("✅ Activating swap...")
            delay(400)
            addSwapLog("📋 Running: swapon /data/swapfile")
            delay(500)
        }

        if (repo.setSwapSize(sizeBytes)) {
            _swapSize.value = sizeBytes
            if (sizeBytes == 0L) {
                addSwapLog("✅ Swap successfully disabled!")
                _rebootCommandFeedback.emit("Swap successfully disabled")
            } else {
                addSwapLog("✅ Swap successfully set to ${sizeBytes / 1024 / 1024}MB!")
                _rebootCommandFeedback.emit("Swap size successfully set to ${sizeBytes / 1024 / 1024} MB")
            }
        } else {
            addSwapLog("❌ Failed to configure swap!")
            _rebootCommandFeedback.emit("Failed to set swap size")
        }

        delay(1000)
        _isSwapLoading.value = false
    }

    private fun addSwapLog(message: String) {
        val currentLogs = _swapLogs.value.toMutableList()
        currentLogs.add(message)
        _swapLogs.value = currentLogs
    }

    /* ---------------- Thermal ---------------- */
    private fun fetchCurrentThermalMode(isInitialLoad: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                thermalRepo.getCurrentThermalModeIndex()
                    .catch { e ->
                        Log.e("TuningVM_Thermal", "Error getting current thermal mode", e)
                        if (isInitialLoad) applyLastSavedThermalProfile()
                    }
                    .collect { index ->
                        _currentThermalModeIndex.value = index
                        if (isInitialLoad) applyLastSavedThermalProfile()
                    }
            } catch (e: Exception) {
                Log.e("TuningVM_Thermal", "Error in fetchCurrentThermalMode", e)
                if (isInitialLoad) {
                    applyLastSavedThermalProfile()
                }
            }
        }
    }

    private suspend fun applyLastSavedThermalProfile() {
        try {
            val idx = thermalPrefs.getInt(KEY_LAST_APPLIED_THERMAL_INDEX, -1)
            val profile = thermalRepo.availableThermalProfiles.find { it.index == idx }
            if (profile != null && _currentThermalModeIndex.value != idx) {
                setThermalProfileInternal(profile, isRestoring = true)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private suspend fun setThermalProfileInternal(profile: ThermalRepository.ThermalProfile, isRestoring: Boolean) {
        thermalRepo.setThermalModeIndex(profile.index).collect { ok ->
            if (ok) {
                _currentThermalModeIndex.value = profile.index
                if (!isRestoring) thermalPrefs.edit { putInt(KEY_LAST_APPLIED_THERMAL_INDEX, profile.index) }
                // For Dynamic mode (10), we need continuous monitoring
                // For other modes, persistent scripts handle reboot persistence
                if (profile.index == 10) {
                    // Only start service for Dynamic mode which requires CPU monitoring
                    val intent = Intent(application, ThermalService::class.java)
                    intent.putExtra("thermal_mode", profile.index)
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                            // For Android 15+, start foreground service with proper handling
                            ContextCompat.startForegroundService(application, intent)
                        } else {
                            application.startService(intent)
                        }
                        Log.d("TuningVM_Thermal", "Started ThermalService for Dynamic mode")
                    } catch (e: Exception) {
                        Log.e("TuningVM_Thermal", "Failed to start ThermalService", e)
                    }
                } else {
                    // For other modes, stop the service if running
                    stopThermalService()
                }
            } else {
                fetchCurrentThermalMode()
            }
        }
    }

    private fun stopThermalService() {
        val intent = Intent(application, ThermalService::class.java)
        application.stopService(intent)
        Log.d("TuningVM_Thermal", "Stopped ThermalService")
    }

    fun setThermalProfile(profile: ThermalRepository.ThermalProfile) =
        viewModelScope.launch { setThermalProfileInternal(profile, isRestoring = false) }
}