package id.nkz.nokontzzzmanager.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TcpCongestionDialog(
    currentAlgorithm: String,
    availableAlgorithms: List<String>,
    onAlgorithmSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAlgorithm by remember { mutableStateOf(currentAlgorithm) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "TCP Congestion Control Algorithm",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableAlgorithms, key = { it }) { algorithm ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                onAlgorithmSelected(algorithm)
                                onDismiss()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = selectedAlgorithm == algorithm,
                            onClick = { 
                                onAlgorithmSelected(algorithm)
                                onDismiss()
                            }
                        )
                        Text(
                            text = algorithm,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}