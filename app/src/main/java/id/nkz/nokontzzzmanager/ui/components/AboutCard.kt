package id.nkz.nokontzzzmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import id.nkz.nokontzzzmanager.R

data class Developer(val name: String, val role: String, val githubUsername: String, val drawableResId: Int)
data class RepositoryContributor(val name: String, val url: String, val description: String, val drawableResId: Int)

// Special recognition for the main developer who led this rebrand
val leadDeveloper = Developer("Viasco", "NKM developer", "bimoalfarrabi", R.drawable.viasco)

val individualContributors = listOf(
    Developer("Gustyx-Power", "XKM developer", "Gustyx-Power", R.drawable.gustyx_power),
    Developer("Radika", "RvKM developer", "Rve27", R.drawable.radika),
    Developer("Danda", "Help and support", "Danda420", R.drawable.danda)
)

val repositoryContributors = listOf(
    RepositoryContributor("Xtra Kernel Manager Repository", "https://github.com/Gustyx-Power/Xtra-Kernel-Manager", "Original project repository", R.drawable.xkm),
    RepositoryContributor("RvKernel Manager Repository", "https://github.com/Rve27/RvKernel-Manager", "Feature and code reference from this repository", R.drawable.rv)
)

@Composable
fun AboutCard(
    blur: Boolean,
    modifier: Modifier = Modifier,
    githubLink: String = stringResource(R.string.github_link),
    telegramLink: String = stringResource(R.string.telegram_link),
) {
    var showCreditsDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.nkm_logo),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(80.dp)
                    )
                }
                Text(
                    text = stringResource(id = R.string.about),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Description
            Text(
                text = stringResource(id = R.string.desc_about),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            // Action Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Social Links Row
                val uriHandler = LocalUriHandler.current
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Follow us:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    // Telegram Button
                    IconButton(
                        onClick = { uriHandler.openUri(telegramLink) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.telegram),
                            contentDescription = stringResource(id = R.string.telegram),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // GitHub Button
                    IconButton(
                        onClick = { uriHandler.openUri(githubLink) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.github),
                            contentDescription = stringResource(id = R.string.github),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Credits Badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { showCreditsDialog = true },
                        label = {
                            Text(
                                text = stringResource(id = R.string.credits),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }

    if (showCreditsDialog) {
        AnimatedVisibility(
            visible = showCreditsDialog,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        ) {
            AlertDialog(
                onDismissRequest = { showCreditsDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(id = R.string.credits))
                    }
                },
                text = {
                    val scrollState = rememberScrollState()
                    var isScrollbarVisible by remember { mutableStateOf(true) }
                    
                    LaunchedEffect(scrollState.value) {
                        isScrollbarVisible = true
                        delay(1000) // Hide scrollbar after 1 second of inactivity
                        isScrollbarVisible = false
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(end = 12.dp), // Add padding to make space for scrollbar
                            verticalArrangement = Arrangement.spacedBy(0.dp) // Remove default spacing since we use custom Spacer
                        ) {
                            // Lead Developer Section
                            Text(
                                text = "Developer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp)) // Add spacing between title and first card
                            
                            DeveloperCreditItem(developer = leadDeveloper, position = 0, totalItems = 1)
                            
                            // Separator
                            Spacer(modifier = Modifier.height(16.dp)) // Reduce spacing before section title
                            
                            // Individual Contributors Section
                            Text(
                                text = "Individual Contributors",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp)) // Add spacing between title and first card
                            
                            individualContributors.forEachIndexed { index, developer ->
                                if (index > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                DeveloperCreditItem(developer = developer, position = index, totalItems = individualContributors.size)
                            }
                            
                            // Repository Contributors Section
                            Spacer(modifier = Modifier.height(16.dp)) // Reduce spacing before section title
                            
                            Text(
                                text = "Repository Contributors",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp)) // Add spacing between title and first card
                            
                            repositoryContributors.forEachIndexed { index, repo ->
                                if (index > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                RepositoryCreditItem(repository = repo, position = index, totalItems = repositoryContributors.size)
                            }
                            
                            // Add some padding at the end
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Custom scrollbar with fade animation - properly positioned on the right
                        // Using a separate Box to position the entire scrollbar component
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                        ) {
                            // Track - always present but only visible when animated container is visible
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(6.dp)
                            ) {
                                AnimatedVisibility(
                                    visible = isScrollbarVisible,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 300))
                                ) {
                                    // Track background
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(6.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }
                            
                            // Thumb - separate element that can be positioned independently
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd) // Position at top-right of parent Box
                                    .offset(y = if (scrollState.maxValue > 0) {
                                        val trackHeight = 400f // Total height of the scrollbar track
                                        val thumbHeight = 30f // Height of the thumb
                                        val availableTrackHeight = trackHeight - thumbHeight
                                        val ratio = scrollState.value.toFloat() / scrollState.maxValue
                                        (availableTrackHeight * ratio).dp
                                    } else 0.dp)
                                    .width(6.dp)
                                    .height(30.dp) // Fixed height for thumb
                            ) {
                                AnimatedVisibility(
                                    visible = isScrollbarVisible,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 300))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .background(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showCreditsDialog = false }) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            )
        }
    }
}

@Composable
fun DeveloperCreditItem(developer: Developer, position: Int, totalItems: Int) {
    val uriHandler = LocalUriHandler.current
    val githubProfileUrl = "https://github.com/${developer.githubUsername}"
    
    // Determine rounded corners based on position
    val shape = when {
        position == 0 && position == totalItems - 1 -> RoundedCornerShape(24.dp) // Only item
        position == 0 -> RoundedCornerShape(24.dp, 24.dp, 4.dp, 4.dp) // First item: top corners 24dp, bottom 8dp
        position == totalItems - 1 -> RoundedCornerShape(4.dp, 4.dp, 24.dp, 24.dp) // Last item: top corners 8dp, bottom 24dp
        else -> RoundedCornerShape(4.dp) // Middle items: all corners 8dp
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(githubProfileUrl) },
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = developer.drawableResId),
                contentDescription = "${developer.name}'s profile picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = developer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = developer.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "@${developer.githubUsername}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RepositoryCreditItem(repository: RepositoryContributor, position: Int, totalItems: Int) {
    val uriHandler = LocalUriHandler.current
    
    // Determine rounded corners based on position
    val shape = when {
        position == 0 && position == totalItems - 1 -> RoundedCornerShape(24.dp) // Only item
        position == 0 -> RoundedCornerShape(24.dp, 24.dp, 8.dp, 8.dp) // First item: top corners 24dp, bottom 8dp
        position == totalItems - 1 -> RoundedCornerShape(8.dp, 8.dp, 24.dp, 24.dp) // Last item: top corners 8dp, bottom 24dp
        else -> RoundedCornerShape(8.dp) // Middle items: all corners 8dp
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(repository.url) },
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Repository icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Image(
                    painter = painterResource(id = repository.drawableResId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repository.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = repository.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "GitHub",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
