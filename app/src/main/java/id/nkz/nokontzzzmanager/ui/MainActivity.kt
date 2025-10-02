package id.nkz.nokontzzzmanager.ui

import TuningScreen
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import id.nkz.nokontzzzmanager.data.repository.RootRepository
import id.nkz.nokontzzzmanager.data.repository.ThermalRepository
import id.nkz.nokontzzzmanager.service.ThermalService
import id.nkz.nokontzzzmanager.ui.components.BottomNavBar

import id.nkz.nokontzzzmanager.ui.components.KernelVerificationDialog
import id.nkz.nokontzzzmanager.ui.components.RootRequiredDialog
import id.nkz.nokontzzzmanager.ui.components.CustomSystemBarStyle
import id.nkz.nokontzzzmanager.ui.dialog.BatteryOptDialog
import id.nkz.nokontzzzmanager.ui.screens.*
import id.nkz.nokontzzzmanager.ui.theme.RvKernelManagerTheme
import id.nkz.nokontzzzmanager.util.ThemeManager
import id.nkz.nokontzzzmanager.util.BatteryOptimizationChecker
import id.nkz.nokontzzzmanager.ui.components.UnifiedTopAppBar
import id.nkz.nokontzzzmanager.util.LanguageManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import id.nkz.nokontzzzmanager.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import java.io.BufferedReader
import java.io.InputStreamReader

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootRepo: RootRepository

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Inject
    lateinit var thermalRepository: ThermalRepository

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var themeManager: ThemeManager

    private lateinit var batteryOptChecker: BatteryOptimizationChecker
    private var showBatteryOptDialog by mutableStateOf(false)
    private var showRootRequiredDialog by mutableStateOf(false)
    private var showKernelVerificationDialog by mutableStateOf(false)
    private var permissionDenialCount by mutableIntStateOf(0)
    private val MAX_PERMISSION_RETRIES = 2

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize batteryOptChecker regardless of root status for consistency
        batteryOptChecker = BatteryOptimizationChecker(this)
        
        // Check root status and update UI accordingly
        // This check will be re-evaluated in onResume when user grants root access
        if (!rootRepo.checkRootFresh()) {
            showRootRequiredDialog = true
        } else {
            showRootRequiredDialog = false // Hide root dialog if root is available
            if (!isKernelSupported()) {
                showKernelVerificationDialog = true
            } else {
                // Only check permissions if device is rooted and kernel is supported
                checkAndHandlePermissions()
            }
        }

        // Observe language changes
        lifecycleScope.launch {
            languageManager.currentLanguage.collect()
        }

        setContent {
            RvKernelManagerTheme(themeManager = themeManager) {
                val navController = rememberNavController()
                val items = listOf("Home", "Tuning", "Misc")
                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                var showFabMenu by remember { mutableStateOf(false) }
                val isAmoledMode by themeManager.isAmoledMode.collectAsState(initial = false)

                val currentRoute = currentDestination?.route

                val title = when (currentRoute) {
                    "settings" -> "Settings"
                    else -> stringResource(id = R.string.n0kz_kernel_manager) // Default title for home, tuning, misc
                }

                val showSettingsIcon = when (currentRoute) {
                    "home", "tuning", "misc" -> true
                    else -> false // Do not show for settings or other screens
                }

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        UnifiedTopAppBar(
                            title = title,
                            navController = navController,
                            showSettingsIcon = showSettingsIcon,
                            scrollBehavior = scrollBehavior,
                            isAmoledMode = isAmoledMode
                        )
                    },
                    floatingActionButton = {
                        if (currentRoute == "home") {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val fabMenuItems = listOf(
                                    "power_off" to R.string.power_off,
                                    "reboot_recovery" to R.string.reboot_recovery,
                                    "reboot_bootloader" to R.string.reboot_bootloader,
                                    "reboot_system" to R.string.reboot_system
                                )

                                val transition = updateTransition(targetState = showFabMenu, label = "fab_transition")

                                fabMenuItems.asReversed().forEachIndexed { index, (action, textRes) ->
                                    val alpha by transition.animateFloat(
                                        label = "alpha_anim",
                                        transitionSpec = {
                                            tween(
                                                durationMillis = 200,
                                                delayMillis = if (targetState) (fabMenuItems.size - 1 - index) * 50 else index * 30
                                            )
                                        }
                                    ) { if (it) 1f else 0f }

                                    val translationY by transition.animateFloat(
                                        label = "translationY_anim",
                                        transitionSpec = {
                                            tween(
                                                durationMillis = 300,
                                                delayMillis = if (targetState) (fabMenuItems.size - 1 - index) * 30 else index * 30
                                            )
                                        }
                                    ) { if (it) 0f else 40f * (fabMenuItems.size - index) }


                                    Box(
                                        modifier = Modifier
                                            .graphicsLayer {
                                                this.alpha = alpha
                                                this.translationY = translationY
                                            }
                                    ) {
                                        val icon = when (action) {
                                            "power_off" -> Icons.Filled.PowerSettingsNew
                                            "reboot_recovery" -> Icons.Filled.SettingsBackupRestore
                                            "reboot_bootloader" -> Icons.Filled.Build
                                            else -> Icons.Filled.Refresh
                                        }
                                        val command = when (action) {
                                            "power_off" -> "reboot -p"
                                            "reboot_recovery" -> "reboot recovery"
                                            "reboot_bootloader" -> "reboot bootloader"
                                            "reboot_system" -> "reboot"
                                            else -> ""
                                        }
                                        ExtendedFloatingActionButton(
                                            text = { Text(stringResource(textRes)) },
                                            icon = { Icon(icon, contentDescription = null) },
                                            onClick = {
                                                if (command.isNotEmpty()) {
                                                    Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                                                }
                                            },
                                            containerColor = if (action == "power_off") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = if (action == "power_off") MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()
                                val fabScale by animateFloatAsState(
                                    targetValue = if (isPressed) 0.9f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = 0.5f,
                                        stiffness = 1500f
                                    ),
                                    label = "fabScale"
                                )
                                val fabShape by animateDpAsState(
                                    targetValue = if (showFabMenu) 28.dp else 16.dp,
                                    animationSpec = spring(
                                        dampingRatio = 0.5f,
                                        stiffness = 1500f
                                    ),
                                    label = "fabShape"
                                )
                                val fabContainerColor by animateColorAsState(
                                    targetValue = if (showFabMenu) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                    animationSpec = tween(durationMillis = 300),
                                    label = "fabContainerColor"
                                )
                                val fabContentColor by animateColorAsState(
                                    targetValue = if (showFabMenu) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                                    animationSpec = tween(durationMillis = 300),
                                    label = "fabContentColor"
                                )
                                FloatingActionButton(
                                    onClick = { showFabMenu = !showFabMenu },
                                    shape = RoundedCornerShape(fabShape),
                                    containerColor = fabContainerColor,
                                    contentColor = fabContentColor,
                                    interactionSource = interactionSource,
                                    modifier = Modifier.graphicsLayer(
                                        scaleX = fabScale,
                                        scaleY = fabScale
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (showFabMenu) Icons.Filled.Close else Icons.Filled.PowerSettingsNew,
                                        contentDescription = "Toggle FAB Menu"
                                    )
                                }
                            }
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    bottomBar = { BottomNavBar(navController, items, isAmoledMode = isAmoledMode) }
                ) { innerPadding ->
                    if (showKernelVerificationDialog) {
                        KernelVerificationDialog(onDismiss = { finish() })
                    }
                    if (showRootRequiredDialog) {
                        RootRequiredDialog(onDismiss = { 
                            finishAndRemoveTask()
                        })
                    }
                    // Only show permission dialog if device is rooted
                    if (showBatteryOptDialog && rootRepo.checkRootFresh()) {
                        BatteryOptDialog(
                            onDismiss = {
                                // Only allow dismiss if we haven't exceeded retry limit
                                if (permissionDenialCount < MAX_PERMISSION_RETRIES) {
                                    showBatteryOptDialog = false
                                }
                            },
                            onConfirm = {
                                showBatteryOptDialog = false
                                batteryOptChecker.checkAndRequestPermissions(this@MainActivity)
                            },
                            onExit = { finish() },
                            showExitButton = permissionDenialCount >= MAX_PERMISSION_RETRIES
                        )
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(
                            "home",
                            enterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)) },
                            exitTransition = { fadeOut(animationSpec = tween(150)) },
                            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                            popExitTransition = { fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.92f, animationSpec = tween(150)) }
                        ) { HomeScreen(navController = navController) }
                        composable(
                            "tuning",
                            enterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)) },
                            exitTransition = { fadeOut(animationSpec = tween(150)) },
                            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                            popExitTransition = { fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.92f, animationSpec = tween(150)) }
                        ) { TuningScreen(navController = navController) }
                        composable(
                            "misc",
                            enterTransition = { fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)) },
                            exitTransition = { fadeOut(animationSpec = tween(150)) },
                            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                            popExitTransition = { fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.92f, animationSpec = tween(150)) }
                        ) { MiscScreen(navController = navController) }
                        composable(
                            "settings",
                            enterTransition = {
                                slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(500))
                            }
                        ) {
                            SettingsScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }

    private fun checkAndHandlePermissions() {
        // Only check permissions if device is rooted
        if (rootRepo.checkRootFresh() && !batteryOptChecker.hasRequiredPermissions()) {
            showBatteryOptDialog = true
        } else if (rootRepo.checkRootFresh()) {
            // Only start service for Dynamic mode (10) which requires continuous monitoring
            // For other thermal modes, persistent scripts handle settings
        }
    }

    private fun isKernelSupported(): Boolean {
        // Check if the kernel has one of the supported signatures
        // This approach focuses on checking /proc/version which is more reliable
        val supportedSignatures = listOf(
            "bimoalfarrabi@github.com",
            "N0Kontzzz"
        )
        
        try {
            // Try to read /proc/version with root access
            var versionLine: String?

            try {
                val versionProcess = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat /proc/version"))
                val versionReader = BufferedReader(InputStreamReader(versionProcess.inputStream))
                versionLine = versionReader.readLine()
                versionReader.close()
                versionProcess.waitFor()
            } catch (e: Exception) {
                // If we can't read the file, assume kernel is not supported
                return false
            }
            
            // Check if the version string contains any supported signature
            if (versionLine != null) {
                for (signature in supportedSignatures) {
                    if (versionLine.contains(signature, ignoreCase = true)) {
                        return true
                    }
                }
            }
            
            // If no supported signature is found, kernel is not supported
            return false
        } catch (e: Exception) {
            // If we can't determine, assume it's not supported for security
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        
        // Check root status again in case user granted root access
        // This handles the scenario where user granted root access after the app started
        if (!rootRepo.checkRootFresh()) {
            showRootRequiredDialog = true
        } else {
            // If root access is now granted, hide the root required dialog
            showRootRequiredDialog = false
            
            // Don't check permissions if kernel verification dialog is shown
            if (showKernelVerificationDialog) {
                return
            }
            
            // Check if permissions were denied
            if (!batteryOptChecker.hasRequiredPermissions()) {
                permissionDenialCount++
                if (permissionDenialCount >= MAX_PERMISSION_RETRIES) {
                    // Show dialog with exit button after max retries
                    showBatteryOptDialog = true
                } else if (!showBatteryOptDialog) {
                    // Show normal dialog if not already showing
                    showBatteryOptDialog = true
                }
            } else {
                // Reset counter if permissions are granted
                permissionDenialCount = 0
                showBatteryOptDialog = false

                // Only start service for Dynamic mode (10) which requires continuous monitoring
                // For other thermal modes, persistent scripts handle settings
            }
        }
    }
}