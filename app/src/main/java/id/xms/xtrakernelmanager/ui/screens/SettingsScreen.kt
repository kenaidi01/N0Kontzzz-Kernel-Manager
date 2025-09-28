package id.xms.xtrakernelmanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.viewmodel.SettingsViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import id.xms.xtrakernelmanager.util.Language
import id.xms.xtrakernelmanager.ui.theme.ThemeMode
import androidx.compose.ui.graphics.lerp
import androidx.compose.material3.surfaceColorAtElevation
import android.widget.Toast

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
    
    val systemUiController = rememberSystemUiController()
    
    // Gunakan warna langsung dari color scheme untuk responsivitas lebih cepat
    val colorScheme = MaterialTheme.colorScheme
    val topBarContainerColor = colorScheme.surface
    
    // Tentukan tema gelap atau terang berdasarkan tema aplikasi, bukan sistem
    val isDarkTheme = when (currentThemeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }

    // Atur ikon status bar sesuai dengan tema aplikasi
    LaunchedEffect(isDarkTheme, colorScheme, themeRefreshKey) {
        systemUiController.setStatusBarColor(
            color = topBarContainerColor,
            darkIcons = !isDarkTheme  // Ikon gelap jika bukan tema gelap (untuk visibilitas)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Settings section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.preferences),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            ListItem(
                headlineContent = { 
                    Text(
                        text = stringResource(R.string.language),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                supportingContent = { 
                    Text(
                        text = currentLanguage.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show()
                    }
            )
            
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
            
            ListItem(
                headlineContent = { 
                    Text(
                        text = stringResource(R.string.theme),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                supportingContent = { 
                    Text(
                        text = when (currentThemeMode) {
                            ThemeMode.SYSTEM_DEFAULT -> stringResource(R.string.theme_system)
                            ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                            ThemeMode.DARK -> stringResource(R.string.theme_dark)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Contrast,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemeDialog = true }
            )

            val isDarkTheme = when (currentThemeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
            }
            val isAmoledMode by viewModel.isAmoledMode.collectAsState()

            ListItem(
                headlineContent = { 
                    Text(
                        text = "AMOLED Mode",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                supportingContent = { 
                    Text(
                        text = "Use a pure black background in dark mode",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingContent = {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isDarkTheme) { 
                        viewModel.setAmoledMode(!isAmoledMode) 
                    }
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
                    ThemeMode.values().forEach { themeMode ->
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
