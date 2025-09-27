package id.xms.xtrakernelmanager.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.loadingindicator.LoadingIndicator
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.data.model.SystemInfo
import id.xms.xtrakernelmanager.ui.components.*
import id.xms.xtrakernelmanager.ui.components.ExpressiveLoadingIndicator
import id.xms.xtrakernelmanager.viewmodel.GraphDataViewModel
import id.xms.xtrakernelmanager.viewmodel.HomeViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.text.isNotBlank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val vm: HomeViewModel = hiltViewModel()
    val storageViewModel: id.xms.xtrakernelmanager.ui.viewmodel.StorageInfoViewModel = hiltViewModel()
    val graphDataViewModel: id.xms.xtrakernelmanager.viewmodel.GraphDataViewModel = viewModel()

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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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