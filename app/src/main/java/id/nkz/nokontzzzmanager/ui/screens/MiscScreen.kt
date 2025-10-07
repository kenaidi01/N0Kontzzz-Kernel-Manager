package id.nkz.nokontzzzmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiscScreen(
    navController: NavController? = null,
    viewModel: MiscViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    val kgslSkipZeroingEnabled by viewModel.kgslSkipZeroingEnabled.collectAsState()
    val isKgslFeatureAvailable by viewModel.isKgslFeatureAvailable.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
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
        shape = RoundedCornerShape(24.dp, 24.dp, 8.dp, 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
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
                        text = "KGSL Skip Pool Zeroing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (featureAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )
                    Text(
                        text = if (featureAvailable) {
                            "Improve FPS in emulators, Unity & Unreal games. May cause UI glitches."
                        } else {
                            "Feature not available in your kernel version."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (featureAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )
                }

                Switch(
                    checked = kgslSkipZeroingEnabled && featureAvailable,
                    onCheckedChange = { if (featureAvailable) onToggleKgslSkipZeroing(!kgslSkipZeroingEnabled) },
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
                                text = "Performance Mode Active",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "This feature may cause UI glitches or visual artifacts. If you experience issues, disable this feature.",
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
        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
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
                        text = "TCP Congestion Control",
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
                    contentDescription = "Change algorithm",
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
                // Close the dialog after selection
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
        shape = RoundedCornerShape(8.dp, 8.dp, 24.dp, 24.dp),
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
                        text = "I/O Scheduler",
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
                    contentDescription = "Change scheduler",
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
            },
            onDismiss = { showDialog = false }
        )
    }
}
