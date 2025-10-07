package id.nkz.nokontzzzmanager.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
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
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank

// Helper function to safely find an Activity from a Context.
private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val vm: HomeViewModel = hiltViewModel()
    val storageViewModel: StorageInfoViewModel = hiltViewModel()
    
    // Safely get the activity context and create the activity-scoped ViewModel.
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val graphDataViewModel: GraphDataViewModel = if (activity != null) {
        viewModel(viewModelStoreOwner = activity as ViewModelStoreOwner)
    } else {
        // Fallback if activity is not available, though unlikely in this context.
        viewModel()
    }

    // Trigger the one-time data load after a short delay to allow animations to finish
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(150)
        vm.loadInitialData()
    }

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

    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Listen for destination changes to reset scroll state
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.route == "home") {
                coroutineScope.launch {
                    lazyListState.scrollToItem(0)
                }
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    // Notify the ViewModel about the scroll state to pause data updates during scroll
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.isScrollInProgress }
            .collect { isScrolling ->
                vm.setScrolling(isScrolling)
            }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + androidx.compose.animation.scaleIn(initialScale = 0.92f, animationSpec = androidx.compose.animation.core.tween(300)),
        exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 100.dp), // Adjust padding to better center the indicator
                        contentAlignment = Alignment.Center
                    ) {
                        IndeterminateExpressiveLoadingIndicator()
                    }
                }
            } else {
                /* 1. CPU */
                item {
                    val currentSystemInfo = systemInfoState
                    val clusters = cpuClusters
                    if (clusters != null) {
                        val socNameToDisplay = currentSystemInfo?.soc?.takeIf { it.isNotBlank() && it != "Unknown" } ?: cpuInfo.soc.takeIf { it.isNotBlank() && it != "Unknown SoC" && it != "N/A" } ?: "CPU"
                        CpuCard(socNameToDisplay, cpuInfo, clusters, false, Modifier, graphDataViewModel)
                    } else {
                        // Show a smaller placeholder if just this data is missing
                        Card(modifier = Modifier.fillMaxWidth().height(150.dp)) { /* Placeholder */ }
                    }
                }

                /* 2. GPU */
                item {
                    GpuCard(gpuInfo, Modifier, graphDataViewModel)
                }

                /* 3. Merged card */
                item {
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
                        // Placeholder for the merged card while data is loading
                        Card(modifier = Modifier.fillMaxWidth().height(200.dp)) { /* Placeholder */ }
                    }
                }

                /* 4. Kernel */
                item {
                    val currentKernel = kernelInfo
                    if (currentKernel != null) {
                        KernelCard(currentKernel, Modifier)
                    } else {
                        // Optional: Placeholder for KernelCard while data is loading
                        Card(modifier = Modifier.fillMaxWidth().height(100.dp)) { /* Placeholder */ }
                    }
                }

                /* 5. About */
                item {
                    AboutCard(false, Modifier)
                }
            }
        }
    }
}