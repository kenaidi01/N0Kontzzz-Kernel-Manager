package id.nkz.nokontzzzmanager.ui.components

import android.os.Process
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import id.nkz.nokontzzzmanager.R

@Composable
fun RootRequiredDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismissal by tapping outside */ },
        title = { Text(text = stringResource(id = R.string.root_required)) },
        text = { Text(text = stringResource(id = R.string.root_required_desc)) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                // Force close the entire app process to ensure fresh start
                Process.killProcess(Process.myPid())
            }) {
                Text(text = stringResource(id = R.string.exit_app))
            }
        }
    )
}
