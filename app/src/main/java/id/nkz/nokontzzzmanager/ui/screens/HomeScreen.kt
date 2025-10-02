package id.nkz.nokontzzzmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import id.nkz.nokontzzzmanager.ui.components.AboutCard
import id.nkz.nokontzzzmanager.ui.components.CpuCard
import id.nkz.nokontzzzmanager.ui.components.GpuCard
import id.nkz.nokontzzzmanager.ui.components.IndeterminateExpressiveLoadingIndicator
import id.nkz.nokontzzzmanager.ui.components.KernelCard
import id.nkz.nokontzzzmanager.ui.components.MergedSystemCard
import id.nkz.nokontzzzmanager.ui.viewmodel.StorageInfoViewModel
import id.nkz.nokontzzzmanager.viewmodel.GraphDataViewModel
import id.nkz.nokontzzzmanager.viewmodel.HomeViewModel
import kotlin.text.isNotBlank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val vm: HomeViewModel = hiltViewModel()
    val storageViewModel: StorageInfoViewModel = hiltViewModel()
    val activity = LocalContext.current as ComponentActivity
    val graphDataViewModel: GraphDataViewModel = viewModel(viewModelStoreOwner = activity)

    // Kumpulkan semua state dari ViewModel
    val cpuInfo by vm.cpuInfo.collectAsState()
    val gpuInfo by vm.gpuInfo.collectAsState()
    val batteryInfo by vm.batteryInfo.collectAsState()
    val memoryInfo by vm.memoryInfo.collectAsState()
    val deepSleepInfo by vm.deepSleep.collectAsState()
    val rootStatus by vm.rootStatus.collectAsState()
    val kernelInfo by vm.kernelInfo.collectAsState()
    val appVersion by vm.appVersion.collectAsState()
    val systemInfoState by vm.systemInfo.collectAsState()
    val cpuClusters by vm.cpuClusters.collectAsState()
    val storageInfo by storageViewModel.storageInfo.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    var showFabMenu by remember { mutableStateOf(false) }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            IndeterminateExpressiveLoadingIndicator()
        }
    } else {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            /* 1. CPU */
            val currentSystemInfo = systemInfoState
            val socNameToDisplay = currentSystemInfo?.soc?.takeIf { it.isNotBlank() && it != "Unknown" } ?: cpuInfo.soc.takeIf { it.isNotBlank() && it != "Unknown SoC" && it != "N/A" } ?: "CPU"
            CpuCard(socNameToDisplay, cpuInfo, cpuClusters, false, Modifier, graphDataViewModel)

            /* 2. GPU */
            GpuCard(gpuInfo, Modifier, graphDataViewModel)

            /* 3. Merged card */
            val currentBattery = batteryInfo
            val currentMemory = memoryInfo
            val currentDeepSleep = deepSleepInfo
            val currentRoot = rootStatus
            val currentVersion = appVersion
            val currentSystem = systemInfoState

            if (currentBattery != null && currentMemory != null && currentDeepSleep != null &&
                currentRoot != null && currentVersion != null && currentSystem != null) {
                MergedSystemCard(
                    b = currentBattery,
                    d = currentDeepSleep,
                    rooted = currentRoot,
                    version = currentVersion,
                    mem = currentMemory,
                    systemInfo = currentSystem,
                    storageInfo = storageInfo,
                    modifier = Modifier
                )
            } else {
                Box(Modifier.fillMaxWidth().height(200.dp).background(Color.LightGray.copy(alpha = 0.5f))) {
                    IndeterminateExpressiveLoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            /* 4. Kernel */
            val currentKernel = kernelInfo
            if (currentKernel != null) {
                KernelCard(currentKernel, Modifier)
            } else {
                // Opsional: Placeholder untuk KernelCard
            }

            /* 5. About */
            AboutCard(false, Modifier)
        }
    }
}