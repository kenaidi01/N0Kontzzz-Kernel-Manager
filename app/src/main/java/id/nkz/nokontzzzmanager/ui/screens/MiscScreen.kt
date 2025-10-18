package id.nkz.nokontzzzmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import id.nkz.nokontzzzmanager.ui.dialog.TcpCongestionDialog
import id.nkz.nokontzzzmanager.viewmodel.MiscViewModel
import androidx.compose.runtime.collectAsState
import id.nkz.nokontzzzmanager.ui.dialog.IoSchedulerDialog

import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import kotlinx.coroutines.launch

import androidx.compose.ui.res.stringResource
import id.nkz.nokontzzzmanager.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiscScreen(
    navController: NavController? = null,
    viewModel: MiscViewModel = hiltViewModel()
) {
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    // Listen for destination changes to reset scroll state
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.route == "misc") {
                coroutineScope.launch {
                    lazyListState.scrollToItem(0)
                }
            }
        }
        navController?.addOnDestinationChangedListener(listener)
        onDispose {
            navController?.removeOnDestinationChangedListener(listener)
        }
    }

    val kgslSkipZeroingEnabled by viewModel.kgslSkipZeroingEnabled.collectAsState()
    val isKgslFeatureAvailable by viewModel.isKgslFeatureAvailable.collectAsState()
    val bypassChargingEnabled by viewModel.bypassChargingEnabled.collectAsState()
    val isBypassChargingAvailable by viewModel.isBypassChargingAvailable.collectAsState()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Group 1: GPU & Power
        item {
            Text(stringResource(id = R.string.gpu_power), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        // KGSL Skip Pool Zeroing feature
        item {
            KgslSkipZeroingCard(
                kgslSkipZeroingEnabled = kgslSkipZeroingEnabled,
                isKgslFeatureAvailable = isKgslFeatureAvailable,
                onToggleKgslSkipZeroing = { enabled ->
                    viewModel.toggleKgslSkipZeroing(enabled)
                }
            )
        }

        // Bypass Charging feature
        item {
            BypassChargingCard(
                bypassChargingEnabled = bypassChargingEnabled,
                isBypassChargingAvailable = isBypassChargingAvailable,
                onToggleBypassCharging = { enabled ->
                    viewModel.toggleBypassCharging(enabled)
                }
            )
        }

        // Spacer between groups
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Group 2: Network & Storage
        item {
            Text(stringResource(id = R.string.network_storage), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // TCP Congestion Control Algorithm feature
        item {
            TcpCongestionControlCard(
                tcpCongestionAlgorithm = viewModel.tcpCongestionAlgorithm.collectAsState().value,
                availableAlgorithms = viewModel.availableTcpCongestionAlgorithms.collectAsState().value,
                onAlgorithmChange = { algorithm ->
                    viewModel.updateTcpCongestionAlgorithm(algorithm)
                }
            )
        }
        
        // I/O Scheduler feature
        item {
            IoSchedulerCard(
                ioScheduler = viewModel.ioScheduler.collectAsState().value,
                availableSchedulers = viewModel.availableIoSchedulers.collectAsState().value,
                onSchedulerChange = { scheduler ->
                    viewModel.updateIoScheduler(scheduler)
                }
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KgslSkipZeroingCard(
    kgslSkipZeroingEnabled: Boolean,
    isKgslFeatureAvailable: Boolean?,
    onToggleKgslSkipZeroing: (Boolean) -> Unit
) {
    // Treat null as false for UI purposes, preventing flicker during initial load
    val featureAvailable = isKgslFeatureAvailable == true

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = {
            if (featureAvailable) {
                onToggleKgslSkipZeroing(!kgslSkipZeroingEnabled)
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.kgsl_skip_pool_zeroing),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (featureAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )
                    Text(
                        text = if (featureAvailable) {
                            stringResource(id = R.string.kgsl_skip_pool_zeroing_desc)
                        } else {
                            stringResource(id = R.string.feature_not_available)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (featureAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )
                }

                Switch(
                    checked = kgslSkipZeroingEnabled && featureAvailable,
                    onCheckedChange = null,
                    enabled = featureAvailable,
                    thumbContent = if (kgslSkipZeroingEnabled && featureAvailable) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    }
                )
            }

            if (kgslSkipZeroingEnabled && featureAvailable) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(id = R.string.performance_mode_active),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.kgsl_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TcpCongestionControlCard(
    tcpCongestionAlgorithm: String?,
    availableAlgorithms: List<String>,
    onAlgorithmChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = { 
            if (availableAlgorithms.isNotEmpty()) {
                showDialog = true 
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.tcp_congestion_control),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = tcpCongestionAlgorithm ?: "...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(id = R.string.change_algorithm),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    if (showDialog) {
        TcpCongestionDialog(
            currentAlgorithm = tcpCongestionAlgorithm ?: "",
            availableAlgorithms = availableAlgorithms,
            onAlgorithmSelected = { algorithm ->
                onAlgorithmChange(algorithm)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun IoSchedulerCard(
    ioScheduler: String?,
    availableSchedulers: List<String>,
    onSchedulerChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = { 
            if (availableSchedulers.isNotEmpty()) {
                showDialog = true 
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.io_scheduler),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = ioScheduler ?: "...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(id = R.string.change_scheduler),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    if (showDialog) {
        IoSchedulerDialog(
            currentScheduler = ioScheduler ?: "",
            availableSchedulers = availableSchedulers,
            onSchedulerSelected = { scheduler ->
                onSchedulerChange(scheduler)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BypassChargingCard(
    bypassChargingEnabled: Boolean,
    isBypassChargingAvailable: Boolean?,
    onToggleBypassCharging: (Boolean) -> Unit
) {
    // Treat null as false for UI purposes, preventing flicker during initial load
    val featureAvailable = isBypassChargingAvailable == true

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = {
            if (featureAvailable) {
                onToggleBypassCharging(!bypassChargingEnabled)
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.bypass_charging),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (featureAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )
                    Text(
                        text = if (featureAvailable) {
                            stringResource(id = R.string.bypass_charging_desc)
                        } else {
                            stringResource(id = R.string.feature_not_available)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (featureAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )
                }

                Switch(
                    checked = bypassChargingEnabled && featureAvailable,
                    onCheckedChange = null,
                    enabled = featureAvailable,
                    thumbContent = if (bypassChargingEnabled && featureAvailable) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    }
                )
            }

            if (bypassChargingEnabled && featureAvailable) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(id = R.string.bypass_charging_activated),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.bypass_charging_active_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
