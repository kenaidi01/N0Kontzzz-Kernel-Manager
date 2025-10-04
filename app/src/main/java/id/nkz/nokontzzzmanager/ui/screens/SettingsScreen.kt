package id.nkz.nokontzzzmanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import id.nkz.nokontzzzmanager.ui.viewmodel.SettingsViewModel
import id.nkz.nokontzzzmanager.ui.theme.ThemeMode
import id.nkz.nokontzzzmanager.R
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    val currentThemeMode by viewModel.currentThemeMode.collectAsState()
    val context = LocalContext.current
    
    // Tambahkan state untuk memicu rekomposisi saat tema berubah
    var themeRefreshKey by remember { mutableIntStateOf(0) }
    
    // Perbarui key saat currentThemeMode berubah untuk memicu rekomposisi
    LaunchedEffect(currentThemeMode) {
        themeRefreshKey++ // Ini akan memicu rekomposisi komponen
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        // Settings section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Language Setting
            Text(
                text = "Language",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            )
            
            // Language Setting Item - Single item in its group
            SettingItemCard(
                headlineText = stringResource(R.string.language),
                supportingText = currentLanguage.displayName,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null
                    )
                },
                shape = getRoundedCornerShape(0, 1), // First (and only) item in group of 1
                onClick = {
                    Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp)) // Add some spacing between groups
            
            // Theme and Display Settings
            Text(
                text = "Theme & Display",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            )
            
            // Theme Setting Item - First item (index 0) in group of 2
            SettingItemCard(
                headlineText = stringResource(R.string.theme),
                supportingText = when (currentThemeMode) {
                    ThemeMode.SYSTEM_DEFAULT -> stringResource(R.string.theme_system)
                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Contrast,
                        contentDescription = null
                    )
                },
                shape = getRoundedCornerShape(0, 2), // First item in group of 2
                onClick = { showThemeDialog = true }
            )

            val isDarkTheme = when (currentThemeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
            }
            val isAmoledMode by viewModel.isAmoledMode.collectAsState()

            // AMOLED Mode Setting Item - Last item (index 1) in group of 2
            SettingItemCard(
                headlineText = "AMOLED Mode",
                supportingText = "Use a pure black background in dark mode",
                icon = {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null
                    )
                },
                trailingContent = {
                    Switch(
                        checked = isAmoledMode,
                        onCheckedChange = { viewModel.setAmoledMode(it) },
                        enabled = isDarkTheme,
                        thumbContent = if (isAmoledMode) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        } else {
                            {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        }
                    )
                },
                shape = getRoundedCornerShape(1, 2), // Second (last) item in group of 2
                onClick = { 
                    if (isDarkTheme) {
                        viewModel.setAmoledMode(!isAmoledMode) 
                    }
                }
            )
        }

        // Version Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val versionName = try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName
            } catch (e: Exception) {
                "N/A"
            }
            Text(
                text = "Version $versionName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { 
                Text(
                    text = stringResource(R.string.select_theme),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            confirmButton = { 
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { themeMode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(themeMode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (themeMode) {
                                    ThemeMode.SYSTEM_DEFAULT -> stringResource(R.string.theme_system)
                                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            RadioButton(
                                selected = currentThemeMode == themeMode,
                                onClick = {
                                    viewModel.setThemeMode(themeMode)
                                    showThemeDialog = false
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun SettingItemCard(
    headlineText: String,
    supportingText: String,
    icon: @Composable () -> Unit,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
            ) {
                icon()
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (trailingContent != {}) {
                trailingContent()
            }
        }
    }
}

private fun getRoundedCornerShape(index: Int, totalItems: Int): RoundedCornerShape {
    return when (totalItems) {
        1 -> RoundedCornerShape(24.dp) // If only one card, all corners 12dp
        2 -> {
            when (index) {
                0 -> RoundedCornerShape( // First card: 12dp top, 4dp bottom
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = 8.dp,
                    bottomEnd = 8.dp
                )
                1 -> RoundedCornerShape( // Second card: 4dp top, 12dp bottom
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp
                )
                else -> RoundedCornerShape(24.dp) // Default case
            }
        }
        else -> {
            // For groups with more than 2 items
            when (index) {
                0 -> RoundedCornerShape( // First card: 12dp top, 4dp bottom
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = 8.dp,
                    bottomEnd = 8.dp
                )
                totalItems - 1 -> RoundedCornerShape( // Last card: 4dp top, 12dp bottom
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp
                )
                else -> RoundedCornerShape(8.dp) // Middle cards: 4dp all sides
            }
        }
    }
}
