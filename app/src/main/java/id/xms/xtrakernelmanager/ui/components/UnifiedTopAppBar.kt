package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import id.xms.xtrakernelmanager.R
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedTopAppBar(
    title: String,
    navController: NavController? = null,
    showSettingsIcon: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val systemUiController = rememberSystemUiController()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceColorAtElevation = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    val topBarContainerColor by androidx.compose.runtime.remember(scrollBehavior) {
        androidx.compose.runtime.derivedStateOf {
            scrollBehavior?.state?.overlappedFraction?.let { fraction ->
                lerp(
                    surfaceColor,
                    surfaceColorAtElevation,
                    fraction
                )
            } ?: surfaceColor
        }
    }
    
    val darkTheme = isSystemInDarkTheme()

    androidx.compose.runtime.LaunchedEffect(topBarContainerColor, darkTheme) {
        systemUiController.setStatusBarColor(
            color = topBarContainerColor,
            darkIcons = !darkTheme
        )
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            if (!showSettingsIcon && navController?.previousBackStackEntry != null) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        },
        actions = {
            if (showSettingsIcon && navController != null) {
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.home_settings_button_desc)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = topBarContainerColor
        ),
        scrollBehavior = scrollBehavior
    )
}