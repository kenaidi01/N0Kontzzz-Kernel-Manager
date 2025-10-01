package id.nkz.nokontzzzmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import id.nkz.nokontzzzmanager.R
import id.nkz.nokontzzzmanager.viewmodel.TuningViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun GpuControlCard(
    tuningViewModel: TuningViewModel = hiltViewModel(),
    blur: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()

    // State variables
    var isExpanded by remember { mutableStateOf(false) }
    var showGovernorDialog by remember { mutableStateOf(false) }
    var showRendererDialog by remember { mutableStateOf(false) }
    var showMinFreqDialog by remember { mutableStateOf(false) }
    var showMaxFreqDialog by remember { mutableStateOf(false) }

    // Collect GPU states from ViewModel
    val gpuGovernor by tuningViewModel.currentGpuGovernor.collectAsState()
    val availableGovernors by tuningViewModel.availableGpuGovernors.collectAsState()
    val gpuMinFreq by tuningViewModel.currentGpuMinFreq.collectAsState()
    val gpuMaxFreq by tuningViewModel.currentGpuMaxFreq.collectAsState()
    val availableGpuFrequencies by tuningViewModel.availableGpuFrequencies.collectAsState()
    val gpuPowerLevelRange by tuningViewModel.gpuPowerLevelRange.collectAsState()
    val currentGpuPowerLevel by tuningViewModel.currentGpuPowerLevel.collectAsState()
    val openGlesDriver by tuningViewModel.currentOpenGlesDriver.collectAsState()
    val vulkanVersion by tuningViewModel.vulkanApiVersion.collectAsState()
    val currentRenderer by tuningViewModel.currentGpuRenderer.collectAsState()
    val availableRenderers = tuningViewModel.availableGpuRenderers
    
    // State for power level that updates during dragging but only applies when released
    var tempPowerLevel by remember { mutableFloatStateOf(currentGpuPowerLevel) }
    
    // Update tempPowerLevel when currentGpuPowerLevel changes externally
    LaunchedEffect(currentGpuPowerLevel) {
        tempPowerLevel = currentGpuPowerLevel
    }

    // No frequency ranges needed since we're using dialogs instead of sliders

    // Load GPU data when component is first composed
    LaunchedEffect(Unit) {
        tuningViewModel.fetchGpuData()
        tuningViewModel.fetchOpenGlesDriver()
        tuningViewModel.fetchVulkanApiVersion()
        tuningViewModel.fetchCurrentGpuRenderer()
    }

    // Animation values - Simplified MD3 animation
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.gpu_card),
                        contentDescription = "GPU Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Column {
                        Text(
                            text = "GPU Control",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Configure GPU governor, frequency, and renderer settings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Expanded Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))

                    // GPU Governor Control
                    GPUControlSection(
                        title = "GPU Governor",
                        description = "Controls GPU frequency scaling behavior",
                        icon = Icons.Default.Tune
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showGovernorDialog = true },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = getRoundedCornerShape(0, 1) // Only one item
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Current Governor",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = gpuGovernor.takeIf { it != "..." && it.isNotBlank() } ?: "Unknown",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Change Governor",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // GPU Frequency Control
                    GPUControlSection(
                        title = "GPU Frequency",
                        description = "Minimum and maximum GPU frequencies",
                        icon = Icons.Default.Speed
                    ) {
                        // Cards for GPU frequency control with dialogs
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp) // Changed from 12.dp to 2.dp as per your other requirements
                        ) {
                            // Min Frequency Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showMinFreqDialog = true },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = getRoundedCornerShape(0, 2) // First card in group of 2
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Minimum Frequency",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "$gpuMinFreq MHz",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Change Min Frequency",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Max Frequency Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showMaxFreqDialog = true },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = getRoundedCornerShape(1, 2) // Second card in group of 2
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Maximum Frequency",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "$gpuMaxFreq MHz",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Change Max Frequency",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // GPU Renderer Control
                    GPUControlSection(
                        title = "GPU Renderer",
                        description = "Select graphics rendering backend",
                        icon = Icons.Default.Monitor
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRendererDialog = true },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = getRoundedCornerShape(0, 1) // Only one item
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Current Renderer",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = currentRenderer.takeIf { it != "Loading..." && it.isNotBlank() } ?: "Default",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Change Renderer",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // GPU Power Level and Throttling Control
                    GPUControlSection(
                        title = "GPU Power Level",
                        description = "Adjust GPU power and throttling settings",
                        icon = Icons.Default.VideogameAsset
                    ) {
                        // Determine min and max values for GPU power level at this scope
                        val minPowerLevel = minOf(gpuPowerLevelRange.first, gpuPowerLevelRange.second)
                        val maxPowerLevel = maxOf(gpuPowerLevelRange.first, gpuPowerLevelRange.second)
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp) // Changed from 12.dp to 2.dp as per your other requirements
                        ) {
                            // GPU Power Level Card
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = getRoundedCornerShape(0, 2) // First card in group of 2
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Power Level",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = tempPowerLevel.toInt().toString(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Slider(
                                        value = tempPowerLevel,
                                        onValueChange = { newValue ->
                                            // Update the temporary value during dragging for smooth UI
                                            tempPowerLevel = newValue
                                        },
                                        onValueChangeFinished = {
                                            // Only apply the change when user stops dragging
                                            tuningViewModel.setGpuPowerLevel(tempPowerLevel)
                                        },
                                        valueRange = minPowerLevel..maxPowerLevel,
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        steps = if (abs(maxPowerLevel - minPowerLevel) > 1f) {
                                            abs(maxPowerLevel - minPowerLevel).toInt() - 1
                                        } else {
                                            0 // No steps if the range is too small
                                        }
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = minPowerLevel.toInt().toString(),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = maxPowerLevel.toInt().toString(),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // GPU Throttling Switch Card
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = getRoundedCornerShape(1, 2) // Second card in group of 2
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "GPU Throttling",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Reduce performance to save power",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = tuningViewModel.gpuThrottlingEnabled.collectAsState().value,
                                        onCheckedChange = { checked ->
                                            tuningViewModel.toggleGpuThrottling(checked)
                                        },
                                        thumbContent = if (tuningViewModel.gpuThrottlingEnabled.collectAsState().value) {
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
                            }
                        }
                    }
                }
            }
        }
    }

    // Governor Selection Dialog
    if (showGovernorDialog) {
        AlertDialog(
            onDismissRequest = { showGovernorDialog = true },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select GPU Governor")
                }
            },
            text = {
                if (availableGovernors.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading governors...")
                    }
                } else {
                    LazyColumn {
                        items(availableGovernors) { governor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = governor == gpuGovernor,
                                        onClick = {
                                            coroutineScope.launch {
                                                tuningViewModel.setGpuGovernor(governor)
                                            }
                                            showGovernorDialog = false
                                        }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = governor == gpuGovernor,
                                    onClick = {
                                        coroutineScope.launch {
                                            tuningViewModel.setGpuGovernor(governor)
                                        }
                                        showGovernorDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = governor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGovernorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Renderer Selection Dialog
    if (showRendererDialog) {
        AlertDialog(
            onDismissRequest = { showRendererDialog = true },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select GPU Renderer")
                }
            },
            text = {
                LazyColumn {
                    items(availableRenderers) { renderer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = renderer == currentRenderer,
                                    onClick = {
                                        coroutineScope.launch {
                                            tuningViewModel.userSelectedGpuRenderer(renderer)
                                        }
                                        showRendererDialog = false
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = renderer == currentRenderer,
                                onClick = {
                                    coroutineScope.launch {
                                        tuningViewModel.userSelectedGpuRenderer(renderer)
                                    }
                                    showRendererDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = renderer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                when (renderer) {
                                    "OpenGL" -> Text(
                                        text = "Traditional rendering",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    "Vulkan" -> Text(
                                        text = "Modern low-overhead API",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    "ANGLE" -> Text(
                                        text = "OpenGL ES on Direct3D",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    "Default" -> Text(
                                        text = "System default",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRendererDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Min Frequency Selection Dialog
    if (showMinFreqDialog) {
        AlertDialog(
            onDismissRequest = { showMinFreqDialog = true },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select Minimum GPU Frequency")
                }
            },
            text = {
                if (availableGpuFrequencies.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading frequencies...")
                    }
                } else {
                    LazyColumn {
                        items(availableGpuFrequencies.sorted()) { frequency ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = frequency == gpuMinFreq,
                                        onClick = {
                                            coroutineScope.launch {
                                                tuningViewModel.setGpuMinFrequency(frequency)
                                            }
                                            showMinFreqDialog = false
                                        }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = frequency == gpuMinFreq,
                                    onClick = {
                                        coroutineScope.launch {
                                            tuningViewModel.setGpuMinFrequency(frequency)
                                        }
                                        showMinFreqDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$frequency MHz",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMinFreqDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Max Frequency Selection Dialog
    if (showMaxFreqDialog) {
        AlertDialog(
            onDismissRequest = { showMaxFreqDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select Maximum GPU Frequency")
                }
            },
            text = {
                if (availableGpuFrequencies.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading frequencies...")
                    }
                } else {
                    LazyColumn {
                        items(availableGpuFrequencies.sorted()) { frequency ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = frequency == gpuMaxFreq,
                                        onClick = {
                                            coroutineScope.launch {
                                                tuningViewModel.setGpuMaxFrequency(frequency)
                                            }
                                            showMaxFreqDialog = false
                                        }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = frequency == gpuMaxFreq,
                                    onClick = {
                                        coroutineScope.launch {
                                            tuningViewModel.setGpuMaxFrequency(frequency)
                                        }
                                        showMaxFreqDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$frequency MHz",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMaxFreqDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}



@Composable
private fun GPUControlSection(
    title: String,
    description: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

private fun getRoundedCornerShape(index: Int, totalItems: Int): RoundedCornerShape {
    return when (totalItems) {
        1 -> RoundedCornerShape(12.dp) // If only one card, all corners 12dp
        2 -> {
            when (index) {
                0 -> RoundedCornerShape( // First card: 12dp top, 4dp bottom
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 4.dp
                )
                1 -> RoundedCornerShape( // Second card: 4dp top, 12dp bottom
                    topStart = 4.dp,
                    topEnd = 4.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 12.dp
                )
                else -> RoundedCornerShape(12.dp) // Default case
            }
        }
        else -> RoundedCornerShape(12.dp) // Default for other cases
    }
}
